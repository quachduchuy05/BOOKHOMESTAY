package BookingHomeStay.BookingHomeStay.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingFilterExtracted {
    private List<String> provinces;
    private List<String> districts;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer guestCount;
    private Integer roomCount;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private List<List<String>> amenityGroups;
    private String sortPreference;
    private String explanation;

    // Helper method to merge new filters into this existing session filter
    public void mergeWith(BookingFilterExtracted newFilters) {
        if (newFilters.getProvinces() != null) this.provinces = newFilters.getProvinces();
        if (newFilters.getDistricts() != null) this.districts = newFilters.getDistricts();
        if (newFilters.getCheckInDate() != null) this.checkInDate = newFilters.getCheckInDate();
        if (newFilters.getCheckOutDate() != null) this.checkOutDate = newFilters.getCheckOutDate();
        if (newFilters.getGuestCount() != null) {
            this.guestCount = newFilters.getGuestCount() == -1 ? null : newFilters.getGuestCount();
        }
        if (newFilters.getRoomCount() != null) {
            this.roomCount = newFilters.getRoomCount() == -1 ? null : newFilters.getRoomCount();
        }
        if (newFilters.getPriceMin() != null) {
            this.priceMin = newFilters.getPriceMin().compareTo(java.math.BigDecimal.valueOf(-1)) == 0 ? null : newFilters.getPriceMin();
        }
        if (newFilters.getPriceMax() != null) {
            this.priceMax = newFilters.getPriceMax().compareTo(java.math.BigDecimal.valueOf(-1)) == 0 ? null : newFilters.getPriceMax();
        }
        if (newFilters.getAmenityGroups() != null) this.amenityGroups = newFilters.getAmenityGroups();
        if (newFilters.getSortPreference() != null) this.sortPreference = newFilters.getSortPreference();
    }
}
