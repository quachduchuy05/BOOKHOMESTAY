package BookingHomeStay.BookingHomeStay.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileForm {
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    private String phone;
    private String address;
    private String avatar;
    private MultipartFile avatarFile;
}
