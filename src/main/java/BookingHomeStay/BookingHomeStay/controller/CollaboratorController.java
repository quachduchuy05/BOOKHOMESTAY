package BookingHomeStay.BookingHomeStay.controller;

import BookingHomeStay.BookingHomeStay.entity.Collaborator;
import BookingHomeStay.BookingHomeStay.security.CustomUserDetails;
import BookingHomeStay.BookingHomeStay.service.CollaboratorService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/cong-tac-vien")
@RequiredArgsConstructor
public class CollaboratorController {

    private final CollaboratorService collaboratorService;

    @GetMapping("/tong-quan")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAllAttributes(collaboratorService.getDashboard(user.getId()));
        return "cong-tac-vien/tong-quan";
    }

    @GetMapping("/hoa-hong")
    public String commissions(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("commissions", collaboratorService.getMyCommissions(user.getId()));
        return "cong-tac-vien/hoa-hong";
    }

    @GetMapping("/rut-tien")
    public String withdrawalForm(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        Collaborator collaborator = collaboratorService.getMyCollaborator(user.getId());
        model.addAttribute("collaborator", collaborator);
        model.addAttribute("availableBalance", collaboratorService.getAvailableBalance(user.getId()));
        return "cong-tac-vien/rut-tien";
    }

    @PostMapping("/rut-tien")
    public String requestWithdrawal(@AuthenticationPrincipal CustomUserDetails user,
                                    @RequestParam BigDecimal amount,
                                    Model model) {
        try {
            collaboratorService.requestWithdrawal(user.getId(), amount);
            return "redirect:/cong-tac-vien/rut-tien?success=true";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("collaborator", collaboratorService.getMyCollaborator(user.getId()));
            model.addAttribute("availableBalance", collaboratorService.getAvailableBalance(user.getId()));
            return "cong-tac-vien/rut-tien";
        }
    }

    @PostMapping("/cap-nhat-ngan-hang")
    public String updateBankInfo(@AuthenticationPrincipal CustomUserDetails user,
                                 @RequestParam String tenNganHang,
                                 @RequestParam String soTaiKhoanNhanHoaHong,
                                 @RequestParam(required = false, defaultValue = "tong-quan") String redirectPage,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            collaboratorService.updateBankInfo(user.getId(), tenNganHang, soTaiKhoanNhanHoaHong);
            redirectAttributes.addFlashAttribute("bankSuccessMessage", "Cập nhật thông tin tài khoản ngân hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("bankErrorMessage", e.getMessage());
        }
        if ("rut-tien".equals(redirectPage)) {
            return "redirect:/cong-tac-vien/rut-tien";
        }
        return "redirect:/cong-tac-vien/tong-quan";
    }

    @GetMapping("/lich-su-rut-tien")
    public String withdrawals(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("withdrawals", collaboratorService.getMyWithdrawals(user.getId()));
        return "cong-tac-vien/lich-su-rut-tien";
    }
}
