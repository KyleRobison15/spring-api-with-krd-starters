package com.krd.store.auth;

import com.krd.store.users.User;
import com.krd.store.users.UserRepository;
import com.krd.starter.jwt.BaseAuthService;
import com.krd.starter.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

@Service
public class AuthService extends BaseAuthService<User> {

    public AuthService(AuthenticationManager authenticationManager,
                      UserRepository userRepository,
                      JwtService jwtService) {
        super(authenticationManager, userRepository, jwtService);
    }

    // All authentication functionality is inherited from BaseAuthService
    // Add custom authentication methods here if needed
}
