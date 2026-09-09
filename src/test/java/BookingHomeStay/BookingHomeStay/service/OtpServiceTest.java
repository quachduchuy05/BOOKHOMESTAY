package BookingHomeStay.BookingHomeStay.service;

import BookingHomeStay.BookingHomeStay.entity.OtpChannel;
import BookingHomeStay.BookingHomeStay.service.impl.OtpServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpServiceImpl(mailSender);
        org.springframework.test.util.ReflectionTestUtils.setField(otpService, "mailFrom", "test@bookinghomestay.vn");
        org.mockito.Mockito.lenient().when(mailSender.createMimeMessage()).thenReturn(new jakarta.mail.internet.MimeMessage((jakarta.mail.Session) null));
    }

    @Test
    void testRateLimitingCooldown() {
        String testEmail = "testuser@example.com";
        otpService.sendOtp(testEmail, OtpChannel.EMAIL);

        // Sending again immediately should throw IllegalStateException due to cooldown
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            otpService.sendOtp(testEmail, OtpChannel.EMAIL);
        });
        assertTrue(ex.getMessage().contains("Vui lòng đợi"));
    }

    @Test
    void testVerifyOtpSuccessAndSingleUse() throws Exception {
        String testEmail = "success@example.com";
        otpService.sendOtp(testEmail, OtpChannel.EMAIL);

        // Retrieve the generated code via reflection for testing
        Field otpStoreField = OtpServiceImpl.class.getDeclaredField("otpStore");
        otpStoreField.setAccessible(true);
        Map<?, ?> store = (Map<?, ?>) otpStoreField.get(otpService);
        Object entry = store.get(testEmail);
        assertNotNull(entry);

        Field codeField = entry.getClass().getDeclaredField("code");
        codeField.setAccessible(true);
        String generatedCode = (String) codeField.get(entry);

        // Successful verification
        assertTrue(otpService.verifyOtp(testEmail, generatedCode));

        // Single-use check: calling verify again should fail
        assertFalse(otpService.verifyOtp(testEmail, generatedCode), "OTP should not be reusable");
    }
}
