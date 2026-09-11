package BookingHomeStay.BookingHomeStay.controller;

import BookingHomeStay.BookingHomeStay.dto.sepay.SepayWebhookPayload;
import BookingHomeStay.BookingHomeStay.entity.Booking;
import BookingHomeStay.BookingHomeStay.repository.BookingRepository;
import BookingHomeStay.BookingHomeStay.service.SepayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SepayWebhookController {

    private final SepayService sepayService;
    private final BookingRepository bookingRepository;

    /**
     * Endpoint tiếp nhận Webhook biến động số dư từ SePay.
     * Cấu hình trên SePay: URL: https://<domain>/api/v1/sepay/webhook
     * Header: Authorization: Apikey <SEPAY_API_KEY>
     */
    @PostMapping("/sepay/webhook")
    public ResponseEntity<Map<String, Object>> handleSepayWebhook(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody SepayWebhookPayload payload
    ) {
        log.info("SePay Webhook received: ID={}, Gateway={}, Type={}, Amount={}, Content='{}'",
                payload != null ? payload.getId() : null,
                payload != null ? payload.getGateway() : null,
                payload != null ? payload.getTransferType() : null,
                payload != null ? payload.getTransferAmount() : null,
                payload != null ? payload.getContent() : null);

        // 1. Xác thực Token bảo mật từ SePay
        if (!sepayService.verifyApiKey(authHeader)) {
            log.warn("SePay Webhook từ chối: Authorization header không hợp lệ: {}", authHeader);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized API Key"));
        }

        // 2. Xử lý nghiệp vụ (Tiền vào hoặc tiền ra)
        try {
            Map<String, Object> result = sepayService.processWebhook(payload);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi khi xử lý SePay Webhook payload ID {}: {}", payload != null ? payload.getId() : "null", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi xử lý nội bộ: " + e.getMessage()));
        }
    }

    /**
     * API kiểm tra trạng thái thanh toán của đơn đặt phòng (phục vụ Polling realtime ở frontend).
     */
    @GetMapping("/bookings/{bookingCode}/status")
    public ResponseEntity<Map<String, Object>> checkBookingPaymentStatus(@PathVariable String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode).orElse(null);
        if (booking == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Không tìm thấy đơn đặt phòng"));
        }

        boolean isPaid = booking.getStatus() != BookingHomeStay.BookingHomeStay.entity.BookingStatus.PENDING_PAYMENT;
        return ResponseEntity.ok(Map.of(
                "success", true,
                "bookingCode", booking.getBookingCode(),
                "status", booking.getStatus().name(),
                "isPaid", isPaid
        ));
    }
}
