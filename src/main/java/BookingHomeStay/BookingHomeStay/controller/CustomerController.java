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

import java.util.List;
import java.util.Map;

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

    @GetMapping("/dat-phong/moi")
    public String newBookingForm(@RequestParam Long roomId, Model model) {
        BookingRequest form = new BookingRequest();
        form.setRoomId(roomId);
        model.addAttribute("bookingRequest", form);
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
    public String myBookings(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        List<Booking> bookings = bookingService.getMyBookings(currentUser.getId());
        List<Long> bookingIds = bookings.stream().map(Booking::getId).toList();
        Map<Long, Review> reviewsMap = reviewService.getReviewsByBookingIds(bookingIds);

        model.addAttribute("bookings", bookings);
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
    public String cancel(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        bookingService.cancelBooking(id, currentUser.getId());
        return "redirect:/khach-hang/don-dat-phong";
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
