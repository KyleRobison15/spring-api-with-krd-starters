package com.krd.store.users;

import com.krd.starter.user.BaseUser;
import com.krd.store.products.Product;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users")
public class User extends BaseUser {
    // Base fields (id, firstName, lastName, username, email, password, roles, enabled, deletedAt)
    // are inherited from BaseUser

    // Domain-specific fields for this application
    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "wishlist",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @Builder.Default
    private Set<Product> favoriteProducts = new HashSet<>();

    // Domain-specific helper methods
    public void addAddress(Address address) {
        addresses.add(address);
        address.setUser(this);
    }

    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setUser(null);
    }

    public void addFavoriteProduct(Product product) {
        favoriteProducts.add(product);
    }
}
