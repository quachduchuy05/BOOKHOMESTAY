package BookingHomeStay.BookingHomeStay.controller;

import BookingHomeStay.BookingHomeStay.dto.BookingRequest;
import BookingHomeStay.BookingHomeStay.dto.ReviewRequest;
import BookingHomeStay.BookingHomeStay.entity.Booking;
import BookingHomeStay.BookingHomeStay.entity.Review;
import BookingHomeStay.BookingHomeStay.security.CustomUserDetails;
import BookingHomeStay.BookingHomeStay.service.BookingService;
import BookingHomeStay.BookingHomeStay.service.FavoriteService;
import BookingHomeStay.BookingHomeStay.service.ReviewService;
import BookingHomeStay.BookingHomeStay.service.VietQrService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Chuc nang danh cho Khach hang. Duong dan goc: /khach-hang (truoc day la /customer).
 */
@Controller
@RequestMapping("/khach-hang")
@RequiredArgsConstructor
public class CustomerController {

    private final BookingService bookingService;
    private final VietQrService vietQrService;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;
    private final BookingHomeStay.BookingHomeStay.repository.RoomRepository roomRepository;

    @GetMapping("/dat-phong/moi")
    public String newBookingForm(@RequestParam Long roomId, Model model) {
        BookingRequest form = new BookingRequest();
        form.setRoomId(roomId);
        form.setPaymentPolicy(BookingHomeStay.BookingHomeStay.entity.PaymentPolicy.PAY_AT_PROPERTY.name());
        form.setPaymentMethod(null);
        model.addAttribute("bookingRequest", form);
        roomRepository.findById(roomId).ifPresent(r -> model.addAttribute("room", r));
        return "khach-hang/dat-phong-form";
    }

    @PostMapping("/dat-phong/xac-nhan")
    public String confirm(@Valid @ModelAttribute BookingRequest bookingRequest, BindingResult result,
                           @AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        if (result.hasErrors()) return "khach-hang/dat-phong-form";
        try {
            Booking booking = bookingService.createBooking(bookingRequest, currentUser.getId());
            model.addAttribute("booking", booking);
            // Sinh san URL anh QR VietQR de khach quet bang app ngan hang, tu dong dien
            // so tai khoan + so tien + noi dung chuyen khoan (khong can nhap tay). Xem VietQrService.
            model.addAttribute("qrCodeUrl", vietQrService.buildQrUrl(booking));
            return "khach-hang/dat-phong-thanh-cong";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("bookingRequest", bookingRequest);
            return "khach-hang/dat-phong-form";
        }
    }

    @GetMapping("/don-dat-phong")
    public String myBookings(
            @RequestParam(value = "dateType", required = false, defaultValue = "CREATED") String dateType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @AuthenticationPrincipal CustomUserDetails currentUser, 
            Model model) {
        int pageSize = 5;
        int pageNumber = Math.max(1, page);
        int pageIndex = pageNumber - 1;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        Page<Booking> bookingPage = bookingService.getMyBookings(currentUser.getId(), dateType, startDate, endDate, pageable);
        int totalPages = Math.max(1, bookingPage.getTotalPages());

        if (bookingPage.getTotalPages() > 0 && pageIndex >= bookingPage.getTotalPages()) {
            pageNumber = totalPages;
            pageIndex = pageNumber - 1;
            pageable = PageRequest.of(pageIndex, pageSize);
            bookingPage = bookingService.getMyBookings(currentUser.getId(), dateType, startDate, endDate, pageable);
        }

        List<Booking> bookings = bookingPage.getContent();
        List<Long> bookingIds = bookings.stream().map(Booking::getId).toList();
        Map<Long, Review> reviewsMap = reviewService.getReviewsByBookingIds(bookingIds);

        long totalItems = bookingPage.getTotalElements();
        long startItem = totalItems == 0 ? 0 : (long) (pageNumber - 1) * pageSize + 1;
        long endItem = Math.min((long) pageNumber * pageSize, totalItems);

        model.addAttribute("bookings", bookings);
        model.addAttribute("bookingPage", bookingPage);
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("startItem", startItem);
        model.addAttribute("endItem", endItem);
        model.addAttribute("dateType", dateType);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("reviewsMap", reviewsMap);
        if (!model.containsAttribute("reviewRequest")) {
            model.addAttribute("reviewRequest", new ReviewRequest());
        }
        return "khach-hang/don-dat-phong";
    }

    @PostMapping("/danh-gia")
    public String submitReview(@Valid @ModelAttribute ReviewRequest reviewRequest,
                               BindingResult result,
                               @AuthenticationPrincipal CustomUserDetails currentUser,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/khach-hang/don-dat-phong";
        }
        try {
            reviewService.createReview(currentUser.getId(), reviewRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá homestay thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/khach-hang/don-dat-phong";
    }

    @PostMapping("/don-dat-phong/{id}/huy")
    public String cancel(@PathVariable Long id, 
                         @AuthenticationPrincipal CustomUserDetails currentUser,
                         RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Yêu cầu hủy đơn đã được ghi nhận thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/khach-hang/don-dat-phong";
    }

    @GetMapping("/don-dat-phong/{id}/thanh-toan")
    public String payBooking(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Booking booking = bookingService.getBookingForCustomer(id, currentUser.getId());
        if (booking.getStatus() != BookingHomeStay.BookingHomeStay.entity.BookingStatus.PENDING_PAYMENT) {
            return "redirect:/khach-hang/don-dat-phong";
        }
        model.addAttribute("booking", booking);
        model.addAttribute("qrCodeUrl", vietQrService.buildQrUrl(booking));
        return "khach-hang/dat-phong-thanh-cong";
    }

    // Danh sach homestay yeu thich cua khach hang
    @GetMapping("/yeu-thich")
    public String myFavorites(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        model.addAttribute("favorites", favoriteService.getFavoriteHomestays(currentUser.getId()));
        return "khach-hang/yeu-thich";
    }

    // AJAX Toggle yeu thich (them hoac xoa)
    @PostMapping("/yeu-thich/toggle")
    @ResponseBody
    public Map<String, Object> toggleFavorite(@RequestParam Long homestayId,
                                              @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) {
            return Map.of("success", false, "message", "Vui lòng đăng nhập để lưu homestay yêu thích.");
        }
        try {
            boolean favorited = favoriteService.toggleFavorite(currentUser.getId(), homestayId);
            return Map.of(
                    "success", true,
                    "favorited", favorited,
                    "message", favorited ? "Đã thêm vào danh sách yêu thích!" : "Đã bỏ lưu khỏi danh sách yêu thích!"
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}
