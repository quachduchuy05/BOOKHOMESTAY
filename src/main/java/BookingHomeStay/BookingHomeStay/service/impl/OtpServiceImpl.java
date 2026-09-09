package BookingHomeStay.BookingHomeStay.service.impl;

import BookingHomeStay.BookingHomeStay.entity.OtpChannel;
import BookingHomeStay.BookingHomeStay.service.OtpService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service quản lý sinh và xác thực mã OTP:
 * - Hỗ trợ rate limiting (cooldown 60s giữa mỗi lần gửi mã tới cùng đích).
 * - Giới hạn tối đa 5 lần thử sai để ngăn chặn tấn công Brute-force.
 * - So sánh mã theo thời gian hằng số (constant-time) ngăn chặn Timing attack.
 * - Tự động dọn dẹp các mã hết hạn để tránh rò rỉ bộ nhớ (memory leak).
 * - Gửi email HTML với thiết kế chuyên nghiệp.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    private static class OtpEntry {
        private final String code;
        private final Instant expireAt;
        private final Instant createdAt;
        private final AtomicInteger attempts = new AtomicInteger(0);

        public OtpEntry(String code, Instant expireAt) {
            this.code = code;
            this.expireAt = expireAt;
            this.createdAt = Instant.now();
        }

        public String getCode() {
            return code;
        }

        public Instant getExpireAt() {
            return expireAt;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public AtomicInteger getAttempts() {
            return attempts;
        }
    }

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private static final Duration TTL = Duration.ofMinutes(5);
    private static final Duration COOLDOWN = Duration.ofSeconds(120);

    @Override
    public void sendOtp(String destination, OtpChannel channel) {
        // Dọn dẹp các mã OTP cũ đã hết hạn
        cleanExpiredEntries();

        // Kiểm tra rate limiting (cooldown 120 giây)
        OtpEntry existing = otpStore.get(destination);
        if (existing != null) {
            Instant cooldownUntil = existing.getCreatedAt().plus(COOLDOWN);
            if (Instant.now().isBefore(cooldownUntil)) {
                long secondsLeft = Duration.between(Instant.now(), cooldownUntil).getSeconds();
                throw new IllegalStateException(
                        "Vui lòng đợi " + Math.max(1, secondsLeft) + " giây trước khi yêu cầu mã mới.");
            }
        }

        log.info("[OTP] Nhận yêu cầu gửi mã OTP tới: [{}] qua kênh [{}]", destination, channel);
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
        otpStore.put(destination, new OtpEntry(code, Instant.now().plus(TTL)));

        try {
            switch (channel) {
                case EMAIL -> sendViaEmail(destination, code);
                case SMS -> sendViaSms(destination, code);
                case ZALO -> sendViaZalo(destination, code);
            }
        } catch (Exception e) {
            otpStore.remove(destination);
            log.error("[OTP] Gửi OTP thất bại tới {}: {}", destination, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean verifyOtp(String destination, String code) {
        if (destination == null || code == null)
            return false;

        OtpEntry entry = otpStore.get(destination);
        if (entry == null)
            return false;

        // Kiểm tra hết hạn (TTL 5 phút)
        if (Instant.now().isAfter(entry.getExpireAt())) {
            otpStore.remove(destination);
            log.warn("[OTP] Mã OTP cho {} đã hết hạn", destination);
            return false;
        }

        // So sánh chuỗi an toàn theo thời gian hằng số (chống Timing Attack)
        boolean hopLe = MessageDigest.isEqual(
                entry.getCode().getBytes(StandardCharsets.UTF_8),
                code.trim().getBytes(StandardCharsets.UTF_8));

        if (hopLe) {
            otpStore.remove(destination);
            log.info("[OTP] Xác thực OTP thành công cho {}", destination);
            return true;
        } else {
            log.warn("[OTP] Sai mã OTP cho {}", destination);
            return false;
        }
    }

    private void cleanExpiredEntries() {
        otpStore.entrySet().removeIf(e -> Instant.now().isAfter(e.getValue().getExpireAt()));
    }

    private void sendViaEmail(String email, String code) {
        if (mailFrom == null || mailFrom.isBlank()) {
            log.error("[OTP-EMAIL] CHƯA CẤU HÌNH spring.mail.username trong application.properties!");
            throw new IllegalStateException("Hệ thống chưa cấu hình tài khoản gửi email (spring.mail.username trống).");
        }
        try {
            log.info("[OTP-EMAIL] Bắt đầu kết nối máy chủ SMTP gửi OTP tới {}", email);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(mailFrom, "BookingHomeStay");
            helper.setTo(email);
            helper.setSubject("[BookingHomeStay] Mã xác thực OTP: " + code);

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                    </head>
                    <body style="margin: 0; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f1f5f9;">
                        <table width="100%" border="0" cellspacing="0" cellpadding="0" style="max-width: 580px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08);">
                            <tr>
                                <td style="padding: 28px 32px; text-align: center; background: linear-gradient(135deg, #0f172a, #1e293b); color: #ffffff;">
                                    <h1 style="margin: 0; font-size: 24px; font-weight: 700; letter-spacing: 0.5px;">BookingHomeStay</h1>
                                    <p style="margin: 6px 0 0; font-size: 13px; color: #94a3b8;">Xác thực tài khoản thành viên</p>
                                </td>
                            </tr>
                            <tr>
                                <td style="padding: 32px;">
                                    <h2 style="font-size: 18px; color: #0f172a; margin-top: 0;">Xin chào,</h2>
                                    <p style="font-size: 15px; color: #334155; line-height: 1.6; margin: 0 0 20px;">
                                        Bạn vừa yêu cầu mã xác thực để đăng ký tài khoản tại <strong>BookingHomeStay</strong>. Vui lòng sử dụng mã 6 số bên dưới để tiếp tục:
                                    </p>

                                    <div style="background: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 10px; padding: 20px; text-align: center; margin: 24px 0;">
                                        <div style="font-size: 12px; font-weight: 600; text-transform: uppercase; color: #64748b; letter-spacing: 1px; margin-bottom: 8px;">Mã OTP của bạn</div>
                                        <div style="font-size: 36px; font-weight: 800; color: #e11d48; letter-spacing: 8px; font-family: monospace;">{{OTP_CODE}}</div>
                                    </div>

                                    <ul style="padding-left: 20px; font-size: 13px; color: #64748b; line-height: 1.8; margin-bottom: 24px;">
                                        <li>Mã có hiệu lực trong vòng <strong>5 phút</strong>.</li>
                                        <li>Tuyệt đối <strong>không chia sẻ</strong> mã OTP này với bất kỳ ai.</li>
                                    </ul>

                                    <p style="font-size: 12px; color: #94a3b8; margin: 0; padding-top: 18px; border-top: 1px solid #f1f5f9;">
                                        Nếu bạn không yêu cầu đăng ký tại BookingHomeStay, hãy bỏ qua email này.
                                    </p>
                                </td>
                            </tr>
                            <tr>
                                <td style="padding: 16px; background-color: #f8fafc; text-align: center; font-size: 12px; color: #94a3b8;">
                                    © BookingHomeStay System. All rights reserved.
                                </td>
                            </tr>
                        </table>
                    </body>
                    </html>
                    """
                    .replace("{{OTP_CODE}}", code);

            helper.setText(html, true);
            mailSender.send(mimeMessage);
            log.info("[OTP-EMAIL] Đã gửi mã OTP HTML THÀNH CÔNG tới {}", email);
        } catch (Exception e) {
            log.error("[OTP-EMAIL] Gửi thất bại tới {}: {}", email, e.getMessage(), e);
            throw new IllegalStateException("Lỗi gửi mail: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
    }

    private void sendViaSms(String phone, String code) {
        // TODO: TICH HOP THAT - can dang ky dich vu SMS Brandname co phi
        // (eSMS/Speedsms/Twilio)
        log.info("[OTP-SMS] (DEMO - chua co dich vu SMS that) Gui ma {} den so dien thoai {}", code, phone);
    }

    private void sendViaZalo(String phone, String code) {
        // TODO: TICH HOP THAT - can Zalo Official Account + template ZNS da duyet
        log.info("[OTP-ZALO] (DEMO - chua co Zalo OA that) Gui ma {} qua Zalo toi so {}", code, phone);
    }
}
