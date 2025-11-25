package com.codewithmosh.store.products;

import com.krd.security.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class ProductSecurityRules implements SecurityRules {

    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry.requestMatchers(HttpMethod.GET,"/products/**").permitAll()
                .requestMatchers(HttpMethod.POST,"/products/**").hasRole("ADMIN") // Allow admin role users to create products
                .requestMatchers(HttpMethod.PUT,"/products/**").hasRole("ADMIN") // Allow admin role users to update products
                .requestMatchers(HttpMethod.DELETE,"/products/**").hasRole("ADMIN"); // Allow admin role users to delete products
    }
}
