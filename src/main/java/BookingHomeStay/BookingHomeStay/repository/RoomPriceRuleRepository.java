package BookingHomeStay.BookingHomeStay.repository;

import BookingHomeStay.BookingHomeStay.entity.LoaiNgayGia;
import BookingHomeStay.BookingHomeStay.entity.RoomPriceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomPriceRuleRepository extends JpaRepository<RoomPriceRule, Long> {
    List<RoomPriceRule> findByRoomId(Long roomId);
    List<RoomPriceRule> findByRoomIdOrderByNgayBatDauAsc(Long roomId);
    Optional<RoomPriceRule> findByRoomIdAndLoaiNgay(Long roomId, LoaiNgayGia loaiNgay);
}
