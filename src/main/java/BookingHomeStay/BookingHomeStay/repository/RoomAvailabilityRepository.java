package BookingHomeStay.BookingHomeStay.repository;

import BookingHomeStay.BookingHomeStay.entity.RoomAvailability;
import BookingHomeStay.BookingHomeStay.entity.RoomAvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {

    Optional<RoomAvailability> findByRoomIdAndDate(Long roomId, LocalDate date);

    List<RoomAvailability> findByRoomIdAndDateBetween(Long roomId, LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT ra FROM RoomAvailability ra
        WHERE ra.room.id = :roomId
          AND ra.date >= :checkIn
          AND ra.date < :checkOut
          AND ra.status IN (:statuses)
        """)
    List<RoomAvailability> findByRoomIdAndDateBetweenAndStatusIn(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("statuses") List<RoomAvailabilityStatus> statuses
    );
}
