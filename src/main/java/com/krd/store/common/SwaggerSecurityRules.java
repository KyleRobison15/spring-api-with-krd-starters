package com.krd.store.common;

import com.krd.security.SecurityRules;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class SwaggerSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry.requestMatchers("/swagger-ui/**").permitAll() // Always permit swagger docs
                .requestMatchers("/swagger-ui.html/**").permitAll() // Always permit swagger docs
                .requestMatchers("/v3/api-docs/**").permitAll(); // Always permit swagger docs
    }
}
