package BookingHomeStay.BookingHomeStay.dto;

import BookingHomeStay.BookingHomeStay.entity.RoomAvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayAvailabilityDto {
    private LocalDate date;
    private int dayOfMonth;
    private int dayOfWeek; // 1 = Monday ... 7 = Sunday
    private RoomAvailabilityStatus status;
    private boolean past;
    private boolean today;
    private boolean currentMonth;
}
