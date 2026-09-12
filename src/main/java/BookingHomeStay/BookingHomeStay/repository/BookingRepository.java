package BookingHomeStay.BookingHomeStay.repository;

import BookingHomeStay.BookingHomeStay.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    java.util.Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByDetails_Room_Homestay_Host_IdOrderByCreatedAtDesc(Long hostId);

    @Query("SELECT DISTINCT b FROM Booking b JOIN b.details bd JOIN bd.room r JOIN r.homestay h JOIN h.host host WHERE host.user.id = :hostUserId ORDER BY b.createdAt DESC")
    List<Booking> findByDetails_Room_Homestay_Host_User_IdOrderByCreatedAtDesc(@org.springframework.data.repository.query.Param("hostUserId") Long hostUserId);
    long countByStatus(BookingHomeStay.BookingHomeStay.entity.BookingStatus status);
    boolean existsByPromotionId(Long promotionId);
    long countBySource(BookingHomeStay.BookingHomeStay.entity.BookingSource source);
    
    List<Booking> findByStatusAndCreatedAtBefore(BookingHomeStay.BookingHomeStay.entity.BookingStatus status, java.time.LocalDateTime dateTime);
    List<Booking> findByStatusAndExpiredAtBefore(BookingHomeStay.BookingHomeStay.entity.BookingStatus status, java.time.LocalDateTime dateTime);

    @Query("""
        SELECT DISTINCT b FROM Booking b
        WHERE (:status IS NULL OR b.status = :status)
          AND (:bookingCode IS NULL OR LOWER(b.bookingCode) LIKE LOWER(CONCAT('%', :bookingCode, '%')))
          AND (:customerName IS NULL OR LOWER(b.customerName) LIKE LOWER(CONCAT('%', :customerName, '%')))
        ORDER BY b.createdAt DESC
        """)
    List<Booking> searchForAdmin(@org.springframework.data.repository.query.Param("status") BookingHomeStay.BookingHomeStay.entity.BookingStatus status,
                                @org.springframework.data.repository.query.Param("bookingCode") String bookingCode,
                                @org.springframework.data.repository.query.Param("customerName") String customerName);

    @Query("""
        SELECT DISTINCT b FROM Booking b JOIN b.details bd JOIN bd.room r JOIN r.homestay h JOIN h.host host
        WHERE (host.user.id = :hostUserId OR host.id = :hostUserId)
          AND (:status IS NULL OR b.status = :status)
          AND (:bookingCode IS NULL OR LOWER(b.bookingCode) LIKE LOWER(CONCAT('%', :bookingCode, '%')))
          AND (:customerName IS NULL OR LOWER(b.customerName) LIKE LOWER(CONCAT('%', :customerName, '%')))
        ORDER BY b.createdAt DESC
        """)
    List<Booking> searchForHost(@org.springframework.data.repository.query.Param("hostUserId") Long hostUserId,
                               @org.springframework.data.repository.query.Param("status") BookingHomeStay.BookingHomeStay.entity.BookingStatus status,
                               @org.springframework.data.repository.query.Param("bookingCode") String bookingCode,
                               @org.springframework.data.repository.query.Param("customerName") String customerName);

    // Gom so luong don dat phong theo thang (dung cho bieu do o Dashboard quan tri).
    // Tra ve moi hang la [ "yyyy-MM", so_luong ] de ve Chart.js.
    @Query("SELECT FUNCTION('DATE_FORMAT', b.createdAt, '%Y-%m'), COUNT(b) " +
           "FROM Booking b GROUP BY FUNCTION('DATE_FORMAT', b.createdAt, '%Y-%m') ORDER BY 1")
    List<Object[]> countBookingsGroupedByMonth();

    @Query(value = "SELECT DISTINCT b FROM Booking b LEFT JOIN b.details bd " +
           "WHERE b.user.id = :userId " +
           "AND ((:dateType = 'CHECKIN' AND (:startDate IS NULL OR bd.checkinDate >= :startDate) AND (:endDate IS NULL OR bd.checkinDate <= :endDate)) " +
           "     OR (:dateType != 'CHECKIN' AND (:fromDateTime IS NULL OR b.createdAt >= :fromDateTime) AND (:toDateTime IS NULL OR b.createdAt <= :toDateTime))) " +
           "ORDER BY b.createdAt DESC",
           countQuery = "SELECT COUNT(DISTINCT b) FROM Booking b LEFT JOIN b.details bd " +
           "WHERE b.user.id = :userId " +
           "AND ((:dateType = 'CHECKIN' AND (:startDate IS NULL OR bd.checkinDate >= :startDate) AND (:endDate IS NULL OR bd.checkinDate <= :endDate)) " +
           "     OR (:dateType != 'CHECKIN' AND (:fromDateTime IS NULL OR b.createdAt >= :fromDateTime) AND (:toDateTime IS NULL OR b.createdAt <= :toDateTime)))")
    org.springframework.data.domain.Page<Booking> findByUserIdWithFilter(
            @org.springframework.data.repository.query.Param("userId") Long userId,
            @org.springframework.data.repository.query.Param("dateType") String dateType,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate,
            @org.springframework.data.repository.query.Param("fromDateTime") java.time.LocalDateTime fromDateTime,
            @org.springframework.data.repository.query.Param("toDateTime") java.time.LocalDateTime toDateTime,
            org.springframework.data.domain.Pageable pageable);
}
