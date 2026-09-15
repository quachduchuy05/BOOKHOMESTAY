package BookingHomeStay.BookingHomeStay.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingChatRequest {
    private String sessionId;
    private String message;
}
