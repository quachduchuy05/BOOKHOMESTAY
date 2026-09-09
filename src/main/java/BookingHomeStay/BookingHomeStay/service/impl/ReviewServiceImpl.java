package BookingHomeStay.BookingHomeStay.service.impl;

import BookingHomeStay.BookingHomeStay.dto.ReviewRequest;
import BookingHomeStay.BookingHomeStay.entity.*;
import BookingHomeStay.BookingHomeStay.exception.OwnershipException;
import BookingHomeStay.BookingHomeStay.exception.ResourceNotFoundException;
import BookingHomeStay.BookingHomeStay.repository.BookingRepository;
import BookingHomeStay.BookingHomeStay.repository.ReviewRepository;
import BookingHomeStay.BookingHomeStay.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public Review createReview(Long userId, ReviewRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt phòng id=" + request.getBookingId()));

        if (!booking.getUser().getId().equals(userId)) {
            throw new OwnershipException("Bạn không có quyền đánh giá đơn đặt phòng này");
        }

        // Kiem tra trang thai don: phai da CHECKED_OUT hoac COMPLETED
        boolean isFinished = booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CHECKED_OUT;
        if (!isFinished) {
            throw new IllegalStateException("Bạn chỉ có thể đánh giá sau khi đã hoàn tất kỳ nghỉ (trả phòng).");
        }

        // Kiem tra trang thai thanh toan: phai da thanh toan (PAID)
        boolean isPaid = booking.getPayment() != null && booking.getPayment().getStatus() == PaymentStatus.PAID;
        if (!isPaid) {
            throw new IllegalStateException("Đơn đặt phòng cần được hoàn tất thanh toán trước khi gửi đánh giá.");
        }

        // Kiem tra xem da tung danh gia chua
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new IllegalStateException("Đơn đặt phòng này đã được đánh giá rồi.");
        }

        Homestay homestay = booking.getDetails().isEmpty() ? null :
                booking.getDetails().get(0).getRoom().getHomestay();

        if (homestay == null) {
            throw new IllegalStateException("Không tìm thấy thông tin homestay của đơn đặt phòng.");
        }

        Review review = Review.builder()
                .booking(booking)
                .user(booking.getUser())
                .homestay(homestay)
                .rating(request.getRating())
                .comment(request.getComment() != null ? request.getComment().trim() : "")
                .visible(true)
                .createdAt(LocalDateTime.now())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("[REVIEW] Khách hàng {} đã đánh giá {} sao cho homestay {}",
                booking.getUser().getEmail(), saved.getRating(), homestay.getName());
        return saved;
    }

    @Override
    public boolean canUserReview(Long userId, Long bookingId) {
        Optional<Booking> opt = bookingRepository.findById(bookingId);
        if (opt.isEmpty()) return false;
        Booking b = opt.get();
        if (!b.getUser().getId().equals(userId)) return false;

        boolean isFinished = b.getStatus() == BookingStatus.COMPLETED || b.getStatus() == BookingStatus.CHECKED_OUT;
        boolean isPaid = b.getPayment() != null && b.getPayment().getStatus() == PaymentStatus.PAID;

        return isFinished && isPaid && !reviewRepository.existsByBookingId(bookingId);
    }

    @Override
    public List<Review> getReviewsOfHomestay(Long homestayId) {
        return reviewRepository.findByHomestayIdAndVisibleTrueOrderByCreatedAtDesc(homestayId);
    }

    @Override
    public Double getAverageRating(Long homestayId) {
        Double avg = reviewRepository.getAverageRatingByHomestayId(homestayId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : null;
    }

    @Override
    public long getReviewCount(Long homestayId) {
        return reviewRepository.countByHomestayIdAndVisibleTrue(homestayId);
    }

    @Override
    public Map<Long, Review> getReviewsByBookingIds(List<Long> bookingIds) {
        if (bookingIds == null || bookingIds.isEmpty()) return Collections.emptyMap();
        List<Review> reviews = reviewRepository.findByBookingIdIn(bookingIds);
        return reviews.stream()
                .collect(Collectors.toMap(r -> r.getBooking().getId(), r -> r, (r1, r2) -> r1));
    }

    @Override
    public Optional<Review> getReviewByBookingId(Long bookingId) {
        return reviewRepository.findByBookingId(bookingId);
    }

    @Override
    @Transactional
    public void replyReview(Long reviewId, Long hostUserId, String replyContent) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá id=" + reviewId));

        Long ownerHostId = review.getHomestay().getHost().getUser().getId();
        if (!ownerHostId.equals(hostUserId)) {
            throw new OwnershipException("Bạn không phải là chủ nhà của homestay này để phản hồi đánh giá");
        }

        review.setHostReply(replyContent != null ? replyContent.trim() : "");
        review.setHostReplyAt(LocalDateTime.now());
        reviewRepository.save(review);
    }

    @Override
    public List<Review> getReviewsOfHost(Long hostUserId) {
        return reviewRepository.findByHomestay_Host_User_IdOrderByCreatedAtDesc(hostUserId);
    }
}
