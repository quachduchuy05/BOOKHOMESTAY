package BookingHomeStay.BookingHomeStay.service;

import BookingHomeStay.BookingHomeStay.dto.ChangePasswordForm;
import BookingHomeStay.BookingHomeStay.dto.ProfileForm;
import BookingHomeStay.BookingHomeStay.dto.RegisterRequest;
import BookingHomeStay.BookingHomeStay.dto.ResetPasswordForm;
import BookingHomeStay.BookingHomeStay.entity.User;

public interface UserService {
    User register(RegisterRequest request);
    User findById(Long userId);
    User capNhatHoSo(Long userId, ProfileForm form);
    void doiMatKhau(Long userId, ChangePasswordForm form);
    void sendForgotPasswordOtp(String email);
    void datLaiMatKhau(ResetPasswordForm form);
}