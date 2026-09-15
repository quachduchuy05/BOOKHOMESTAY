package BookingHomeStay.BookingHomeStay.service;

import BookingHomeStay.BookingHomeStay.dto.DayAvailabilityDto;
import BookingHomeStay.BookingHomeStay.entity.RoomAvailabilityStatus;

import java.time.LocalDate;
import java.util.List;

public interface RoomAvailabilityService {
    List<DayAvailabilityDto> getMonthCalendar(Long roomId, int year, int month);
    void lockDate(Long roomId, LocalDate date, Long hostUserId);
    void unlockDate(Long roomId, LocalDate date, Long hostUserId);
    RoomAvailabilityStatus toggleDate(Long roomId, LocalDate date, Long hostUserId);
    void lockRange(Long roomId, LocalDate start, LocalDate end, Long hostUserId);
    void unlockRange(Long roomId, LocalDate start, LocalDate end, Long hostUserId);
    boolean isRoomAvailableForDates(Long roomId, LocalDate checkIn, LocalDate checkOut);
    void recordBookingDays(Long roomId, LocalDate checkIn, LocalDate checkOut);
    void releaseBookingDays(Long roomId, LocalDate checkIn, LocalDate checkOut);
}
