package com.krd.store.carts;

import com.krd.store.products.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "date_created", insertable = false, updatable = false)
    private LocalDate dateCreated;

    // To persist a Cart Item when saving a cart, we use the MERGE cascade type
        // This is because we are UPDATING a cart when we add a cart item
        // The MERGE attribute tells JPA to persist the update for any child entities
        // Cart Item is a child of Cart
    @OneToMany(mappedBy = "cart", cascade = {CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<CartItem> items = new HashSet<>();

    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartItem item : items) {
            totalPrice = totalPrice.add(item.getTotalPrice());
        }
        return totalPrice;
    }

    public CartItem getItem(Long productId) {
        // Find the cart item for the given product id
        return items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    public CartItem addItem(Product product) {
        // Check if the product already exists in the cart
        var cartItem = getItem(product.getId());

        // If the product already exists in the cart, increment its quantity
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        }

        // Otherwise, create a new cart item
        else {
            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(1);
            cartItem.setCart(this);
            items.add(cartItem);
        }

        return cartItem;
    }

    public void removeItem(Long productId) {
        var cartItem = getItem(productId);
        if (cartItem != null) {
            items.remove(cartItem);
            cartItem.setCart(null);
        }
    }

    public void clearItems() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

}