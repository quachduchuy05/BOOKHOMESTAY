package BookingHomeStay.BookingHomeStay.service.impl;

import BookingHomeStay.BookingHomeStay.dto.RegisterRequest;
import BookingHomeStay.BookingHomeStay.entity.*;
import BookingHomeStay.BookingHomeStay.repository.CollaboratorRepository;
import BookingHomeStay.BookingHomeStay.repository.HostRepository;
import BookingHomeStay.BookingHomeStay.repository.RoleRepository;
import BookingHomeStay.BookingHomeStay.repository.UserRepository;
import BookingHomeStay.BookingHomeStay.service.EmailService;
import BookingHomeStay.BookingHomeStay.service.OtpService;
import BookingHomeStay.BookingHomeStay.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HostRepository hostRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email da duoc su dung: " + request.getEmail());
        }

        // Xac thuc OTP truoc khi tao tai khoan (task 9). Dia chi nhan OTP la email hoac
        // so dien thoai tuy theo kenh nguoi dung chon o form dang ky.
        String otpDestination = request.getOtpChannel() == OtpChannel.EMAIL
                ? request.getEmail() : request.getPhone();
        if (request.getOtpCode() == null || !otpService.verifyOtp(otpDestination, request.getOtpCode())) {
            throw new IllegalArgumentException("Ma OTP khong dung hoac da het han, vui long thu lai.");
        }

        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName("ROLE_CUSTOMER").orElseThrow());
        if (request.isRegisterAsHost()) {
            roles.add(roleRepository.findByName("ROLE_HOST").orElseThrow());
        }
        if (request.isRegisterAsCollaborator()) {
            roles.add(roleRepository.findByName("ROLE_COLLABORATOR").orElseThrow());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .roles(roles)
                .build();
        user = userRepository.save(user);

        if (request.isRegisterAsHost()) {
            Host host = Host.builder()
                    .user(user)
                    .businessName(request.getFullName())
                    .status(HostStatus.PENDING)
                    .build();
            hostRepository.save(host);
        }

        if (request.isRegisterAsCollaborator()) {
            Collaborator collaborator = Collaborator.builder()
                    .user(user)
                    .maGioiThieu("CTV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .soTaiKhoanNhanHoaHong(request.getBankAccountNumber())
                    .tenNganHang(request.getBankName())
                    .status(CollaboratorStatus.PENDING)
                    .build();
            collaboratorRepository.save(collaborator);
        }

        // Task 8: gui email thong bao dang ky thanh cong + link truy cap trang web.
        // Neu chua cau hinh SMTP that thi EmailService se tu log ra console, khong
        // lam gian doan qua trinh dang ky (xem EmailServiceImpl.java).
        emailService.send(
                user.getEmail(),
                "Đăng ký tài khoản BookingHomeStay thành công",
                "Xin chào " + user.getFullName() + ",\n\n"
                        + "Bạn đã đăng ký tài khoản BookingHomeStay thành công.\n"
                        + "Truy cập trang web tại: " + baseUrl + "/trang-chu\n"
                        + "Đăng nhập tại: " + baseUrl + "/dang-nhap\n\n"
                        + "Cảm ơn bạn đã sử dụng dịch vụ!"
        );

        return user;
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));
    }

    @Override
    @Transactional
    public User capNhatHoSo(Long userId, BookingHomeStay.BookingHomeStay.dto.ProfileForm form) {
        User user = findById(userId);
        if (form.getFullName() != null && !form.getFullName().isBlank()) {
            user.setFullName(form.getFullName().trim());
        }
        if (form.getPhone() != null) {
            user.setPhone(form.getPhone().trim());
        }
        if (form.getAddress() != null) {
            user.setAddress(form.getAddress().trim());
        }

        // Xu ly upload avatar file neu co
        if (form.getAvatarFile() != null && !form.getAvatarFile().isEmpty()) {
            try {
                String originalFilename = form.getAvatarFile().getOriginalFilename();
                String ext = (originalFilename != null && originalFilename.contains(".")) 
                        ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
                String filename = "avatar_" + userId + "_" + System.currentTimeMillis() + ext;

                java.nio.file.Path uploadDir = java.nio.file.Paths.get("src/main/resources/static/images/avatars");
                if (!java.nio.file.Files.exists(uploadDir)) {
                    java.nio.file.Files.createDirectories(uploadDir);
                }
                java.nio.file.Path filePath = uploadDir.resolve(filename);
                form.getAvatarFile().transferTo(filePath.toFile());

                // Copy vao target/classes neu ton tai de hien thi ngay tren server
                java.nio.file.Path targetDir = java.nio.file.Paths.get("target/classes/static/images/avatars");
                if (java.nio.file.Files.exists(targetDir.getParent())) {
                    if (!java.nio.file.Files.exists(targetDir)) {
                        java.nio.file.Files.createDirectories(targetDir);
                    }
                    java.nio.file.Files.copy(filePath, targetDir.resolve(filename), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }

                user.setAvatar("/images/avatars/" + filename);
            } catch (Exception e) {
                // Neu upload file that bai, fallback sang avatar link neu co
                if (form.getAvatar() != null && !form.getAvatar().isBlank()) {
                    user.setAvatar(form.getAvatar().trim());
                }
            }
        } else if (form.getAvatar() != null && !form.getAvatar().isBlank()) {
            user.setAvatar(form.getAvatar().trim());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void doiMatKhau(Long userId, BookingHomeStay.BookingHomeStay.dto.ChangePasswordForm form) {
        User user = findById(userId);
        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không trùng khớp");
        }
        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void sendForgotPasswordOtp(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Vui lòng cung cấp địa chỉ email");
        }
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản liên kết với email: " + email.trim()));
        otpService.sendOtp(user.getEmail(), OtpChannel.EMAIL);
    }

    @Override
    @Transactional
    public void datLaiMatKhau(BookingHomeStay.BookingHomeStay.dto.ResetPasswordForm form) {
        User user = userRepository.findByEmail(form.getEmail().trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + form.getEmail().trim()));

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không trùng khớp");
        }
        if (!otpService.verifyOtp(form.getEmail().trim(), form.getOtpCode().trim())) {
            throw new IllegalArgumentException("Mã OTP không đúng hoặc đã hết hạn");
        }

        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(user);
    }
}
