package BookingHomeStay.BookingHomeStay.repository;

import BookingHomeStay.BookingHomeStay.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByHomestayIdAndVisibleTrueOrderByCreatedAtDesc(Long homestayId);
    List<Review> findAllByOrderByCreatedAtDesc();
    Optional<Review> findByBookingId(Long bookingId);
    boolean existsByBookingId(Long bookingId);
    List<Review> findByBookingIdIn(Collection<Long> bookingIds);
    long countByHomestayIdAndVisibleTrue(Long homestayId);

    List<Review> findByHomestay_Host_User_IdOrderByCreatedAtDesc(Long hostUserId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.homestay.id = :homestayId AND r.visible = true")
    Double getAverageRatingByHomestayId(@Param("homestayId") Long homestayId);
}