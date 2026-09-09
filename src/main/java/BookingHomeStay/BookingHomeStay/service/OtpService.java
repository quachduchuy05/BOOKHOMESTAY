package BookingHomeStay.BookingHomeStay.service;

import BookingHomeStay.BookingHomeStay.entity.OtpChannel;

public interface OtpService {
    /** Sinh ma OTP 6 so va "gui" toi dia chi (email/so dien thoai) qua kenh tuong ung. */
    void sendOtp(String destination, OtpChannel channel);

    /** Kiem tra ma OTP nguoi dung nhap co dung va con han khong. Dung 1 lan roi bi xoa. */
    boolean verifyOtp(String destination, String code);
}
