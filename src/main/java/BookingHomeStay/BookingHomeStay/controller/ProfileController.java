package BookingHomeStay.BookingHomeStay.controller;

import BookingHomeStay.BookingHomeStay.dto.ChangePasswordForm;
import BookingHomeStay.BookingHomeStay.dto.ProfileForm;
import BookingHomeStay.BookingHomeStay.entity.User;
import BookingHomeStay.BookingHomeStay.security.CustomUserDetails;
import BookingHomeStay.BookingHomeStay.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/ho-so")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails currentUser,
                              @RequestParam(value = "tab", required = false, defaultValue = "info") String tab,
                              Model model) {
        User user = userService.findById(currentUser.getId());
        ProfileForm profileForm = ProfileForm.builder()
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .avatar(user.getAvatar())
                .build();

        model.addAttribute("user", user);
        model.addAttribute("profileForm", profileForm);
        if (!model.containsAttribute("changePasswordForm")) {
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
        }
        model.addAttribute("activeTab", tab);
        return "nguoi-dung/ho-so";
    }

    @PostMapping
    public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileForm profileForm,
                                BindingResult result,
                                @AuthenticationPrincipal CustomUserDetails currentUser,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            User user = userService.findById(currentUser.getId());
            model.addAttribute("user", user);
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
            model.addAttribute("activeTab", "info");
            return "nguoi-dung/ho-so";
        }

        try {
            User updated = userService.capNhatHoSo(currentUser.getId(), profileForm);
            if (currentUser != null && updated != null) {
                currentUser.setAvatar(updated.getAvatar());
                currentUser.setFullName(updated.getFullName());
            }
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ cá nhân thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/ho-so";
    }

    @PostMapping("/doi-mat-khau")
    public String changePassword(@Valid @ModelAttribute("changePasswordForm") ChangePasswordForm changePasswordForm,
                                 BindingResult result,
                                 @AuthenticationPrincipal CustomUserDetails currentUser,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("passwordErrorMessage", result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/ho-so?tab=password";
        }

        try {
            userService.doiMatKhau(currentUser.getId(), changePasswordForm);
            redirectAttributes.addFlashAttribute("passwordSuccessMessage", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("passwordErrorMessage", e.getMessage());
        }
        return "redirect:/ho-so?tab=password";
    }
}
