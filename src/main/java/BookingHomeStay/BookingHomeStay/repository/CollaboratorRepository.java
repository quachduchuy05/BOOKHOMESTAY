package BookingHomeStay.BookingHomeStay.repository;

import BookingHomeStay.BookingHomeStay.entity.Collaborator;
import BookingHomeStay.BookingHomeStay.entity.CollaboratorStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollaboratorRepository extends JpaRepository<Collaborator, Long> {

    Optional<Collaborator> findByUserId(Long userId);

    Optional<Collaborator> findByMaGioiThieuAndStatus(String maGioiThieu, CollaboratorStatus status);

    List<Collaborator> findByStatus(CollaboratorStatus status);

    boolean existsByMaGioiThieu(String maGioiThieu);
}
