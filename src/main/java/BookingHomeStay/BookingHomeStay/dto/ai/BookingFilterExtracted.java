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
        if (newFilters.getProvinces() != null && !newFilters.getProvinces().isEmpty()) {
            if (this.provinces == null) this.provinces = new ArrayList<>();
            for (String p : newFilters.getProvinces()) {
                if (!this.provinces.contains(p)) this.provinces.add(p);
            }
        }
        if (newFilters.getDistricts() != null && !newFilters.getDistricts().isEmpty()) {
            if (this.districts == null) this.districts = new ArrayList<>();
            for (String d : newFilters.getDistricts()) {
                if (!this.districts.contains(d)) this.districts.add(d);
            }
        }
        if (newFilters.getCheckInDate() != null) this.checkInDate = newFilters.getCheckInDate();
        if (newFilters.getCheckOutDate() != null) this.checkOutDate = newFilters.getCheckOutDate();
        if (newFilters.getGuestCount() != null) this.guestCount = newFilters.getGuestCount();
        if (newFilters.getRoomCount() != null) this.roomCount = newFilters.getRoomCount();
        if (newFilters.getPriceMin() != null) this.priceMin = newFilters.getPriceMin();
        if (newFilters.getPriceMax() != null) this.priceMax = newFilters.getPriceMax();
        
        if (newFilters.getAmenityGroups() != null && !newFilters.getAmenityGroups().isEmpty()) {
            if (this.amenityGroups == null) {
                this.amenityGroups = new ArrayList<>();
            }
            // For simplicity, just append new amenity groups if they are not identical
            for (List<String> group : newFilters.getAmenityGroups()) {
                if (!this.amenityGroups.contains(group)) {
                    this.amenityGroups.add(group);
                }
            }
        }
        if (newFilters.getSortPreference() != null) this.sortPreference = newFilters.getSortPreference();
    }
}
