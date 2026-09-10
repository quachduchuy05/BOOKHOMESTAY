package BookingHomeStay.BookingHomeStay.service;

import BookingHomeStay.BookingHomeStay.dto.DayAvailabilityDto;
import BookingHomeStay.BookingHomeStay.entity.*;
import BookingHomeStay.BookingHomeStay.repository.BookingDetailRepository;
import BookingHomeStay.BookingHomeStay.repository.RoomAvailabilityRepository;
import BookingHomeStay.BookingHomeStay.repository.RoomRepository;
import BookingHomeStay.BookingHomeStay.service.impl.RoomAvailabilityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomAvailabilityServiceTest {

    @Mock
    private RoomAvailabilityRepository roomAvailabilityRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingDetailRepository bookingDetailRepository;

    @InjectMocks
    private RoomAvailabilityServiceImpl roomAvailabilityService;

    private User hostUser;
    private Host host;
    private Homestay homestay;
    private Room room;

    @BeforeEach
    void setUp() {
        hostUser = User.builder().id(10L).fullName("Chủ nhà").build();
        host = Host.builder().id(1L).user(hostUser).build();
        homestay = Homestay.builder().id(100L).host(host).name("Homestay View Biển").build();
        room = Room.builder()
                .id(200L)
                .homestay(homestay)
                .name("Phòng Vip")
                .pricePerNight(new BigDecimal("500000"))
                .status(RoomStatus.ACTIVE)
                .build();
    }

    @Test
    void testGetMonthCalendar() {
        when(roomRepository.findById(200L)).thenReturn(Optional.of(room));
        when(roomAvailabilityRepository.findByRoomIdAndDateBetween(eq(200L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(bookingDetailRepository.findOverlapping(eq(200L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        List<DayAvailabilityDto> days = roomAvailabilityService.getMonthCalendar(200L, 2026, 9);

        assertNotNull(days);
        assertFalse(days.isEmpty());
        // Verify month has 30 days plus paddings
        long currentMonthDays = days.stream().filter(DayAvailabilityDto::isCurrentMonth).count();
        assertEquals(30, currentMonthDays);
    }

    @Test
    void testLockDateSuccess() {
        when(roomRepository.findById(200L)).thenReturn(Optional.of(room));
        LocalDate targetDate = LocalDate.of(2026, 9, 20);
        when(roomAvailabilityRepository.findByRoomIdAndDate(200L, targetDate)).thenReturn(Optional.empty());

        roomAvailabilityService.lockDate(200L, targetDate, 10L);

        verify(roomAvailabilityRepository, times(1)).save(argThat(ra -> 
                ra.getDate().equals(targetDate) && ra.getStatus() == RoomAvailabilityStatus.LOCKED
        ));
    }

    @Test
    void testToggleDateFromAvailableToLocked() {
        when(roomRepository.findById(200L)).thenReturn(Optional.of(room));
        LocalDate targetDate = LocalDate.of(2026, 9, 22);
        when(roomAvailabilityRepository.findByRoomIdAndDate(200L, targetDate)).thenReturn(Optional.empty());

        RoomAvailabilityStatus status = roomAvailabilityService.toggleDate(200L, targetDate, 10L);

        assertEquals(RoomAvailabilityStatus.LOCKED, status);
        verify(roomAvailabilityRepository, times(1)).save(any(RoomAvailability.class));
    }

    @Test
    void testIsRoomAvailableForDates() {
        LocalDate checkIn = LocalDate.of(2026, 9, 15);
        LocalDate checkOut = LocalDate.of(2026, 9, 18);

        when(roomAvailabilityRepository.findByRoomIdAndDateBetweenAndStatusIn(
                eq(200L), eq(checkIn), eq(checkOut), anyList()
        )).thenReturn(Collections.emptyList());

        boolean available = roomAvailabilityService.isRoomAvailableForDates(200L, checkIn, checkOut);
        assertTrue(available);
    }
}
