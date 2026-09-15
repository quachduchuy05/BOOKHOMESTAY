package BookingHomeStay.BookingHomeStay.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomSuggestionDto {
    private Long homestayId;
    private Long roomId;
    private String homestayName;
    private String roomName;
    private BigDecimal price;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;
    private Integer guestCount;
    private Integer roomCount; // Recommended room count
    private List<String> matchedAmenities;
    private String reason;
}
