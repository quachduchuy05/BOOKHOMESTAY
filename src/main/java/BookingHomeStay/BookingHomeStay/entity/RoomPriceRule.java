package BookingHomeStay.BookingHomeStay.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "room_price_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomPriceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoaiNgayGia loaiNgay;

    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal giaMoiDem;

    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;

    private String tenQuyTac;
}
