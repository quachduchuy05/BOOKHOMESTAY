package BookingHomeStay.BookingHomeStay.controller;

import BookingHomeStay.BookingHomeStay.dto.HomestayForm;
import BookingHomeStay.BookingHomeStay.dto.RoomForm;
import BookingHomeStay.BookingHomeStay.entity.Room;
import BookingHomeStay.BookingHomeStay.security.CustomUserDetails;
import BookingHomeStay.BookingHomeStay.service.BookingService;
import BookingHomeStay.BookingHomeStay.service.HomestayService;
import BookingHomeStay.BookingHomeStay.service.ReviewService;
import BookingHomeStay.BookingHomeStay.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Chuc nang danh cho Chu nha cho thue (HOST) va Cong tac vien (COLLABORATOR).
 * Duong dan goc: /chu-nha  (truoc day la /host).
 */
@Controller
@RequestMapping("/chu-nha")
@RequiredArgsConstructor
public class HostController {

    private final HomestayService homestayService;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final ReviewService reviewService;

    // Trang tong quan cua Chu nha: so luong homestay dang co + tat ca don dat
    // phong cua cac homestay do (KHONG dung chung du lieu voi Dashboard Admin).
    @GetMapping("/tong-quan")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        model.addAttribute("homestays", homestayService.getMyHomestays(currentUser.getId()));
        model.addAttribute("bookings", bookingService.getBookingsOfHost(currentUser.getId()));
        return "chu-nha/tong-quan";
    }

    // Danh sach homestay CUA RIENG Chu nha dang dang nhap (loc theo hostUserId).
    @GetMapping("/danh-sach-homestay")
    public String homestays(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        model.addAttribute("homestays", homestayService.getMyHomestays(currentUser.getId()));
        return "chu-nha/danh-sach-homestay";
    }

    // Mo form trong de tao 1 homestay moi.
    @GetMapping("/homestay/them-moi")
    public String newForm(Model model) {
        model.addAttribute("homestayForm", new HomestayForm());
        return "chu-nha/homestay-form";
    }

    // Luu homestay: form khong co id -> TAO MOI, co id -> CAP NHAT. Homestay
    // moi tao mac dinh o trang thai PENDING, can Admin duyet moi hien cong khai.
    @PostMapping("/homestay/luu")
    public String save(@Valid @ModelAttribute HomestayForm form, BindingResult result,
                        @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (result.hasErrors()) return "chu-nha/homestay-form";
        if (form.getId() == null) homestayService.createHomestay(form, currentUser.getId());
        else homestayService.updateHomestay(form, currentUser.getId());
        return "redirect:/chu-nha/danh-sach-homestay";
    }

    // GHI CHU: route nay bi THIEU trong ban goc (nut "+ Them phong" tro toi 1 URL
    // khong ton tai). Da bo sung de tinh nang them phong hoat dong day du (task 17).
    // Mo form SUA 1 phong da co san (dien san du lieu cu). Co kiem tra quyen so
    // huu qua roomService.getByIdForHost() - Chu nha CHI sua duoc phong cua minh.
    @GetMapping("/homestay/{homestayId}/phong/{roomId}/sua")
    public String editRoomForm(@PathVariable Long homestayId, @PathVariable Long roomId,
                                @AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Room room = roomService.getByIdForHost(roomId, currentUser.getId());
        RoomForm form = new RoomForm();
        form.setId(room.getId());
        form.setName(room.getName());
        form.setPricePerNight(room.getPricePerNight());
        form.setMaxGuests(room.getMaxGuests());
        form.setDescription(room.getDescription());
        model.addAttribute("homestay", homestayService.getByIdForHost(homestayId, currentUser.getId()));
        model.addAttribute("homestayId", homestayId);
        model.addAttribute("roomForm", form);
        return "chu-nha/phong-form";
    }

    // Mo form TRONG de them 1 phong moi vao homestay chi dinh.
    @GetMapping("/homestay/{homestayId}/phong/them-moi")
    public String newRoomForm(@PathVariable Long homestayId, @AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        model.addAttribute("homestay", homestayService.getByIdForHost(homestayId, currentUser.getId()));
        model.addAttribute("homestayId", homestayId);
        model.addAttribute("roomForm", new RoomForm());
        return "chu-nha/phong-form";
    }

    // Danh sach toan bo phong cua 1 homestay (dung chung cho ca xem va lam
    // trang trung gian truoc khi bam "+ Them phong" / "Sua gia").
    @GetMapping("/homestay/{homestayId}/phong")
    public String rooms(@PathVariable Long homestayId, @AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        model.addAttribute("homestay", homestayService.getByIdForHost(homestayId, currentUser.getId()));
        model.addAttribute("rooms", roomService.getRoomsOfHomestay(homestayId));
        return "chu-nha/danh-sach-phong";
    }

    // Luu phong: form khong co id -> TAO MOI phong trong homestay nay, co id ->
    // CAP NHAT (bao gom ca SUA GIA - dung chung route cho ca tao va sua).
    @PostMapping("/homestay/{homestayId}/phong/luu")
    public String saveRoom(@PathVariable Long homestayId, @Valid @ModelAttribute RoomForm form,
                            BindingResult result, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (result.hasErrors()) return "chu-nha/phong-form";
        if (form.getId() == null) roomService.create(homestayId, form, currentUser.getId());
        else roomService.update(form, currentUser.getId());
        return "redirect:/chu-nha/homestay/" + homestayId + "/phong";
    }

    // Dao trang thai Con phong <-> Het phong cho 1 phong CUA CHINH MINH (task 17).
    @PostMapping("/homestay/{homestayId}/phong/{roomId}/doi-trang-thai")
    public String toggleRoomStatus(@PathVariable Long homestayId, @PathVariable Long roomId,
                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        roomService.toggleActive(roomId, currentUser.getId());
        return "redirect:/chu-nha/homestay/" + homestayId + "/phong";
    }

    // Danh sach TOAN BO don dat phong thuoc cac homestay cua Chu nha nay.
    @GetMapping("/don-dat-phong")
    public String bookings(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        model.addAttribute("bookings", bookingService.getBookingsOfHost(currentUser.getId()));
        return "chu-nha/don-dat-phong";
    }

    // Xac nhan 1 don dat phong (chuyen sang trang thai da xac nhan, thong bao
    // cho khach biet don da duoc chap nhan).
    @PostMapping("/don-dat-phong/{id}/xac-nhan")
    public String confirm(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        bookingService.confirmBooking(id, currentUser.getId());
        return "redirect:/chu-nha/don-dat-phong";
    }

    // Tu choi 1 don dat phong (vd het phong dot xuat, hoac ly do khac).
    @PostMapping("/don-dat-phong/{id}/tu-choi")
    public String reject(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        bookingService.rejectBooking(id, currentUser.getId());
        return "redirect:/chu-nha/don-dat-phong";
    }

    // Danh dau khach da NHAN PHONG (check-in).
    @PostMapping("/don-dat-phong/{id}/nhan-phong")
    public String checkIn(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        bookingService.checkIn(id, currentUser.getId());
        return "redirect:/chu-nha/don-dat-phong";
    }

    // Danh dau khach da TRA PHONG (check-out). Neu da thanh toan se thanh COMPLETED.
    @PostMapping("/don-dat-phong/{id}/tra-phong")
    public String checkOut(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        bookingService.checkOut(id, currentUser.getId());
        return "redirect:/chu-nha/don-dat-phong";
    }

    // Xac nhan khach da THANH TOAN (PAID). Neu da checkout thi tu dong COMPLETED.
    @PostMapping("/don-dat-phong/{id}/thanh-toan")
    public String markPaid(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        bookingService.markPaymentPaid(id, currentUser.getId());
        return "redirect:/chu-nha/don-dat-phong";
    }

    // Xac nhan HOAN THANH DON (COMPLETED va PAID).
    @PostMapping("/don-dat-phong/{id}/hoan-thanh")
    public String complete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        bookingService.completeBooking(id, currentUser.getId());
        return "redirect:/chu-nha/don-dat-phong";
    }

    // Danh sach danh gia cua khach hang cho cac homestay cua chu nha
    @GetMapping("/danh-gia")
    public String reviews(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        model.addAttribute("reviews", reviewService.getReviewsOfHost(currentUser.getId()));
        return "chu-nha/danh-sach-danh-gia";
    }

    // Chu nha gui phan hoi cho 1 danh gia
    @PostMapping("/danh-gia/{reviewId}/phan-hoi")
    public String replyReview(@PathVariable Long reviewId,
                               @RequestParam String replyContent,
                               @AuthenticationPrincipal CustomUserDetails currentUser,
                               RedirectAttributes redirectAttributes) {
        try {
            reviewService.replyReview(reviewId, currentUser.getId(), replyContent);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi phản hồi đánh giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/chu-nha/danh-gia";
    }
}
