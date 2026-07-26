package com.grocery.customerportal.repository;

import com.grocery.customerportal.model.CartItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByShoppingCartCartIdAndProductProductId(Long cartId, Long productId);
}
