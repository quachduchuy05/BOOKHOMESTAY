package BookingHomeStay.BookingHomeStay.service;

import BookingHomeStay.BookingHomeStay.dto.ai.BookingChatRequest;
import BookingHomeStay.BookingHomeStay.dto.ai.BookingSuggestionResponse;

import java.time.LocalDate;

public interface AiBookingService {
    BookingSuggestionResponse processChat(BookingChatRequest request);
    BookingSuggestionResponse processManualQuery(String sessionId, BookingHomeStay.BookingHomeStay.dto.ai.BookingFilterExtracted manualFilters);
    boolean checkRoomAvailability(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);
}
