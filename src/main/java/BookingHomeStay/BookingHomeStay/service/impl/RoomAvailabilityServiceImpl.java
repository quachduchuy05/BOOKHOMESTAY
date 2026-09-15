package BookingHomeStay.BookingHomeStay.service.impl;

import BookingHomeStay.BookingHomeStay.dto.DayAvailabilityDto;
import BookingHomeStay.BookingHomeStay.entity.BookingDetail;
import BookingHomeStay.BookingHomeStay.entity.Room;
import BookingHomeStay.BookingHomeStay.entity.RoomAvailability;
import BookingHomeStay.BookingHomeStay.entity.RoomAvailabilityStatus;
import BookingHomeStay.BookingHomeStay.exception.OwnershipException;
import BookingHomeStay.BookingHomeStay.exception.ResourceNotFoundException;
import BookingHomeStay.BookingHomeStay.repository.BookingDetailRepository;
import BookingHomeStay.BookingHomeStay.repository.RoomAvailabilityRepository;
import BookingHomeStay.BookingHomeStay.repository.RoomRepository;
import BookingHomeStay.BookingHomeStay.service.RoomAvailabilityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomAvailabilityServiceImpl implements RoomAvailabilityService {

    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final RoomRepository roomRepository;
    private final BookingDetailRepository bookingDetailRepository;

    private Room getRoomAndVerifyHost(Long roomId, Long hostUserId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + roomId));
        if (hostUserId != null) {
            Long ownerId = room.getHomestay().getHost().getUser().getId();
            if (!ownerId.equals(hostUserId)) {
                throw new OwnershipException("Bạn không có quyền quản lý phòng này.");
            }
        }
        return room;
    }

    @Override
    public List<DayAvailabilityDto> getMonthCalendar(Long roomId, int year, int month) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + roomId));

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        LocalDate lastOfMonth = yearMonth.atEndOfMonth();

        // Lấy tất cả bản ghi availability trong tháng
        List<RoomAvailability> availabilities = roomAvailabilityRepository.findByRoomIdAndDateBetween(
                roomId, firstOfMonth, lastOfMonth);
        Map<LocalDate, RoomAvailabilityStatus> statusMap = new HashMap<>();
        for (RoomAvailability ra : availabilities) {
            statusMap.put(ra.getDate(), ra.getStatus());
        }

        // Lấy các booking trong tháng để đánh dấu BOOKED nếu chưa có trong availability
        List<BookingDetail> bookingsInMonth = bookingDetailRepository.findOverlapping(
                roomId, firstOfMonth, lastOfMonth.plusDays(1));
        for (BookingDetail bd : bookingsInMonth) {
            LocalDate curr = bd.getCheckinDate();
            while (curr.isBefore(bd.getCheckoutDate())) {
                if (!curr.isBefore(firstOfMonth) && !curr.isAfter(lastOfMonth)) {
                    // Nếu ngày đó chưa bị LOCKED bởi host thì là BOOKED
                    if (statusMap.get(curr) != RoomAvailabilityStatus.LOCKED) {
                        statusMap.put(curr, RoomAvailabilityStatus.BOOKED);
                    }
                }
                curr = curr.plusDays(1);
            }
        }

        LocalDate today = LocalDate.now();
        List<DayAvailabilityDto> days = new ArrayList<>();

        // Thêm các ngày đầu tuần trước mùng 1 (padding ngày của tháng trước)
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 = Mon ... 7 = Sun
        for (int i = 1; i < firstDayOfWeek; i++) {
            LocalDate padDate = firstOfMonth.minusDays(firstDayOfWeek - i);
            days.add(DayAvailabilityDto.builder()
                    .date(padDate)
                    .dayOfMonth(padDate.getDayOfMonth())
                    .dayOfWeek(padDate.getDayOfWeek().getValue())
                    .status(RoomAvailabilityStatus.AVAILABLE)
                    .past(padDate.isBefore(today))
                    .today(padDate.isEqual(today))
                    .currentMonth(false)
                    .build());
        }

        // Các ngày trong tháng
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            RoomAvailabilityStatus status = statusMap.getOrDefault(date, RoomAvailabilityStatus.AVAILABLE);
            days.add(DayAvailabilityDto.builder()
                    .date(date)
                    .dayOfMonth(day)
                    .dayOfWeek(date.getDayOfWeek().getValue())
                    .status(status)
                    .past(date.isBefore(today))
                    .today(date.isEqual(today))
                    .currentMonth(true)
                    .build());
        }

        // Thêm các ngày cuối tuần sau ngày cuối tháng (padding ngày của tháng sau để đủ tuần)
        int lastDayOfWeek = lastOfMonth.getDayOfWeek().getValue();
        for (int i = 1; i <= (7 - lastDayOfWeek); i++) {
            LocalDate padDate = lastOfMonth.plusDays(i);
            days.add(DayAvailabilityDto.builder()
                    .date(padDate)
                    .dayOfMonth(padDate.getDayOfMonth())
                    .dayOfWeek(padDate.getDayOfWeek().getValue())
                    .status(RoomAvailabilityStatus.AVAILABLE)
                    .past(padDate.isBefore(today))
                    .today(padDate.isEqual(today))
                    .currentMonth(false)
                    .build());
        }

        return days;
    }

    @Override
    @Transactional
    public void lockDate(Long roomId, LocalDate date, Long hostUserId) {
        Room room = getRoomAndVerifyHost(roomId, hostUserId);
        RoomAvailability ra = roomAvailabilityRepository.findByRoomIdAndDate(roomId, date)
                .orElse(RoomAvailability.builder().room(room).date(date).build());
        ra.setStatus(RoomAvailabilityStatus.LOCKED);
        roomAvailabilityRepository.save(ra);
        log.info("[CALENDAR] Host id={} đã khóa ngày {} cho phòng id={}", hostUserId, date, roomId);
    }

    @Override
    @Transactional
    public void unlockDate(Long roomId, LocalDate date, Long hostUserId) {
        getRoomAndVerifyHost(roomId, hostUserId);
        roomAvailabilityRepository.findByRoomIdAndDate(roomId, date).ifPresent(ra -> {
            ra.setStatus(RoomAvailabilityStatus.AVAILABLE);
            roomAvailabilityRepository.save(ra);
        });
        log.info("[CALENDAR] Host id={} đã mở lại ngày {} cho phòng id={}", hostUserId, date, roomId);
    }

    @Override
    @Transactional
    public RoomAvailabilityStatus toggleDate(Long roomId, LocalDate date, Long hostUserId) {
        Room room = getRoomAndVerifyHost(roomId, hostUserId);
        RoomAvailability ra = roomAvailabilityRepository.findByRoomIdAndDate(roomId, date)
                .orElse(RoomAvailability.builder().room(room).date(date).status(RoomAvailabilityStatus.AVAILABLE).build());

        if (ra.getStatus() == RoomAvailabilityStatus.BOOKED) {
            throw new IllegalStateException("Ngày " + date + " đã có khách đặt, không thể khóa/mở thủ công.");
        }

        RoomAvailabilityStatus nextStatus = (ra.getStatus() == RoomAvailabilityStatus.LOCKED)
                ? RoomAvailabilityStatus.AVAILABLE
                : RoomAvailabilityStatus.LOCKED;

        ra.setStatus(nextStatus);
        roomAvailabilityRepository.save(ra);
        return nextStatus;
    }

    @Override
    @Transactional
    public void lockRange(Long roomId, LocalDate start, LocalDate end, Long hostUserId) {
        Room room = getRoomAndVerifyHost(roomId, hostUserId);
        LocalDate curr = start;
        while (!curr.isAfter(end)) {
            RoomAvailability ra = roomAvailabilityRepository.findByRoomIdAndDate(roomId, curr)
                    .orElse(RoomAvailability.builder().room(room).date(curr).build());
            if (ra.getStatus() != RoomAvailabilityStatus.BOOKED) {
                ra.setStatus(RoomAvailabilityStatus.LOCKED);
                roomAvailabilityRepository.save(ra);
            }
            curr = curr.plusDays(1);
        }
    }

    @Override
    @Transactional
    public void unlockRange(Long roomId, LocalDate start, LocalDate end, Long hostUserId) {
        getRoomAndVerifyHost(roomId, hostUserId);
        LocalDate curr = start;
        while (!curr.isAfter(end)) {
            final LocalDate target = curr;
            roomAvailabilityRepository.findByRoomIdAndDate(roomId, target).ifPresent(ra -> {
                if (ra.getStatus() == RoomAvailabilityStatus.LOCKED) {
                    ra.setStatus(RoomAvailabilityStatus.AVAILABLE);
                    roomAvailabilityRepository.save(ra);
                }
            });
            curr = curr.plusDays(1);
        }
    }

    @Override
    public boolean isRoomAvailableForDates(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return false;
        }
        LocalDate lastNight = checkOut.minusDays(1);
        List<RoomAvailability> blocked = roomAvailabilityRepository.findByRoomIdAndDateBetweenAndStatusIn(
                roomId, checkIn, lastNight,
                List.of(RoomAvailabilityStatus.BOOKED, RoomAvailabilityStatus.LOCKED)
        );
        return blocked.isEmpty();
    }

    @Override
    @Transactional
    public void recordBookingDays(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng: " + roomId));
        LocalDate curr = checkIn;
        while (curr.isBefore(checkOut)) {
            RoomAvailability ra = roomAvailabilityRepository.findByRoomIdAndDate(roomId, curr)
                    .orElse(RoomAvailability.builder().room(room).date(curr).build());
            ra.setStatus(RoomAvailabilityStatus.BOOKED);
            roomAvailabilityRepository.save(ra);
            curr = curr.plusDays(1);
        }
    }

    @Override
    @Transactional
    public void releaseBookingDays(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        if (roomId == null || checkIn == null || checkOut == null) return;
        LocalDate curr = checkIn;
        while (curr.isBefore(checkOut)) {
            final LocalDate target = curr;
            roomAvailabilityRepository.findByRoomIdAndDate(roomId, target).ifPresent(ra -> {
                if (ra.getStatus() == RoomAvailabilityStatus.BOOKED) {
                    ra.setStatus(RoomAvailabilityStatus.AVAILABLE);
                    roomAvailabilityRepository.save(ra);
                }
            });
            curr = curr.plusDays(1);
        }
    }
}
