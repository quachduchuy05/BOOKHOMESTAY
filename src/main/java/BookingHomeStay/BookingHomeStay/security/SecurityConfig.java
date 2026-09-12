package BookingHomeStay.BookingHomeStay.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                "/dang-ky/gui-otp",
                "/quen-mat-khau/gui-otp",
                "/api/v1/sepay/webhook"
            ))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/trang-chu", "/trang-chu/**", "/tim-kiem", "/tim-kiem/**", "/homestay/**", "/dang-nhap", "/dang-ky",
                    "/dang-ky/gui-otp", "/quen-mat-khau", "/quen-mat-khau/**", "/dat-lai-mat-khau", "/dat-lai-mat-khau/**",
                    "/api/**", "/css/**", "/js/**", "/images/**", "/uploads/**"
                ).permitAll()
                // Cong tac vien dung chung khong gian voi Chu nha (ca hai deu "ban hang phong")
                .requestMatchers("/khach-hang/**").hasAnyRole("CUSTOMER", "COLLABORATOR")
                .requestMatchers("/chu-nha/**").hasAnyRole("HOST", "COLLABORATOR")
                .requestMatchers("/cong-tac-vien/**").hasAnyRole("COLLABORATOR", "ADMIN")
                .requestMatchers("/quan-tri/**", "/admin", "/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/dang-nhap")
                .successHandler(new LoginSuccessHandler())
                .failureUrl("/dang-nhap?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/dang-nhap?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/khong-co-quyen"))
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}