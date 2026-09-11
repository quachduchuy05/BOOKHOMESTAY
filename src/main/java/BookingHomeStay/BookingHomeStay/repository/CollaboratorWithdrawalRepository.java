package BookingHomeStay.BookingHomeStay.repository;

import BookingHomeStay.BookingHomeStay.entity.CollaboratorWithdrawal;
import BookingHomeStay.BookingHomeStay.entity.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollaboratorWithdrawalRepository extends JpaRepository<CollaboratorWithdrawal, Long> {

    List<CollaboratorWithdrawal> findByCollaboratorIdOrderByCreatedAtDesc(Long collaboratorId);

    List<CollaboratorWithdrawal> findByStatusOrderByCreatedAtAsc(WithdrawalStatus status);

    List<CollaboratorWithdrawal> findAllByOrderByCreatedAtDesc();
}
