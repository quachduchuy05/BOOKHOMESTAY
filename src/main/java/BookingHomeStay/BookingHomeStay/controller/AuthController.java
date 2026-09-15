package BookingHomeStay.BookingHomeStay.controller;

import BookingHomeStay.BookingHomeStay.dto.RegisterRequest;
import BookingHomeStay.BookingHomeStay.dto.ResetPasswordForm;
import BookingHomeStay.BookingHomeStay.entity.OtpChannel;
import BookingHomeStay.BookingHomeStay.service.OtpService;
import BookingHomeStay.BookingHomeStay.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final OtpService otpService;

    // Goi bang AJAX tu nut "Gui ma" o trang dang ky.
    // dest = email hoac so dien thoai tuong ung voi kenh da chon.
    @PostMapping("/dang-ky/gui-otp")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> guiOtp(@RequestParam String dest, @RequestParam OtpChannel kenh) {
        log.info("[AUTH-CONTROLLER] Nhận request POST /dang-ky/gui-otp: dest={}, kenh={}", dest, kenh);

        if (dest == null || dest.trim().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Vui lòng nhập " + (kenh == OtpChannel.EMAIL ? "địa chỉ email" : "số điện thoại")
                            + " trước khi gửi mã."));
        }
        try {
            otpService.sendOtp(dest.trim(), kenh);
            log.info("[AUTH-CONTROLLER] Gửi OTP thành công cho {}", dest);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Mã OTP đã được gửi thành công qua " + kenh + " tới " + dest.trim()));
        } catch (IllegalStateException e) {
            log.warn("[AUTH-CONTROLLER] Lỗi logic khi gửi OTP: {}", e.getMessage());
            HttpStatus status = e.getMessage().contains("Vui lòng đợi")
                    ? HttpStatus.TOO_MANY_REQUESTS
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        } catch (Exception e) {
            log.error("[AUTH-CONTROLLER] Lỗi hệ thống khi gửi OTP tới {}: {}", dest, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Không thể gửi mã OTP: " + e.getMessage()));
        }
    }

    @GetMapping("/dang-nhap")
    public String loginPage() {
        return "xac-thuc/dang-nhap";
    }

    @GetMapping("/dang-ky")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "xac-thuc/dang-ky";
    }

    @PostMapping("/dang-ky")
    public String register(@Valid @ModelAttribute RegisterRequest registerRequest,
            BindingResult result, Model model) {
        if (result.hasErrors())
            return "xac-thuc/dang-ky";
        try {
            userService.register(registerRequest);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "xac-thuc/dang-ky";
        }
        return "redirect:/dang-nhap?registered=true";
    }

    @GetMapping("/khong-co-quyen")
    public String accessDenied() {
        return "loi/403";
    }

    // ================= QUEN MAT KHAU & DAT LAI MAT KHAU =================

    @GetMapping("/quen-mat-khau")
    public String forgotPasswordPage() {
        return "xac-thuc/quen-mat-khau";
    }

    @PostMapping("/quen-mat-khau")
    public String processForgotPassword(@RequestParam String email,
                                        org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes,
                                        Model model) {
        if (email == null || email.trim().isBlank()) {
            model.addAttribute("errorMessage", "Vui lòng nhập địa chỉ email tài khoản.");
            return "xac-thuc/quen-mat-khau";
        }
        try {
            userService.sendForgotPasswordOtp(email.trim());
            redirectAttributes.addFlashAttribute("successMessage", "Mã xác thực OTP đã được gửi tới email của bạn. Vui lòng kiểm tra hộp thư!");
            return "redirect:/dat-lai-mat-khau?email=" + java.net.URLEncoder.encode(email.trim(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("email", email);
            return "xac-thuc/quen-mat-khau";
        }
    }

    @GetMapping("/dat-lai-mat-khau")
    public String resetPasswordPage(@RequestParam(required = false) String email, Model model) {
        ResetPasswordForm form = new ResetPasswordForm();
        if (email != null) {
            form.setEmail(email.trim());
        }
        model.addAttribute("resetPasswordForm", form);
        return "xac-thuc/dat-lai-mat-khau";
    }

    @PostMapping("/dat-lai-mat-khau")
    public String processResetPassword(@Valid @ModelAttribute("resetPasswordForm") ResetPasswordForm form,
                                       BindingResult result,
                                       org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes,
                                       Model model) {
        if (result.hasErrors()) {
            return "xac-thuc/dat-lai-mat-khau";
        }
        try {
            userService.datLaiMatKhau(form);
            redirectAttributes.addFlashAttribute("resetSuccessMessage", "Đặt lại mật khẩu thành công! Mời bạn đăng nhập bằng mật khẩu mới.");
            return "redirect:/dang-nhap";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "xac-thuc/dat-lai-mat-khau";
        }
    }
}