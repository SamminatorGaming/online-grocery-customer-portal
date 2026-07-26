package com.grocery.customerportal.repository;

import com.grocery.customerportal.model.ShoppingCart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    Optional<ShoppingCart> findByCustomerCustomerId(Long customerId);
}
