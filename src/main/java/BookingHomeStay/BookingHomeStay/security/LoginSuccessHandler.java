package BookingHomeStay.BookingHomeStay.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.io.IOException;

public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {

        clearAuthenticationAttributes(request);

        // Định hướng thẳng sang giao diện quản lý tương ứng cho Admin, CTV và Chủ nhà
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_ADMIN".equals(role)) {
                getRedirectStrategy().sendRedirect(request, response, "/quan-tri/tong-quan");
                return;
            }
            if ("ROLE_COLLABORATOR".equals(role)) {
                getRedirectStrategy().sendRedirect(request, response, "/cong-tac-vien/tong-quan");
                return;
            }
            if ("ROLE_HOST".equals(role)) {
                getRedirectStrategy().sendRedirect(request, response, "/chu-nha/tong-quan");
                return;
            }
        }

        // Đối với Khách hàng (ROLE_CUSTOMER): nếu có savedRequest thì quay lại, nếu không thì về trang chủ
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            getRedirectStrategy().sendRedirect(request, response, savedRequest.getRedirectUrl());
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, "/");
    }
}