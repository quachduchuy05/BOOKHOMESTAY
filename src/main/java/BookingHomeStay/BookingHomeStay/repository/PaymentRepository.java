package BookingHomeStay.BookingHomeStay.repository;

import BookingHomeStay.BookingHomeStay.entity.Payment;
import BookingHomeStay.BookingHomeStay.entity.PaymentMethod;
import BookingHomeStay.BookingHomeStay.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(Long bookingId);
    boolean existsBySepayTransactionId(Long sepayTransactionId);
    Optional<Payment> findBySepayTransactionId(Long sepayTransactionId);

    @Query("""
        SELECT p FROM Payment p
        WHERE (:status IS NULL OR p.status = :status)
          AND (:method IS NULL OR p.paymentMethod = :method)
          AND (:fromTime IS NULL OR (p.paymentTime IS NOT NULL AND p.paymentTime >= :fromTime))
          AND (:toTime IS NULL OR (p.paymentTime IS NOT NULL AND p.paymentTime <= :toTime))
          AND (:keyword IS NULL OR :keyword = '' 
               OR LOWER(p.transactionCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.booking.bookingCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.booking.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY p.id DESC
        """)
    List<Payment> filterPayments(
            @Param("status") PaymentStatus status,
            @Param("method") PaymentMethod method,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("keyword") String keyword
    );
}