package BookingHomeStay.BookingHomeStay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceDto {
    private String name;
    private String region;      // "bac", "trung", "nam"
    private String regionName;  // "Miền Bắc", "Miền Trung & Tây Nguyên", "Miền Nam"
    private String description;
    private String imageUrl;
    private boolean featured;
}
