package com.krd.store.payments;

import lombok.Data;

@Data
public class CheckoutResponse {
    private Long orderId;
    private String stripeCheckoutUrl;

    public CheckoutResponse(Long orderId, String stripeCheckoutUrl) {
        this.orderId = orderId;
        this.stripeCheckoutUrl = stripeCheckoutUrl;
    }
}
