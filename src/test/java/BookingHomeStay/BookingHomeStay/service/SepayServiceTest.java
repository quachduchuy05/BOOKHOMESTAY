package BookingHomeStay.BookingHomeStay.service;

import BookingHomeStay.BookingHomeStay.dto.sepay.SepayWebhookPayload;
import BookingHomeStay.BookingHomeStay.entity.Booking;
import BookingHomeStay.BookingHomeStay.entity.BookingStatus;
import BookingHomeStay.BookingHomeStay.entity.PaymentPolicy;
import BookingHomeStay.BookingHomeStay.repository.BookingRepository;
import BookingHomeStay.BookingHomeStay.repository.PaymentRepository;
import BookingHomeStay.BookingHomeStay.service.impl.SepayServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SepayServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingService bookingService;

    @Mock
    private EmailService emailService;

    private SepayServiceImpl sepayService;

    @BeforeEach
    void setUp() {
        sepayService = new SepayServiceImpl(
                paymentRepository,
                bookingRepository,
                bookingService,
                emailService
        );
        ReflectionTestUtils.setField(sepayService, "sepayApiKey", "test_secret_key_123");
        ReflectionTestUtils.setField(sepayService, "bookingPrefix", "BK");
    }

    @Test
    void testVerifyApiKey() {
        assertTrue(sepayService.verifyApiKey("Apikey test_secret_key_123"));
        assertTrue(sepayService.verifyApiKey("test_secret_key_123"));
        assertFalse(sepayService.verifyApiKey("Apikey wrong_key"));
        assertFalse(sepayService.verifyApiKey(null));
    }

    @Test
    void testProcessWebhookInflowSuccess() {
        SepayWebhookPayload payload = new SepayWebhookPayload();
        payload.setId(1001L);
        payload.setGateway("MBBank");
        payload.setTransferType("in");
        payload.setTransferAmount(BigDecimal.valueOf(1500000));
        payload.setContent("BK123456 thanh toan tien coc");
        payload.setReferenceCode("FT260911001");
        payload.setTransactionDate("2026-09-11 10:00:00");

        Booking booking = Booking.builder()
                .id(99L)
                .bookingCode("BK123456")
                .paymentPolicy(PaymentPolicy.FULL_PREPAYMENT)
                .finalAmount(BigDecimal.valueOf(1500000))
                .status(BookingStatus.PENDING_PAYMENT)
                .customerName("Nguyen Van Khach")
                .customerEmail("khach@gmail.com")
                .build();

        when(paymentRepository.existsBySepayTransactionId(1001L)).thenReturn(false);
        when(bookingRepository.findByBookingCode("BK123456")).thenReturn(Optional.of(booking));

        Map<String, Object> result = sepayService.processWebhook(payload);

        assertTrue((Boolean) result.get("success"));
        assertEquals("BK123456", result.get("bookingCode"));

        // Verify payment updated in payments table and email sent
        verify(bookingService).markPaymentPaidBySystem(99L, "FT260911001", 1001L, "MBBank");
        verify(emailService).send(eq("khach@gmail.com"), anyString(), anyString());
    }

    @Test
    void testProcessWebhookIdempotency() {
        SepayWebhookPayload payload = new SepayWebhookPayload();
        payload.setId(1002L);

        when(paymentRepository.existsBySepayTransactionId(1002L)).thenReturn(true);

        Map<String, Object> result = sepayService.processWebhook(payload);

        assertTrue((Boolean) result.get("success"));
        assertTrue(((String) result.get("message")).contains("đã được xử lý trước đó"));

        verifyNoInteractions(bookingService);
    }

    @Test
    void testProcessWebhookOutflowIgnored() {
        SepayWebhookPayload payload = new SepayWebhookPayload();
        payload.setId(1003L);
        payload.setTransferType("out");

        when(paymentRepository.existsBySepayTransactionId(1003L)).thenReturn(false);

        Map<String, Object> result = sepayService.processWebhook(payload);

        assertTrue((Boolean) result.get("success"));
        assertTrue(((String) result.get("message")).contains("không phải tiền vào"));

        verifyNoInteractions(bookingService);
    }
}
