package BookingHomeStay.BookingHomeStay.controller.api;

import BookingHomeStay.BookingHomeStay.dto.ai.BookingChatRequest;
import BookingHomeStay.BookingHomeStay.dto.ai.BookingSuggestionResponse;
import BookingHomeStay.BookingHomeStay.service.AiBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-booking")
@RequiredArgsConstructor
public class AiBookingController {

    private final AiBookingService aiBookingService;

    @PostMapping("/chat")
    public ResponseEntity<BookingSuggestionResponse> chat(@RequestBody BookingChatRequest request) {
        BookingSuggestionResponse response = aiBookingService.processChat(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/manual-query")
    public ResponseEntity<BookingSuggestionResponse> manualQuery(
            @RequestParam String sessionId,
            @RequestBody BookingHomeStay.BookingHomeStay.dto.ai.BookingFilterExtracted filters) {
        BookingSuggestionResponse response = aiBookingService.processManualQuery(sessionId, filters);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-availability/{roomId}")
    public ResponseEntity<Map<String, Boolean>> checkAvailability(
            @PathVariable Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        
        boolean isAvailable = aiBookingService.checkRoomAvailability(roomId, checkIn, checkOut);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }
}
