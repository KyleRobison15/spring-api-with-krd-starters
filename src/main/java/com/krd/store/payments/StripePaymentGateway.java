package com.krd.store.payments;

import com.krd.store.orders.Order;
import com.krd.store.orders.OrderItem;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class StripePaymentGateway implements PaymentGateway {

    // Inject the URL for the client dynamically (for environment URL flexibility)
    @Value("${websiteUrl}")
    private String websiteUrl;

    @Value("${stripe.webhookSecretKey}")
    private String webhookSecretKey;

    @Override
    public CheckoutSession createCheckoutSession(Order order) {
        try {
            // Create the parameters to be used for the Stripe Session
            var builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(websiteUrl + "/checkout-success?orderId=" + order.getId())
                    .setCancelUrl(websiteUrl + "/checkout-cancel")
                    // Pass the order id to Stripe as a Metadata object
                    // This will allow us to update the order status later when Stripe sends us events related to this payment
                    .setPaymentIntentData(createPaymentIntent(order)
                    );

            // Create and add each line item to our Stripe Session Params
            order.getItems().forEach(item -> {
                var lineItem = createLineItem(item);
                builder.addLineItem(lineItem);
            });

            // Create the Stripe Session using the Stripe Session Params we built up
            var session = Session.create(builder.build());
            return new CheckoutSession(session.getUrl());
        } catch (StripeException e) {
            // In a real world application, here you should use a Logging service to log the exception for monitoring purposes
            System.out.println(e.getMessage());
            throw new PaymentException();
        }
    }

    private static SessionCreateParams.PaymentIntentData createPaymentIntent(Order order) {
        return SessionCreateParams.PaymentIntentData.builder()
                .putMetadata("order_id", order.getId().toString())
                .build();
    }

    @Override
    public Optional<PaymentResult> parseWebhookRequest(WebhookRequest request) {
        try {
            var payload = request.getPayload();
            var signature = request.getHeaders().get("stripe-signature");

            // Securely extract the details of the event from Stripe so we know what happened
            var event = Webhook.constructEvent(payload, signature, webhookSecretKey);

            // Check the type of the event
            return switch (event.getType()) {
                // If payment succeeded, return a PaymentResult with status of PAID
                case "payment_intent.succeeded" ->
                        Optional.of(new PaymentResult(extractOrderId(event), PaymentStauts.PAID));

                // If payment failed, return a PaymentResult with status of FAILED
                case "payment_intent.payment_failed" ->
                        Optional.of(new PaymentResult(extractOrderId(event), PaymentStauts.FAILED));

                default -> Optional.empty();
            };

        } catch (SignatureVerificationException e) {
            throw new PaymentException("Invalid Signature.");
        }

    }

    private Long extractOrderId(Event event) {
        // Get the StripeObject from the event
        // The StripeObject class is the most general Stripe object class
        // This can be null if the Stripe SDK and API versions are incompatible -> throw an exception in that case
        var stripeObject = event.getDataObjectDeserializer().getObject().orElseThrow(
                () -> new PaymentException("Could not deserialize Stripe event. Check the SDK and API versions.")
        );

        // Depending on the event type, we need to cast the object from the event to a more specific type
        // charge -> (Charge) stripeObject
        // payment_intent.succeeded -> (PaymentIntent) stripeObject
        var paymentIntent = (PaymentIntent) stripeObject;
        return Long.valueOf(paymentIntent.getMetadata().get("order_id"));
    }

    private SessionCreateParams.LineItem createLineItem(OrderItem item) {
        // Create a "Stripe Line Item" param for each of the items in the order
        return SessionCreateParams.LineItem.builder()
                .setQuantity(Long.valueOf(item.getQuantity()))
                .setPriceData(createPriceData(item))
                .build();
    }

    private SessionCreateParams.LineItem.PriceData createPriceData(OrderItem item) {
        return SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("usd")
                // Must use smallest unit of currency when passing amount to stripe
                // So we have to convert the price in dollars to the price in cents
                .setUnitAmountDecimal(item.getUnitPrice().multiply(BigDecimal.valueOf(100)))
                .setProductData(createProductData(item))
                .build();
    }

    private SessionCreateParams.LineItem.PriceData.ProductData createProductData(OrderItem item) {
        return SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(item.getProduct().getName())
                .build();
    }
}
