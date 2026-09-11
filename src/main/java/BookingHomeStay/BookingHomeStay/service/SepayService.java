package BookingHomeStay.BookingHomeStay.service;

import BookingHomeStay.BookingHomeStay.dto.sepay.SepayWebhookPayload;

import java.util.Map;

public interface SepayService {

    /**
     * Xác thực header Authorization: Apikey <TOKEN> từ SePay.
     */
    boolean verifyApiKey(String authorizationHeader);

    /**
     * Xử lý webhook biến động số dư từ SePay cho thanh toán đặt phòng (Khách hàng ➔ Admin):
     * - Khớp mã đơn đặt phòng BK...
     * - Kiểm tra số tiền chuyển
     * - Cập nhật trực tiếp bản ghi Payment sang PAID và Booking sang CONFIRMED.
     */
    Map<String, Object> processWebhook(SepayWebhookPayload payload);
}
