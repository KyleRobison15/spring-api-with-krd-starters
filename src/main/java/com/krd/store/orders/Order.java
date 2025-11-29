package com.krd.store.orders;

import com.krd.starter.payment.models.OrderInfo;
import com.krd.starter.payment.models.PaymentStatus;
import com.krd.store.carts.Cart;
import com.krd.store.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order implements OrderInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;


    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<OrderItem> items = new HashSet<>();

    public static Order fromCart(Cart cart, User customer) {
        var order = new Order();
        order.setCustomer(customer);
        order.setStatus(PaymentStatus.PENDING);
        order.setTotalPrice(cart.getTotalPrice());

        cart.getItems().forEach(item -> {
            var orderItem = new OrderItem(order, item.getProduct(), item.getQuantity());
            order.items.add(orderItem);
        });

        return order;
    }

    public boolean isPlacedBy(User customer) {
        return this.customer.equals(customer);
    }

    // Implement OrderInfo interface methods
    @Override
    public Long getOrderId() {
        return this.id;
    }

    @Override
    public List<LineItem> getLineItems() {
        return items.stream()
                .map(item -> new LineItem(
                        item.getProduct().getName(),
                        item.getUnitPrice(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());
    }
}