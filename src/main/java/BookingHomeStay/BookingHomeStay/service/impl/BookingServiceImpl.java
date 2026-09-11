package BookingHomeStay.BookingHomeStay.service.impl;

import BookingHomeStay.BookingHomeStay.dto.BookingRequest;
import BookingHomeStay.BookingHomeStay.entity.*;
import BookingHomeStay.BookingHomeStay.exception.BookingConflictException;
import BookingHomeStay.BookingHomeStay.exception.OwnershipException;
import BookingHomeStay.BookingHomeStay.exception.ResourceNotFoundException;
import BookingHomeStay.BookingHomeStay.repository.*;
import BookingHomeStay.BookingHomeStay.service.BookingService;
import BookingHomeStay.BookingHomeStay.service.EmailService;
import BookingHomeStay.BookingHomeStay.service.PromotionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PromotionService promotionService;
    private final CollaboratorRepository collaboratorRepository;
    private final EmailService emailService;
    private final BookingHomeStay.BookingHomeStay.service.CollaboratorService collaboratorService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return bookingDetailRepository.findOverlapping(roomId, checkIn, checkOut).isEmpty();
    }

    @Override
    @Transactional
    public Booking createBooking(BookingRequest request, Long userId) {
        if (!request.getCheckoutDate().isAfter(request.getCheckinDate())) {
            throw new IllegalArgumentException("Ngay tra phong phai sau ngay nhan phong");
        }

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phong"));

        // ===== Kiem tra trung lich o Service Layer - khong chi dua vao JS =====
        List<BookingDetail> overlap = bookingDetailRepository.findOverlapping(
                room.getId(), request.getCheckinDate(), request.getCheckoutDate());
        if (!overlap.isEmpty()) {
            throw new BookingConflictException("Phong da co nguoi dat trong khoang ngay ban chon");
        }

        long nights = ChronoUnit.DAYS.between(request.getCheckinDate(), request.getCheckoutDate());
        BigDecimal subtotal = room.getPricePerNight()
                .multiply(BigDecimal.valueOf(nights))
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        BigDecimal discount = BigDecimal.ZERO;
        Promotion promotion = null;
        if (request.getPromotionCode() != null && !request.getPromotionCode().isBlank()) {
            promotion = promotionService.validate(request.getPromotionCode(), subtotal);
            discount = promotionService.calculateDiscount(promotion, subtotal);
        }

        BigDecimal serviceFee = subtotal.multiply(BigDecimal.valueOf(0.05));
        BigDecimal finalAmount = subtotal.subtract(discount).add(serviceFee);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay tai khoan"));

        // ===== TASK 4: xac dinh nguon goc don (truc tiep / qua CTV) =====
        BookingSource source = BookingSource.DIRECT;
        String matchedReferralCode = null;
        // Tam thoi bo qua logic lien quan den CTV theo yeu cau
        /*
        if (request.getReferralCode() != null && !request.getReferralCode().isBlank()) {
            var ctv = collaboratorRepository.findByMaGioiThieuAndStatus(
                    request.getReferralCode().trim(), CollaboratorStatus.APPROVED);
            if (ctv.isPresent()) {
                source = BookingSource.COLLABORATOR;
                matchedReferralCode = ctv.get().getMaGioiThieu();
            }
        }
        */

        PaymentPolicy policy;
        try { policy = PaymentPolicy.valueOf(request.getPaymentPolicy()); }
        catch (Exception e) { policy = PaymentPolicy.PAY_AT_PROPERTY; }

        BigDecimal requiredDeposit = BigDecimal.ZERO;
        BigDecimal remainingBalance = finalAmount;

        if (policy == PaymentPolicy.FULL_PREPAYMENT) {
            requiredDeposit = finalAmount;
            remainingBalance = BigDecimal.ZERO;
        } else if (policy == PaymentPolicy.DEPOSIT) {
            requiredDeposit = finalAmount.multiply(new BigDecimal("0.30"));
            remainingBalance = finalAmount.subtract(requiredDeposit);
        }

        BookingStatus initialStatus = (requiredDeposit.compareTo(BigDecimal.ZERO) > 0) ? BookingStatus.PENDING_PAYMENT : BookingStatus.CONFIRMED;
        LocalDateTime expiredAt = (initialStatus == BookingStatus.PENDING_PAYMENT) ? LocalDateTime.now().plusHours(2) : null;

        Booking booking = Booking.builder()
                .bookingCode("BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .user(user)
                .promotion(promotion)
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .totalAmount(subtotal)
                .discountAmount(discount)
                .serviceFee(serviceFee)
                .finalAmount(finalAmount)
                .note(request.getNote())
                .status(initialStatus)
                .source(source)
                .collaboratorCode(matchedReferralCode)
                .paymentPolicy(policy)
                .requiredDeposit(requiredDeposit)
                .remainingBalance(remainingBalance)
                .expiredAt(expiredAt)
                .build();

        BookingDetail detail = BookingDetail.builder()
                .booking(booking)
                .room(room)
                .checkinDate(request.getCheckinDate())
                .checkoutDate(request.getCheckoutDate())
                .quantity(request.getQuantity())
                .pricePerNight(room.getPricePerNight())
                .subtotal(subtotal)
                .build();
        booking.getDetails().add(detail);
        booking = bookingRepository.save(booking);

        // Neu don can thanh toan online (DEPOSIT hoac FULL_PREPAYMENT) thi tao ban ghi Payment UNPAID de doi soat
        if (requiredDeposit.compareTo(BigDecimal.ZERO) > 0) {
            PaymentMethod method = null;
            if (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank()) {
                try {
                    method = PaymentMethod.valueOf(request.getPaymentMethod().trim().toUpperCase());
                } catch (Exception ignored) {}
            }
            if (method == null) {
                method = PaymentMethod.BANK_TRANSFER;
            }

            PaymentPhase phase = (policy == PaymentPolicy.DEPOSIT) ? PaymentPhase.DEPOSIT : PaymentPhase.FULL_PAYMENT;
            Payment initialPayment = Payment.builder()
                    .booking(booking)
                    .paymentPhase(phase)
                    .paymentMethod(method)
                    .amount(requiredDeposit)
                    .status(PaymentStatus.UNPAID)
                    .build();
            paymentRepository.save(initialPayment);
            booking.getPayments().add(initialPayment);
        }

        return booking;
    }

    @Override
    public List<Booking> getMyBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = findOrThrow(bookingId);
        if (!booking.getUser().getId().equals(userId)) {
            throw new OwnershipException("Ban khong co quyen huy don nay");
        }
        if (booking.getStatus() != BookingStatus.PENDING 
                && booking.getStatus() != BookingStatus.PENDING_PAYMENT 
                && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Khong the huy don o trang thai hien tai");
        }
        LocalDate checkinDate = booking.getDetails().stream()
                .map(BookingDetail::getCheckinDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        long daysUntilCheckin = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), checkinDate);
        boolean eligibleForRefund = daysUntilCheckin >= 2;

        boolean hasAdvancePayment = booking.getPaymentPolicy() != PaymentPolicy.PAY_AT_PROPERTY;
        boolean hasPaid = booking.getPayments() != null && booking.getPayments().stream()
                .anyMatch(p -> p.getStatus() == PaymentStatus.PAID);

        boolean shouldRefund = eligibleForRefund && (hasAdvancePayment || hasPaid);

        if (shouldRefund) {
            booking.setStatus(BookingStatus.PENDING_REFUND);
            if (booking.getPayments() != null && !booking.getPayments().isEmpty()) {
                for (Payment p : booking.getPayments()) {
                    if (p.getStatus() == PaymentStatus.PAID || p.getStatus() == PaymentStatus.UNPAID) {
                        p.setStatus(PaymentStatus.PENDING_REFUND);
                        paymentRepository.save(p);
                    }
                }
            } else if (hasAdvancePayment) {
                BigDecimal refundAmount = booking.getPaymentPolicy() == PaymentPolicy.DEPOSIT 
                        ? booking.getRequiredDeposit() 
                        : booking.getFinalAmount();
                Payment p = Payment.builder()
                        .booking(booking)
                        .paymentMethod(PaymentMethod.BANK_TRANSFER)
                        .amount(refundAmount)
                        .status(PaymentStatus.PENDING_REFUND)
                        .paymentPhase(booking.getPaymentPolicy() == PaymentPolicy.DEPOSIT ? PaymentPhase.DEPOSIT : PaymentPhase.FULL_PAYMENT)
                        .build();
                paymentRepository.save(p);
            }
        } else {
            booking.setStatus(BookingStatus.CANCELLED);
            if (booking.getPayments() != null) {
                for (Payment p : booking.getPayments()) {
                    if (p.getStatus() == PaymentStatus.UNPAID) {
                        p.setStatus(PaymentStatus.FAILED);
                        paymentRepository.save(p);
                    }
                }
            }
        }
        bookingRepository.save(booking);
    }

    @Override
    public List<Booking> getBookingsOfHost(Long hostUserId) {
        return bookingRepository.findByDetails_Room_Homestay_Host_IdOrderByCreatedAtDesc(hostUserId);
    }

    @Override
    @Transactional
    public void confirmBooking(Long bookingId, Long hostUserId) {
        Booking booking = getForHostOrThrow(bookingId, hostUserId);
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void rejectBooking(Long bookingId, Long hostUserId) {
        Booking booking = getForHostOrThrow(bookingId, hostUserId);
        booking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void checkIn(Long bookingId, Long hostUserId) {
        Booking booking = getForHostOrThrow(bookingId, hostUserId);
        booking.setStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void checkOut(Long bookingId, Long hostUserId) {
        Booking booking = getForHostOrThrow(bookingId, hostUserId);
        
        // Neu da thanh toan -> Hoan thanh COMPLETED, chua thanh toan -> CHECKED_OUT
        if (booking.isFullyPaid()) {
            booking.setStatus(BookingStatus.COMPLETED);
        } else {
            booking.setStatus(BookingStatus.CHECKED_OUT);
        }
        bookingRepository.save(booking);

        // Task 9: sau khi khach TRA PHONG xong, tu dong gui email xin danh gia
        String tenHomestay = booking.getDetails().get(0).getRoom().getHomestay().getName();
        emailService.send(
                booking.getUser().getEmail(),
                "Bạn cảm thấy thế nào về kỳ nghỉ tại " + tenHomestay + "?",
                "Xin chào " + booking.getCustomerName() + ",\n\n"
                        + "Cảm ơn bạn đã sử dụng dịch vụ tại " + tenHomestay + " (mã đơn: " + booking.getBookingCode() + ").\n"
                        + "Chúng tôi rất mong nhận được đánh giá của bạn để cải thiện chất lượng dịch vụ.\n"
                        + "Vui lòng đăng nhập và để lại đánh giá tại: " + baseUrl + "/khach-hang/don-dat-phong\n\n"
                        + "Trân trọng cảm ơn!"
        );
    }

    @Override
    @Transactional
    public void markPaymentPaid(Long bookingId, Long hostUserId) {
        Booking booking = getForHostOrThrow(bookingId, hostUserId);
        if (booking.isFullyPaid()) {
            return; // Da thanh toan du
        }

        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
            Payment depositPayment = null;
            if (booking.getPayments() != null) {
                depositPayment = booking.getPayments().stream()
                        .filter(p -> p.getStatus() == PaymentStatus.UNPAID)
                        .findFirst()
                        .orElse(null);
            }

            if (depositPayment != null) {
                depositPayment.setStatus(PaymentStatus.PAID);
                depositPayment.setPaymentTime(LocalDateTime.now());
                paymentRepository.save(depositPayment);
            } else {
                PaymentPhase phase = (booking.getPaymentPolicy() == PaymentPolicy.DEPOSIT)
                        ? PaymentPhase.DEPOSIT
                        : PaymentPhase.FULL_PAYMENT;
                depositPayment = Payment.builder()
                        .booking(booking)
                        .paymentPhase(phase)
                        .paymentMethod(PaymentMethod.BANK_TRANSFER)
                        .amount(booking.getRequiredDeposit())
                        .status(PaymentStatus.PAID)
                        .paymentTime(LocalDateTime.now())
                        .build();
                paymentRepository.save(depositPayment);
                booking.getPayments().add(depositPayment);
            }
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            return;
        }

        Payment remainingPayment = Payment.builder()
                .booking(booking)
                .paymentPhase(PaymentPhase.REMAINING)
                .paymentMethod(PaymentMethod.DIRECT)
                .amount(booking.getRemainingBalance())
                .status(PaymentStatus.PAID)
                .paymentTime(LocalDateTime.now())
                .build();
        paymentRepository.save(remainingPayment);
        booking.getPayments().add(remainingPayment);

        // Neu don da CHECKED_OUT ma gio thanh toan xong thi chuyen sang COMPLETED
        if (booking.getStatus() == BookingStatus.CHECKED_OUT) {
            booking.setStatus(BookingStatus.COMPLETED);
        }
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void completeBooking(Long bookingId, Long hostUserId) {
        Booking booking = getForHostOrThrow(bookingId, hostUserId);
        booking.setStatus(BookingStatus.COMPLETED);
        if (!booking.isFullyPaid()) {
            Payment remainingPayment = Payment.builder()
                    .booking(booking)
                    .paymentPhase(PaymentPhase.REMAINING)
                    .paymentMethod(PaymentMethod.DIRECT)
                    .amount(booking.getRemainingBalance())
                    .status(PaymentStatus.PAID)
                    .paymentTime(LocalDateTime.now())
                    .build();
            paymentRepository.save(remainingPayment);
            booking.getPayments().add(remainingPayment);
        }
        bookingRepository.save(booking);
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public Booking getBookingForCustomer(Long bookingId, Long customerUserId) {
        Booking booking = findOrThrow(bookingId);
        if (!booking.getUser().getId().equals(customerUserId)) {
            throw new OwnershipException("Ban khong co quyen truy cap don nay");
        }
        return booking;
    }

    @Override
    @Transactional
    public void refundBooking(Long bookingId, Long hostUserId) {
        Booking booking = getForHostOrThrow(bookingId, hostUserId);
        if (booking.getStatus() != BookingStatus.PENDING_REFUND) {
            throw new IllegalStateException("Don nay khong o trang thai cho hoan tien");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        if (booking.getPayments() != null) {
            for (Payment p : booking.getPayments()) {
                if (p.getStatus() == PaymentStatus.PENDING_REFUND) {
                    p.setStatus(PaymentStatus.REFUNDED);
                    p.setRefundTime(java.time.LocalDateTime.now());
                    paymentRepository.save(p);
                }
            }
        }
        bookingRepository.save(booking);
    }

    private Booking findOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay don id=" + id));
    }

    private Booking getForHostOrThrow(Long bookingId, Long hostUserId) {
        Booking booking = findOrThrow(bookingId);
        Long ownerId = booking.getDetails().get(0).getRoom().getHomestay().getHost().getUser().getId();
        if (!ownerId.equals(hostUserId)) {
            throw new OwnershipException("Don nay khong thuoc homestay cua ban");
        }
        return booking;
    }
}