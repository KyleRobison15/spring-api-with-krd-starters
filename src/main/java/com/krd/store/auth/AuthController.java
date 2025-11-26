package com.krd.store.auth;

import com.krd.store.users.UserDto;
import com.krd.store.users.UserMapper;
import com.krd.starter.jwt.JwtConfig;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

    private final JwtConfig jwtConfig;
    private final UserMapper userMapper;
    private final AuthService authService;

    @PostMapping("/login")
    public JwtResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {

        var loginResponse = authService.login(request);
        var refreshToken = loginResponse.getRefreshToken().toString();

        var cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true); // Ensures this cookie CANNOT be accessed by JavaScript
        cookie.setPath("/auth/refresh"); // Ensures the only time this cookie is set is when a request is made to the /auth/refresh endpoint
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration()); //7d - this cookie should expire the same time the refresh token does
        cookie.setSecure(true); // Ensures this cookie can only be sent over HTTPS (and not unsecured HTTP)
        response.addCookie(cookie); // Add the cookie to the response

        // Return the access token in the response body
        return new JwtResponse(loginResponse.getAccessToken().toString());
    }

    @PostMapping("/refresh")
    public JwtResponse refresh(@CookieValue(value = "refreshToken") String refreshToken) {
        var accessToken = authService.refreshAccessToken(refreshToken);
        return new JwtResponse(accessToken.toString());
    }

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

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(){
        var user = authService.getCurrentUser();
        if(user == null){
            return ResponseEntity.notFound().build();
        }

        var userDto = userMapper.toDto(user);
        return ResponseEntity.ok(userDto);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialsException(){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
