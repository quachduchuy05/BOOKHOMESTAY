package BookingHomeStay.BookingHomeStay.service.impl;

import BookingHomeStay.BookingHomeStay.dto.sepay.SepayWebhookPayload;
import BookingHomeStay.BookingHomeStay.entity.Booking;
import BookingHomeStay.BookingHomeStay.entity.PaymentPolicy;
import BookingHomeStay.BookingHomeStay.repository.BookingRepository;
import BookingHomeStay.BookingHomeStay.repository.PaymentRepository;
import BookingHomeStay.BookingHomeStay.service.BookingService;
import BookingHomeStay.BookingHomeStay.service.EmailService;
import BookingHomeStay.BookingHomeStay.service.SepayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class SepayServiceImpl implements SepayService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final EmailService emailService;

    @Value("${sepay.api-key:}")
    private String sepayApiKey;

    @Value("${sepay.booking-prefix:BK}")
    private String bookingPrefix;

    @Override
    public boolean verifyApiKey(String authorizationHeader) {
        if (sepayApiKey == null || sepayApiKey.isBlank() || sepayApiKey.equals("sepay_api_secret_key_placeholder")) {
            // Cho phép test trên môi trường phát triển khi chưa cấu hình key thật
            return true;
        }
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return false;
        }
        String token = authorizationHeader.replaceFirst("(?i)^Apikey\\s+", "").trim();
        return sepayApiKey.trim().equals(token);
    }

    @Override
    @Transactional
    public Map<String, Object> processWebhook(SepayWebhookPayload payload) {
        if (payload == null || payload.getId() == null) {
            return Map.of("success", false, "message", "Payload hoặc ID giao dịch không hợp lệ");
        }

        // 1. Kiểm tra Idempotency trực tiếp trong bảng payments (chống xử lý trùng lặp giao dịch)
        if (paymentRepository.existsBySepayTransactionId(payload.getId())) {
            log.info("SePay Webhook: Giao dịch ID {} đã được ghi nhận trong bảng payments.", payload.getId());
            return Map.of(
                    "success", true,
                    "message", "Giao dịch đã được xử lý trước đó",
                    "id", payload.getId()
            );
        }

        // 2. Chỉ xử lý dòng tiền vào (Khách hàng thanh toán tiền cọc / toàn bộ đơn phòng)
        String transferType = payload.getTransferType() != null ? payload.getTransferType().toLowerCase().trim() : "in";
        if (!"in".equals(transferType)) {
            log.info("SePay Webhook: Bỏ qua biến động loại '{}' (không phải thanh toán vào của khách hàng)", transferType);
            return Map.of(
                    "success", true,
                    "message", "Bỏ qua giao dịch không phải tiền vào",
                    "id", payload.getId()
            );
        }

        // 3. Trích xuất mã đơn bookingCode từ nội dung chuyển khoản
        String contentText = ((payload.getContent() != null ? payload.getContent() : "") + " "
                + (payload.getDescription() != null ? payload.getDescription() : "")).toUpperCase();

        String prefix = (bookingPrefix != null && !bookingPrefix.isBlank()) ? bookingPrefix.toUpperCase().trim() : "BK";
        Pattern pattern = Pattern.compile("\\b(" + Pattern.quote(prefix) + "[0-9A-Z]+)\\b");
        Matcher matcher = pattern.matcher(contentText);

        if (!matcher.find()) {
            log.warn("SePay Webhook: Không tìm thấy mã đơn tiền tố {} trong nội dung '{}'", prefix, contentText);
            return Map.of("success", false, "message", "Không tìm thấy mã đơn đặt phòng hợp lệ", "id", payload.getId());
        }

        String extractedCode = matcher.group(1);
        Booking booking = bookingRepository.findByBookingCode(extractedCode).orElse(null);

        if (booking == null) {
            log.warn("SePay Webhook: Không tìm thấy đơn đặt phòng với mã {}", extractedCode);
            return Map.of("success", false, "message", "Không tìm thấy đơn đặt phòng: " + extractedCode, "id", payload.getId());
        }

        // 4. Kiểm tra số tiền chuyển
        BigDecimal requiredAmount = (booking.getPaymentPolicy() == PaymentPolicy.DEPOSIT)
                ? booking.getRequiredDeposit()
                : booking.getFinalAmount();
        BigDecimal transferAmount = payload.getTransferAmount() != null ? payload.getTransferAmount() : BigDecimal.ZERO;

        if (transferAmount.compareTo(requiredAmount) < 0) {
            log.warn("SePay Webhook: Đơn {} yêu cầu {} VNĐ nhưng nhận được {} VNĐ",
                    extractedCode, requiredAmount, transferAmount);
            return Map.of(
                    "success", false,
                    "message", "Số tiền chuyển không đủ",
                    "required", requiredAmount,
                    "actual", transferAmount
            );
        }

        // 5. Cập nhật trực tiếp bản ghi Payment và Booking
        String txRef = payload.getReferenceCode() != null ? payload.getReferenceCode() : String.valueOf(payload.getId());
        bookingService.markPaymentPaidBySystem(booking.getId(), txRef, payload.getId(), payload.getGateway());

        // 6. Gửi email xác nhận thanh toán thành công cho khách hàng
        try {
            if (booking.getCustomerEmail() != null && !booking.getCustomerEmail().isBlank()) {
                String subject = "[BookingHomeStay] Xác nhận thanh toán thành công đơn #" + booking.getBookingCode();
                String body = "Xin chào " + booking.getCustomerName() + ",\n\n"
                        + "Hệ thống BookingHomeStay đã nhận được khoản thanh toán: "
                        + String.format("%,d VNĐ", transferAmount.longValue()) + " cho đơn #" + booking.getBookingCode() + ".\n"
                        + "Cổng thanh toán: " + (payload.getGateway() != null ? payload.getGateway() : "Ngân hàng") + "\n"
                        + "Mã tham chiếu: " + txRef + "\n"
                        + "Trạng thái đơn phòng của bạn hiện đã được XÁC NHẬN (CONFIRMED).\n\n"
                        + "Cảm ơn bạn đã tin tưởng dịch vụ của chúng tôi!";
                emailService.send(booking.getCustomerEmail(), subject, body);
            }
        } catch (Exception e) {
            log.warn("Không gửi được email thông báo cho đơn {}: {}", extractedCode, e.getMessage());
        }

        log.info("SePay Webhook: Xác nhận thanh toán thành công cho đơn {}", extractedCode);

        return Map.of(
                "success", true,
                "message", "Xác nhận thanh toán thành công cho đơn " + extractedCode,
                "bookingCode", extractedCode,
                "id", payload.getId()
        );
    }
}
