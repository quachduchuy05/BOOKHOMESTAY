package BookingHomeStay.BookingHomeStay.service;

import BookingHomeStay.BookingHomeStay.dto.ReviewRequest;
import BookingHomeStay.BookingHomeStay.entity.*;
import BookingHomeStay.BookingHomeStay.exception.OwnershipException;
import BookingHomeStay.BookingHomeStay.repository.BookingRepository;
import BookingHomeStay.BookingHomeStay.repository.ReviewRepository;
import BookingHomeStay.BookingHomeStay.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User user;
    private Homestay homestay;
    private Room room;
    private Booking booking;

    @BeforeEach
    void setUp() {
        user = User.builder().id(10L).fullName("Nguyễn Văn Khách").email("khach@gmail.com").build();
        Host host = Host.builder().id(1L).user(User.builder().id(99L).fullName("Chủ nhà").build()).build();
        homestay = Homestay.builder().id(20L).name("Homestay Đà Lạt Mộng Mơ").host(host).build();
        room = Room.builder().id(30L).homestay(homestay).name("Phòng Hoa Hồng").build();

        BookingDetail detail = BookingDetail.builder().room(room).build();
        Payment payment = Payment.builder().status(PaymentStatus.PAID).amount(BigDecimal.valueOf(1000000)).build();

        booking = Booking.builder()
                .id(100L)
                .user(user)
                .status(BookingStatus.COMPLETED)
                .payment(payment)
                .details(List.of(detail))
                .build();
    }

    @Test
    void testCreateReviewSuccessWhenCompletedAndPaid() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(100L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review r = invocation.getArgument(0);
            r.setId(500L);
            return r;
        });

        ReviewRequest req = ReviewRequest.builder()
                .bookingId(100L)
                .rating(5)
                .comment("Không gian rất đẹp và sạch sẽ!")
                .build();

        Review created = reviewService.createReview(10L, req);

        assertNotNull(created);
        assertEquals(5, created.getRating());
        assertEquals("Không gian rất đẹp và sạch sẽ!", created.getComment());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testCreateReviewFailsWhenUnpaid() {
        booking.getPayment().setStatus(PaymentStatus.UNPAID);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        ReviewRequest req = ReviewRequest.builder()
                .bookingId(100L)
                .rating(5)
                .comment("Đẹp")
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            reviewService.createReview(10L, req);
        });
        assertTrue(ex.getMessage().contains("hoàn tất thanh toán"));
    }

    @Test
    void testCreateReviewFailsWhenNotCheckedOutOrCompleted() {
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        ReviewRequest req = ReviewRequest.builder()
                .bookingId(100L)
                .rating(5)
                .comment("Đẹp")
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            reviewService.createReview(10L, req);
        });
        assertTrue(ex.getMessage().contains("hoàn tất kỳ nghỉ"));
    }

    @Test
    void testCreateReviewFailsWhenWrongUser() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        ReviewRequest req = ReviewRequest.builder()
                .bookingId(100L)
                .rating(5)
                .comment("Đẹp")
                .build();

        OwnershipException ex = assertThrows(OwnershipException.class, () -> {
            reviewService.createReview(999L, req); // different user
        });
        assertTrue(ex.getMessage().contains("không có quyền"));
    }

    @Test
    void testCreateReviewFailsWhenAlreadyReviewed() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(100L)).thenReturn(true);

        ReviewRequest req = ReviewRequest.builder()
                .bookingId(100L)
                .rating(5)
                .comment("Đẹp")
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            reviewService.createReview(10L, req);
        });
        assertTrue(ex.getMessage().contains("đã được đánh giá"));
    }

    @Test
    void testHostReplySuccess() {
        Review r = Review.builder().id(200L).homestay(homestay).rating(5).comment("Rất tốt").build();
        when(reviewRepository.findById(200L)).thenReturn(Optional.of(r));

        reviewService.replyReview(200L, 99L, "Cảm ơn quý khách đã ủng hộ!");

        assertEquals("Cảm ơn quý khách đã ủng hộ!", r.getHostReply());
        assertNotNull(r.getHostReplyAt());
        verify(reviewRepository, times(1)).save(r);
    }

    @Test
    void testHostReplyFailsWhenNotOwner() {
        Review r = Review.builder().id(200L).homestay(homestay).rating(5).comment("Rất tốt").build();
        when(reviewRepository.findById(200L)).thenReturn(Optional.of(r));

        OwnershipException ex = assertThrows(OwnershipException.class, () -> {
            reviewService.replyReview(200L, 888L, "Phản hồi"); // 888L is not owner (99L is)
        });
        assertTrue(ex.getMessage().contains("không phải là chủ nhà"));
    }
}
