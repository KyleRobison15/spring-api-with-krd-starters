package com.krd.store.auth;

import com.krd.store.users.User;
import com.krd.store.users.UserDto;
import com.krd.store.users.UserMapper;
import com.krd.starter.jwt.BaseAuthController;
import com.krd.starter.jwt.JwtConfig;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController extends BaseAuthController<User, UserDto> {

    public AuthController(JwtConfig jwtConfig,
                         UserMapper userMapper,
                         AuthService authService) {
        super(jwtConfig, userMapper, authService);
    }

    // All authentication endpoints (login, refresh, me) are inherited from BaseAuthController

    /**
     * Revoke the refresh token by clearing the HttpOnly cookie.
     *
     * Note: The current access token remains valid until expiration (~15 minutes).
     * The user will not be able to obtain new access tokens after this call.
     *
     * For complete logout, the client should:
     * 1. Call this endpoint to revoke the refresh token
     * 2. Delete the access token from client memory
     * 3. Redirect to login page
     */
    @PostMapping("/revoke-refresh-token")
    public ResponseEntity<Void> revokeRefreshToken(HttpServletResponse response) {
        // Clear the refresh token cookie by setting MaxAge to 0
        var cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh"); // Must match the path used in login
        cookie.setMaxAge(0); // Immediately expire the cookie
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }
}
