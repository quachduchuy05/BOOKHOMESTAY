package BookingHomeStay.BookingHomeStay.service;

import BookingHomeStay.BookingHomeStay.dto.ChangePasswordForm;
import BookingHomeStay.BookingHomeStay.dto.ProfileForm;
import BookingHomeStay.BookingHomeStay.dto.ResetPasswordForm;
import BookingHomeStay.BookingHomeStay.entity.User;
import BookingHomeStay.BookingHomeStay.repository.UserRepository;
import BookingHomeStay.BookingHomeStay.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Nguyễn Văn A")
                .password("encoded_old_pass")
                .phone("0912345678")
                .build();
    }

    @Test
    void testCapNhatHoSoSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileForm form = ProfileForm.builder()
                .fullName("Nguyễn Văn B")
                .phone("0988888888")
                .address("Hà Nội")
                .avatar("https://example.com/avatar.png")
                .build();

        User updated = userService.capNhatHoSo(1L, form);

        assertEquals("Nguyễn Văn B", updated.getFullName());
        assertEquals("0988888888", updated.getPhone());
        assertEquals("Hà Nội", updated.getAddress());
        assertEquals("https://example.com/avatar.png", updated.getAvatar());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDoiMatKhauSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old_pass", "encoded_old_pass")).thenReturn(true);
        when(passwordEncoder.encode("new_pass123")).thenReturn("encoded_new_pass");

        ChangePasswordForm form = ChangePasswordForm.builder()
                .currentPassword("old_pass")
                .newPassword("new_pass123")
                .confirmPassword("new_pass123")
                .build();

        userService.doiMatKhau(1L, form);

        assertEquals("encoded_new_pass", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDoiMatKhauWrongCurrentPasswordThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_pass", "encoded_old_pass")).thenReturn(false);

        ChangePasswordForm form = ChangePasswordForm.builder()
                .currentPassword("wrong_pass")
                .newPassword("new_pass123")
                .confirmPassword("new_pass123")
                .build();

        assertThrows(IllegalArgumentException.class, () -> userService.doiMatKhau(1L, form));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testDatLaiMatKhauSuccess() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(otpService.verifyOtp("test@example.com", "123456")).thenReturn(true);
        when(passwordEncoder.encode("new_secret_pass")).thenReturn("encoded_secret");

        ResetPasswordForm form = ResetPasswordForm.builder()
                .email("test@example.com")
                .otpCode("123456")
                .newPassword("new_secret_pass")
                .confirmPassword("new_secret_pass")
                .build();

        userService.datLaiMatKhau(form);

        assertEquals("encoded_secret", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }
}
