package BookingHomeStay.BookingHomeStay.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordForm {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Vui lòng nhập mã OTP")
    private String otpCode;

    @NotBlank(message = "Vui lòng nhập mật khẩu mới")
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    private String newPassword;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu mới")
    private String confirmPassword;
}
