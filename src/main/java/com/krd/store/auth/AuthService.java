package com.krd.store.auth;

import com.krd.store.users.User;
import com.krd.store.users.UserRepository;
import com.krd.auth.Jwt;
import com.krd.auth.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public User getCurrentUser() {
        // 1. Extract the principal from our Security Context
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        // We cast the result of getPrincipal() to a string because
        // we stored the EMAIL of our users as the principle in the authentication object (in our JWT Auth Filter)
        var userId = (Long) authentication.getPrincipal();

        // 2. Find the user in our database
        return userRepository.findById(userId).orElse(null);
    }

    public LoginResponse login(LoginRequest request) {

        // Authenticate the user using our Authentication Manager
        // which uses our UserDetailsService implementation to find a user and verify their password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Get the user from our database so we can generate the JWT for them
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        // Once the user is authenticated, generate an Access Token and Refresh Token
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken);
    }

    public Jwt refreshAccessToken(String refreshToken) {
        var jwt = jwtService.parseToken(refreshToken);
        if(jwt == null || jwt.isExpired()){
            throw new BadCredentialsException("Invalid refresh token");
        }

        var user = userRepository.findById(jwt.getUserId()).orElseThrow();
        return jwtService.generateAccessToken(user);
    }

}
