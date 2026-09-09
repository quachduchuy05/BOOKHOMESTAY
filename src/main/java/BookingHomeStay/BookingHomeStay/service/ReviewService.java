package BookingHomeStay.BookingHomeStay.service;

import BookingHomeStay.BookingHomeStay.dto.ReviewRequest;
import BookingHomeStay.BookingHomeStay.entity.Review;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReviewService {
    Review createReview(Long userId, ReviewRequest request);

    boolean canUserReview(Long userId, Long bookingId);

    List<Review> getReviewsOfHomestay(Long homestayId);

    Double getAverageRating(Long homestayId);

    long getReviewCount(Long homestayId);

    Map<Long, Review> getReviewsByBookingIds(List<Long> bookingIds);

    Optional<Review> getReviewByBookingId(Long bookingId);

    List<Review> getReviewsOfHost(Long hostUserId);

    void replyReview(Long reviewId, Long hostUserId, String replyContent);
}
