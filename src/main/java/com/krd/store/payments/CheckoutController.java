package com.krd.store.payments;

import com.krd.starter.payment.dto.CheckoutRequest;
import com.krd.starter.payment.dto.CheckoutResponse;
import com.krd.starter.payment.gateway.PaymentException;
import com.krd.starter.payment.models.WebhookRequest;
import com.krd.store.carts.CartEmptyException;
import com.krd.store.carts.CartNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/checkout")
@Tag(name = "Checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    public CheckoutResponse checkout(@Valid @RequestBody CheckoutRequest request) {
        return checkoutService.checkout(request);
    }

    @PostMapping("/webhook")
    public void handleWebhook(
            // This header is used to ensure the request actually came from Stripe and that it was not tampered with
            @RequestHeader Map<String,String> headers,
            // The JSON object Stripe sends us that describes what happened during the payment process
            @RequestBody String payload
    ){
        checkoutService.handleWebhookEvent(new WebhookRequest(headers, payload));
    }


    @ExceptionHandler({CartNotFoundException.class, CartEmptyException.class})
    public ResponseEntity<Map<String, String>> handleCartNotFoundException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, String>> handlePaymentException() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error creating a checkout session."));
    }

}
