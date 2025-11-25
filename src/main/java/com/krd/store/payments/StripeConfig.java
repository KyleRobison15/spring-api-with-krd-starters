package com.krd.store.payments;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.secretKey}")
    private String secretKey;

    @PostConstruct // Tells spring to call this method when this bean is created
    public void init(){
        Stripe.apiKey = secretKey;
    }

}
