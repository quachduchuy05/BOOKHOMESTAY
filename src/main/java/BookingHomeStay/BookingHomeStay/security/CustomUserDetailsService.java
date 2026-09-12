 package BookingHomeStay.BookingHomeStay.security;

import BookingHomeStay.BookingHomeStay.entity.User;
import BookingHomeStay.BookingHomeStay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        if (identifier == null || identifier.isBlank()) {
            throw new UsernameNotFoundException("Vui lòng nhập Email hoặc Số điện thoại");
        }
        String clean = identifier.trim();
        User user = userRepository.findByEmail(clean)
                .or(() -> userRepository.findByPhone(clean))
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản với Email hoặc Số điện thoại: " + clean));
        return new CustomUserDetails(user);
    }
}