package BookingHomeStay.BookingHomeStay.service.impl;

import BookingHomeStay.BookingHomeStay.entity.Booking;
import BookingHomeStay.BookingHomeStay.service.VietQrService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * QUAN TRONG - GIAI THICH CHO NGUOI PHAT TRIEN SAU:
 * Lop nay KHONG goi API co phi/can API-key cua ngan hang. No chi dung dich vu
 * anh QR mien phi cong khai cua VietQR.io (img.vietqr.io) de dung san 1 URL
 * <img>. Khi trinh duyet load URL nay, VietQR.io se ve ra ma QR chuan VietQR
 * (theo chuan EMVCo cua NAPAS) da nhung san: so tai khoan + ten chu TK + so
 * tien + noi dung. Nguoi dung CHI VIEC quet ma bang app ngan hang bat ky
 * (hoac MoMo) la moi thong tin duoc dien tu dong, khong can go tay.
 * Tai lieu: https://www.vietqr.io/danh-sach-api/
 *
 * Cau hinh 3 gia tri BANK_BIN / SO_TAI_KHOAN / TEN_CHU_TK trong application.properties
 * bang cac key: vietqr.bank-bin, vietqr.account-no, vietqr.account-name
 * (hien dang de gia tri demo, PHAI thay bang tai khoan ngan hang that truoc khi
 * dua len production).
 */
@Service
public class VietQrServiceImpl implements VietQrService {

    @Value("${vietqr.bank-bin:970422}")
    private String bankBin;

    @Value("${vietqr.account-no}")
    private String accountNo;

    @Value("${vietqr.account-name:BOOKING HOMESTAY}")
    private String accountName;

    @Value("${vietqr.template:compact2}")
    private String template; // cac template co san cua VietQR: compact, compact2, qr_only, print

    @Override
    public String buildQrUrl(Booking booking) {
        BigDecimal amount = (booking.getRequiredDeposit() != null && booking.getRequiredDeposit().compareTo(BigDecimal.ZERO) > 0)
                ? booking.getRequiredDeposit()
                : (booking.getFinalAmount() != null ? booking.getFinalAmount() : BigDecimal.ZERO);

        // Giữ mã đơn bookingCode rõ ràng ở đầu để SePay nhận diện chính xác
        String noiDung = booking.getBookingCode();

        String encodedAccountName = URLEncoder.encode(accountName, StandardCharsets.UTF_8);
        String encodedNoiDung = URLEncoder.encode(noiDung, StandardCharsets.UTF_8);

        return String.format(
                "https://img.vietqr.io/image/%s-%s-%s.png?amount=%s&addInfo=%s&accountName=%s",
                bankBin, accountNo, template,
                amount.toBigInteger().toString(),
                encodedNoiDung, encodedAccountName
        );
    }
}
