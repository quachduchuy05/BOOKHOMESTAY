package BookingHomeStay.BookingHomeStay.repository;

import BookingHomeStay.BookingHomeStay.entity.CollaboratorReferralClick;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollaboratorReferralClickRepository extends JpaRepository<CollaboratorReferralClick, Long> {

    long countByCollaboratorId(Long collaboratorId);

    long countByCollaboratorIdAndHomestaySlug(Long collaboratorId, String homestaySlug);
}
