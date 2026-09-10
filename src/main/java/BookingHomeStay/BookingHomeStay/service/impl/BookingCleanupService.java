package BookingHomeStay.BookingHomeStay.service.impl;

import BookingHomeStay.BookingHomeStay.entity.Booking;
import BookingHomeStay.BookingHomeStay.entity.BookingStatus;
import BookingHomeStay.BookingHomeStay.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingCleanupService {

    private final BookingRepository bookingRepository;

    /**
     * Chạy định kỳ mỗi phút một lần.
     * Tìm tất cả các Booking có trạng thái PENDING_PAYMENT mà quá 2 tiếng chưa thanh toán, 
     * tự động chuyển sang CANCELLED.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        // 1. Quét các đơn có expiredAt và đã quá hạn
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiredAtBefore(BookingStatus.PENDING_PAYMENT, now);

        // 2. Fallback cho đơn cũ chưa có expiredAt (tạo trước khi có cột này): quá 2 tiếng tính từ createdAt
        List<Booking> legacyExpired = bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING_PAYMENT, now.minusHours(2));
        for (Booking b : legacyExpired) {
            if (!expiredBookings.contains(b)) {
                expiredBookings.add(b);
            }
        }

        if (!expiredBookings.isEmpty()) {
            log.info("Tìm thấy {} đơn đặt phòng quá hạn thanh toán. Tiến hành hủy tự động...", expiredBookings.size());
            for (Booking booking : expiredBookings) {
                booking.setStatus(BookingStatus.CANCELLED);
                String oldNote = booking.getNote() != null ? booking.getNote() : "";
                booking.setNote(oldNote + "\n[SYSTEM] Tự động hủy đơn do quá hạn thanh toán giữ chỗ.");
            }
            bookingRepository.saveAll(expiredBookings);
            log.info("Đã hủy thành công {} đơn đặt phòng quá hạn.", expiredBookings.size());
        }
    }
}
