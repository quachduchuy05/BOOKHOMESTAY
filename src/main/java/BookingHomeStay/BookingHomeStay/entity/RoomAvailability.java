package BookingHomeStay.BookingHomeStay.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "room_availability",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_room_date", columnNames = {"room_id", "date"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RoomAvailabilityStatus status = RoomAvailabilityStatus.AVAILABLE;
}
