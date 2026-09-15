package BookingHomeStay.BookingHomeStay.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSuggestionResponse {
    private String systemPrompt; // Used to ask the user for missing info (e.g. province, guest count)
    private BookingFilterExtracted currentFilters;
    private List<RoomSuggestionDto> suggestions;
    private List<String> relaxedFields; // E.g., ["priceMin", "priceMax", "amenities"]
}
