package BookingHomeStay.BookingHomeStay.service;

import BookingHomeStay.BookingHomeStay.dto.BookingRequest;
import BookingHomeStay.BookingHomeStay.entity.Booking;
import java.util.List;

public interface BookingService {
    boolean isRoomAvailable(Long roomId, java.time.LocalDate checkIn, java.time.LocalDate checkOut);
    Booking createBooking(BookingRequest request, Long userId);
    List<Booking> getMyBookings(Long userId);
    org.springframework.data.domain.Page<Booking> getMyBookings(
            Long userId,
            String dateType,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            org.springframework.data.domain.Pageable pageable);
    void cancelBooking(Long bookingId, Long userId);
    List<Booking> getBookingsOfHost(Long hostUserId);
    void confirmBooking(Long bookingId, Long hostUserId);
    void rejectBooking(Long bookingId, Long hostUserId);
    void checkIn(Long bookingId, Long hostUserId);
    void checkOut(Long bookingId, Long hostUserId);
    void markPaymentPaid(Long bookingId, Long hostUserId);
    void markPaymentPaidBySystem(Long bookingId, String transactionCode, Long sepayTransactionId, String gateway);
    void completeBooking(Long bookingId, Long hostUserId);
    List<Booking> getAllBookings();
    List<Booking> searchBookingsForAdmin(String bookingCode, String customerName);
    List<Booking> searchBookingsForAdmin(BookingHomeStay.BookingHomeStay.entity.BookingStatus status, String bookingCode, String customerName);
    List<Booking> searchBookingsOfHost(Long hostUserId, String bookingCode, String customerName);
    List<Booking> searchBookingsOfHost(Long hostUserId, BookingHomeStay.BookingHomeStay.entity.BookingStatus status, String bookingCode, String customerName);
    Booking getBookingForCustomer(Long bookingId, Long customerUserId);
    void refundBooking(Long bookingId, Long hostUserId);
    List<java.time.LocalDate> getBookedDates(Long roomId);
}