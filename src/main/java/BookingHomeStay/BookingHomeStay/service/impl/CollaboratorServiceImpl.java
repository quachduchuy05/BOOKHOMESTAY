package BookingHomeStay.BookingHomeStay.service.impl;

import BookingHomeStay.BookingHomeStay.entity.*;
import BookingHomeStay.BookingHomeStay.exception.ResourceNotFoundException;
import BookingHomeStay.BookingHomeStay.repository.*;
import BookingHomeStay.BookingHomeStay.service.CollaboratorService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CollaboratorServiceImpl implements CollaboratorService {

    private final CollaboratorRepository collaboratorRepository;
    private final CollaboratorCommissionRepository commissionRepository;
    private final CollaboratorWithdrawalRepository withdrawalRepository;
    private final CollaboratorReferralClickRepository clickRepository;
    private final HomestayRepository homestayRepository;

    @Override
    public Collaborator getMyCollaborator(Long userId) {
        return collaboratorRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Tài khoản chưa đăng ký làm Cộng tác viên"
                        )
                );
    }

    @Override
    public Map<String, Object> getDashboard(Long userId) {
        Collaborator c = getMyCollaborator(userId);

        BigDecimal available = nz(
                commissionRepository.sumCommissionAmountByCollaboratorIdAndStatus(
                        c.getId(),
                        CommissionStatus.AVAILABLE
                )
        );

        BigDecimal paid = nz(
                commissionRepository.sumCommissionAmountByCollaboratorIdAndStatus(
                        c.getId(),
                        CommissionStatus.PAID
                )
        );

        BigDecimal pending = nz(
                commissionRepository.sumCommissionAmountByCollaboratorIdAndStatus(
                        c.getId(),
                        CommissionStatus.PENDING
                )
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("collaborator", c);
        data.put("clicks", clickRepository.countByCollaboratorId(c.getId()));
        data.put("bookings", commissionRepository.countByCollaboratorId(c.getId()));
        data.put("available", available);
        data.put("paid", paid);
        data.put("pending", pending);
        data.put("homestays", getShareableHomestays());

        return data;
    }

    @Override
    public List<Homestay> getShareableHomestays() {
        return homestayRepository.findByStatus(HomestayStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void recordClick(String code, String homestaySlug) {
        if (code == null || code.isBlank()) {
            return;
        }

        collaboratorRepository
                .findByMaGioiThieuAndStatus(code.trim(), CollaboratorStatus.APPROVED)
                .ifPresent(c -> {
                    CollaboratorReferralClick click = CollaboratorReferralClick.builder()
                            .collaborator(c)
                            .homestaySlug(homestaySlug)
                            .build();
                    clickRepository.save(click);
                });
    }

    @Override
    public List<CollaboratorCommission> getMyCommissions(Long userId) {
        Collaborator c = getMyCollaborator(userId);
        return commissionRepository.findByCollaboratorIdOrderByCreatedAtDesc(c.getId());
    }

    @Override
    public List<CollaboratorWithdrawal> getMyWithdrawals(Long userId) {
        Collaborator c = getMyCollaborator(userId);
        return withdrawalRepository.findByCollaboratorIdOrderByCreatedAtDesc(c.getId());
    }

    @Override
    @Transactional
    public CollaboratorWithdrawal requestWithdrawal(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền rút phải lớn hơn 0");
        }

        Collaborator c = getMyCollaborator(userId);

        if (c.getStatus() != CollaboratorStatus.APPROVED) {
            throw new IllegalStateException("Cộng tác viên chưa được Admin duyệt");
        }

        BigDecimal available = nz(
                commissionRepository.sumCommissionAmountByCollaboratorIdAndStatus(
                        c.getId(),
                        CommissionStatus.AVAILABLE
                )
        );

        // Trừ các khoản đang chờ xử lý/rút
        BigDecimal reserved = withdrawalRepository
                .findByStatusOrderByCreatedAtAsc(WithdrawalStatus.PENDING)
                .stream()
                .filter(w -> w.getCollaborator().getId().equals(c.getId()))
                .map(CollaboratorWithdrawal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal realAvailable = available.subtract(reserved);

        if (amount.compareTo(realAvailable) > 0) {
            throw new IllegalArgumentException(
                    "Số dư khả dụng không đủ. Số dư có thể rút: " + realAvailable
            );
        }

        if (c.getSoTaiKhoanNhanHoaHong() == null || c.getSoTaiKhoanNhanHoaHong().isBlank() ||
                c.getTenNganHang() == null || c.getTenNganHang().isBlank()) {
            throw new IllegalArgumentException("Vui lòng cập nhật tài khoản ngân hàng trước khi rút tiền");
        }

        return withdrawalRepository.save(
                CollaboratorWithdrawal.builder()
                        .collaborator(c)
                        .amount(amount)
                        .bankAccountNumber(c.getSoTaiKhoanNhanHoaHong())
                        .bankName(c.getTenNganHang())
                        .status(WithdrawalStatus.PENDING)
                        .build()
        );
    }

    @Override
    public List<CollaboratorWithdrawal> getPendingWithdrawals() {
        return withdrawalRepository.findByStatusOrderByCreatedAtAsc(WithdrawalStatus.PENDING);
    }

    @Override
    @Transactional
    public void approveWithdrawal(Long withdrawalId) {
        CollaboratorWithdrawal w = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu rút tiền"));

        if (w.getStatus() != WithdrawalStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu không còn ở trạng thái chờ duyệt");
        }

        w.setStatus(WithdrawalStatus.APPROVED);
        w.setProcessedAt(LocalDateTime.now());
        withdrawalRepository.save(w);
    }

    @Override
    @Transactional
    public void rejectWithdrawal(Long withdrawalId, String note) {
        CollaboratorWithdrawal w = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu rút tiền"));

        if (w.getStatus() != WithdrawalStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu không còn ở trạng thái chờ duyệt");
        }

        w.setStatus(WithdrawalStatus.REJECTED);
        w.setNote(note);
        w.setProcessedAt(LocalDateTime.now());
        withdrawalRepository.save(w);
    }

    @Override
    @Transactional
    public void markWithdrawalPaid(Long withdrawalId) {
        CollaboratorWithdrawal w = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu rút tiền"));

        if (w.getStatus() != WithdrawalStatus.APPROVED) {
            throw new IllegalStateException("Chỉ có yêu cầu đã duyệt mới được thanh toán");
        }

        w.setStatus(WithdrawalStatus.PAID);
        w.setProcessedAt(LocalDateTime.now());
        withdrawalRepository.save(w);

        // Đánh dấu các commission đã được thanh toán
        List<CollaboratorCommission> commissions = commissionRepository
                .findByCollaboratorIdOrderByCreatedAtDesc(w.getCollaborator().getId());

        BigDecimal remaining = w.getAmount();

        for (CollaboratorCommission commission : commissions) {
            if (remaining.signum() <= 0) {
                break;
            }

            if (commission.getStatus() != CommissionStatus.AVAILABLE) {
                continue;
            }

            BigDecimal commissionAmount = commission.getCommissionAmount();

            if (commissionAmount.compareTo(remaining) <= 0) {
                remaining = remaining.subtract(commissionAmount);
                commission.setStatus(CommissionStatus.PAID);
                commission.setPaidAt(LocalDateTime.now());
                commissionRepository.save(commission);
            }
        }
    }

    @Override
    @Transactional
    public void createCommissionForBooking(Booking booking) {
        if (booking == null) {
            return;
        }

        if (booking.getCollaboratorCode() == null || booking.getCollaboratorCode().isBlank()) {
            return;
        }

        // Không tạo trùng commission
        if (commissionRepository.findByBookingId(booking.getId()).isPresent()) {
            return;
        }

        Collaborator c = collaboratorRepository
                .findByMaGioiThieuAndStatus(booking.getCollaboratorCode(), CollaboratorStatus.APPROVED)
                .orElse(null);

        if (c == null) {
            return;
        }

        BigDecimal rate = c.getCommissionRate() == null ? BigDecimal.valueOf(5) : c.getCommissionRate();
        BigDecimal bookingAmount = booking.getFinalAmount();

        if (bookingAmount == null || bookingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal commissionAmount = bookingAmount
                .multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        CollaboratorCommission commission = CollaboratorCommission.builder()
                .collaborator(c)
                .booking(booking)
                .commissionRate(rate)
                .bookingAmount(bookingAmount)
                .commissionAmount(commissionAmount)
                .status(CommissionStatus.AVAILABLE)
                .build();

        commissionRepository.save(commission);
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
