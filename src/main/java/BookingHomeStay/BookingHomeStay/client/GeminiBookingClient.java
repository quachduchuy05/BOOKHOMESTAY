package BookingHomeStay.BookingHomeStay.client;

import BookingHomeStay.BookingHomeStay.dto.ai.BookingFilterExtracted;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class GeminiBookingClient {

        private static final Logger log = LoggerFactory.getLogger(GeminiBookingClient.class);
        private final RestClient restClient;
        private final String apiKey;
        private final ObjectMapper objectMapper;

        public GeminiBookingClient(
                        RestClient.Builder restClientBuilder,
                        @Value("${gemini.api.key:${gemini_api_key:}}") String apiKey,
                        ObjectMapper objectMapper) {
                this.restClient = restClientBuilder.baseUrl("https://generativelanguage.googleapis.com").build();
                this.apiKey = apiKey;
                this.objectMapper = objectMapper;
        }

        public BookingFilterExtracted extractFilters(String userMessage, BookingFilterExtracted currentFilters) {
                String currentDateStr = java.time.LocalDate.now().toString();
                String previousFiltersStr = "{}";
                if (currentFilters != null) {
                        try {
                                previousFiltersStr = objectMapper.writeValueAsString(currentFilters);
                        } catch (Exception e) {
                                log.warn("Could not serialize current filters", e);
                        }
                }

                Map<String, Object> requestBody = Map.of(
                                "systemInstruction", Map.of(
                                                "parts", List.of(Map.of("text",
                                                                "Bạn là một Chuyên gia tư vấn du lịch (AI Travel Agent) cực kỳ thông minh, linh hoạt và thấu hiểu tâm lý khách hàng.\n"
                                                                                +
                                                                                "HÔM NAY LÀ NGÀY: " + currentDateStr
                                                                                + " (Định dạng YYYY-MM-DD).\n\n"
                                                                                +
                                                                                "NHIỆM VỤ CỦA BẠN: Trích xuất các điều kiện tìm kiếm phòng từ lời nói của khách và trả về JSON. KHÔNG ĐƯỢC CỨNG NHẮC! Hãy coi đây là một tính năng 'GỢI Ý'. Nếu khách miêu tả chung chung, bạn HÃY TỰ DO SUY LUẬN để điền các thông số sao cho hợp lý nhất.\n\n"
                                                                                +
                                                                                "CÁC NGUYÊN TẮC SUY LUẬN (TỰ DO & THOẢI MÁI):\n"
                                                                                +
                                                                                "1. ĐỊA ĐIỂM (provinces/districts): Khách có thể tìm 1 hoặc NHIỀU địa điểm (Ví dụ: 'Đà Lạt hoặc Hà Nội'). Hãy trả về danh sách các địa điểm. Giữ nguyên tên địa danh khách cung cấp.\n"
                                                                                +
                                                                                "2. SỐ NGƯỜI (guestCount): Nếu khách nói rõ '2 người', hãy điền 2. Nếu khách nói 'đi tuần trăng mật', tự hiểu là 2 người. Nếu khách nói 'đi gia đình', có thể tự điền 4. Nếu nói 'đi team building đông', cứ thoải mái điền 10 hoặc 15 người.\n"
                                                                                +
                                                                                "3. THỜI GIAN (checkInDate / checkOutDate): Tự động tính toán ngày dựa vào ngày hôm nay. Nếu khách nói 'cuối tuần', hãy tính ra Thứ 7, Chủ Nhật. Nếu khách nói 'đi nghỉ 2 ngày' mà không biết khi nào, bạn HOÀN TOÀN CÓ THỂ tự chọn bừa một ngày cuối tuần gần nhất hoặc tuần tới để gợi ý luôn cho khách!\n"
                                                                                +
                                                                                "4. TIỆN ÍCH (amenityGroups): NHÓM CÁC TỪ ĐỒNG NGHĨA. Khách có thể yêu cầu nhiều tiện ích (AND). Với mỗi yêu cầu, hãy tự nghĩ ra các từ đồng nghĩa (OR). Ví dụ khách nói 'bơi lội và nấu nướng', bạn hãy trả về 2 nhóm: [[\"bơi lội\", \"bể bơi\", \"hồ bơi\"], [\"nấu nướng\", \"bếp\", \"BBQ\"]]. Nếu khách nói 'tuần trăng mật', tự nghĩ ra [[\"bồn tắm\"], [\"lãng mạn\"]].\n\n"
                                                                                +
                                                                                "5. MỨC GIÁ (priceMin / priceMax): Khách có thể nói 'dưới 1 triệu', 'từ 500k đến 2 triệu'. Hãy tự quy đổi 'k', 'tr', 'triệu' thành con số nguyên (VD: 1 triệu = 1000000). Nếu khách nói 'rẻ', có thể tự set priceMax tầm 500000. Để xóa lọc giá, BẮT BUỘC trả về -1.\n\n"
                                                                                +
                                                                                "6. LỜI NHẮN (explanation): BẮT BUỘC viết 1 câu giao tiếp tự nhiên bằng tiếng Việt để phản hồi khách. Yêu cầu xưng hô là 'mình' (bản thân AI) và 'bạn' (khách hàng). Nếu khách nói CHƯA RÕ địa điểm hoặc số người, hãy hỏi lại (Ví dụ: 'Bạn muốn đi đâu và đi mấy người vậy?'). Nếu khách đã cung cấp đủ thông tin, hãy xác nhận (Ví dụ: 'Tuyệt vời, để mình tìm phòng ở Đà Lạt cho 2 người nhé').\n\n"
                                                                                +
                                                                                "7. LƯU Ý QUAN TRỌNG VỀ XÓA VÀ THAY ĐỔI (PATCH UPDATE): Tôi sẽ cung cấp 'Bộ lọc hiện tại'. Dựa vào yêu cầu MỚI NHẤT, bạn chỉ trích xuất phần thay đổi:\n"
                                                                                +
                                                                                "- Nếu thông tin KHÔNG ĐỔI: Bắt buộc trả về null (hoặc không include key đó).\n"
                                                                                +
                                                                                "- Nếu khách muốn XÓA BỎ (ví dụ: 'bỏ hồ bơi đi'): Với các trường mảng (provinces, districts, amenityGroups), hãy trả về mảng rỗng [] để hệ thống xóa. Với số (giá tiền, số người, số phòng), trả về -1 để hệ thống xóa.\n"
                                                                                +
                                                                                "- Nếu khách muốn THÊM/SỬA mảng đã có: Hãy trả về TOÀN BỘ mảng mới (cả cũ + mới). Ví dụ cũ có [\"Đà Lạt\"], khách đòi \"thêm Nha Trang\", bạn trả về [\"Đà Lạt\", \"Nha Trang\"].\n\n"
                                                                                +
                                                                                "TÓM LẠI: Đừng bắt khách hàng phải cung cấp thông tin hoàn hảo. Hãy dùng trí thông minh của bạn để phỏng đoán, bù đắp thông tin. Nếu thiếu thông tin hoặc thông tin không thay đổi, BẮT BUỘC trả về giá trị null (tuyệt đối không điền số 0, không điền chuỗi chữ 'null')."))),
                                "contents", List.of(
                                                Map.of("parts", List.of(Map.of("text",
                                                                "BỘ LỌC HIỆN TẠI (Định dạng JSON):\n"
                                                                                + previousFiltersStr
                                                                                + "\n\nYÊU CẦU MỚI CỦA KHÁCH:\n"
                                                                                + userMessage)))),
                                "generationConfig", Map.of(
                                                "responseMimeType", "application/json",
                                                "responseSchema", Map.of(
                                                                "type", "OBJECT",
                                                                "properties", java.util.Map.ofEntries(
                                                                                java.util.Map.entry("provinces", Map.of(
                                                                                                "type", "ARRAY",
                                                                                                "items",
                                                                                                Map.of("type", "STRING"),
                                                                                                "description",
                                                                                                "Danh sách Tỉnh/Thành phố (nếu có nhiều)",
                                                                                                "nullable", true)),
                                                                                java.util.Map.entry("districts", Map.of(
                                                                                                "type", "ARRAY",
                                                                                                "items",
                                                                                                Map.of("type", "STRING"),
                                                                                                "description",
                                                                                                "Danh sách Quận/Huyện (nếu có nhiều)",
                                                                                                "nullable", true)),
                                                                                java.util.Map.entry("checkInDate",
                                                                                                Map.of("type", "STRING",
                                                                                                                "description",
                                                                                                                "Ngày nhận phòng (YYYY-MM-DD)",
                                                                                                                "nullable",
                                                                                                                true)),
                                                                                java.util.Map.entry("checkOutDate",
                                                                                                Map.of("type", "STRING",
                                                                                                                "description",
                                                                                                                "Ngày trả phòng (YYYY-MM-DD)",
                                                                                                                "nullable",
                                                                                                                true)),
                                                                                java.util.Map.entry("guestCount",
                                                                                                Map.of("type", "INTEGER",
                                                                                                                "description",
                                                                                                                "Tổng số người đi (Ví dụ: 2, 4, 16)",
                                                                                                                "nullable",
                                                                                                                true)),
                                                                                java.util.Map.entry("roomCount", Map.of(
                                                                                                "type", "INTEGER",
                                                                                                "description",
                                                                                                "Số lượng phòng",
                                                                                                "nullable", true)),
                                                                                java.util.Map.entry("priceMin", Map.of(
                                                                                                "type", "NUMBER",
                                                                                                "description",
                                                                                                "Giá tối thiểu",
                                                                                                "nullable", true)),
                                                                                java.util.Map.entry("priceMax", Map.of(
                                                                                                "type", "NUMBER",
                                                                                                "description",
                                                                                                "Giá tối đa",
                                                                                                "nullable", true)),
                                                                                java.util.Map.entry("amenityGroups",
                                                                                                Map.of("type", "ARRAY",
                                                                                                                "items",
                                                                                                                Map.of("type", "ARRAY",
                                                                                                                                "items",
                                                                                                                                Map.of("type", "STRING")),
                                                                                                                "description",
                                                                                                                "Mảng 2 chiều chứa các nhóm tiện ích đồng nghĩa. VD: [[\"hồ bơi\", \"bể bơi\"], [\"BBQ\", \"nướng\"]]",
                                                                                                                "nullable",
                                                                                                                true)),
                                                                                java.util.Map.entry("sortPreference",
                                                                                                Map.of("type", "STRING",
                                                                                                                "description",
                                                                                                                "Sở thích sắp xếp",
                                                                                                                "nullable",
                                                                                                                true)),
                                                                                java.util.Map.entry("explanation",
                                                                                                Map.of("type", "STRING",
                                                                                                                "description",
                                                                                                                "Câu trả lời giao tiếp tự nhiên với khách hàng",
                                                                                                                "nullable",
                                                                                                                true))))));

                try {
                        byte[] responseBytes = restClient.post()
                                        .uri(java.net.URI.create(
                                                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                                                                        + apiKey))
                                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                        .acceptCharset(java.nio.charset.StandardCharsets.UTF_8)
                                        .body(requestBody)
                                        .retrieve()
                                        .body(byte[].class);

                        String responseStr = new String(responseBytes, java.nio.charset.StandardCharsets.UTF_8);
                        Map<String, Object> response = objectMapper.readValue(responseStr, Map.class);

                        // Navigate through the Gemini response structure
                        // { "candidates": [ { "content": { "parts": [ { "text": "{\"province\": ...}" }
                        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                        if (candidates != null && !candidates.isEmpty()) {
                                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                                if (content != null) {
                                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content
                                                        .get("parts");
                                        if (parts != null && !parts.isEmpty()) {
                                                String jsonText = (String) parts.get(0).get("text");
                                                log.info("Raw JSON response từ Gemini: {}", jsonText); // Thêm log ở đây
                                                return objectMapper.readValue(jsonText, BookingFilterExtracted.class);
                                        }
                                }
                        }
                        return new BookingFilterExtracted();
                } catch (org.springframework.web.client.HttpStatusCodeException e) {
                        log.error("Failed to call Gemini API. Status: {}, Body: {}", e.getStatusCode(),
                                        e.getResponseBodyAsString(),
                                        e);
                        return new BookingFilterExtracted(); // Return empty on failure
                } catch (Exception e) {
                        log.error("Failed to call Gemini API", e);
                        return new BookingFilterExtracted(); // Return empty on failure
                }
        }
}
