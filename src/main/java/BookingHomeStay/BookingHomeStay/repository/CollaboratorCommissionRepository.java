package BookingHomeStay.BookingHomeStay.repository;

import BookingHomeStay.BookingHomeStay.entity.CollaboratorCommission;
import BookingHomeStay.BookingHomeStay.entity.CommissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CollaboratorCommissionRepository extends JpaRepository<CollaboratorCommission, Long> {

    List<CollaboratorCommission> findByCollaboratorIdOrderByCreatedAtDesc(Long collaboratorId);

    Optional<CollaboratorCommission> findByBookingId(Long bookingId);

    long countByCollaboratorId(Long collaboratorId);

    @Query("""
        SELECT COALESCE(SUM(c.commissionAmount), 0)
        FROM CollaboratorCommission c
        WHERE c.collaborator.id = :collaboratorId
        AND c.status = :status
    """)
    BigDecimal sumCommissionAmountByCollaboratorIdAndStatus(
            @Param("collaboratorId") Long collaboratorId,
            @Param("status") CommissionStatus status
    );
}
