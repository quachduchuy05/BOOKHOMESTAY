package BookingHomeStay.BookingHomeStay.repository;

import BookingHomeStay.BookingHomeStay.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserIdAndHomestayId(Long userId, Long homestayId);
    Optional<Favorite> findByUserIdAndHomestayId(Long userId, Long homestayId);
    List<Favorite> findByUserIdOrderByIdDesc(Long userId);
    void deleteByUserIdAndHomestayId(Long userId, Long homestayId);
}