package com.grocery.customerportal.service;

import com.grocery.customerportal.model.CartItem;
import com.grocery.customerportal.model.Customer;
import com.grocery.customerportal.model.Product;
import com.grocery.customerportal.model.ShoppingCart;
import com.grocery.customerportal.repository.CartItemRepository;
import com.grocery.customerportal.repository.CustomerRepository;
import com.grocery.customerportal.repository.ProductRepository;
import com.grocery.customerportal.repository.ShoppingCartRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public ShoppingCartService(
            ShoppingCartRepository shoppingCartRepository,
            CartItemRepository cartItemRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.cartItemRepository = cartItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CartItem addItem(Long customerId, Long productId, int quantity) {
        validateQuantity(quantity);
        Product product = getProduct(productId);
        validateAvailableQuantity(product, quantity);
        ShoppingCart cart = getOrCreateCart(customerId);

        CartItem item = cartItemRepository
                .findByShoppingCartCartIdAndProductProductId(cart.getCartId(), productId)
                .orElse(null);

        if (item == null) {
            item = new CartItem();
            item.setShoppingCart(cart);
            item.setProduct(product);
            item.setItemPrice(product.getPrice());
            item.setQuantity(quantity);
            cart.getItems().add(item);
        } else {
            int newQuantity = item.getQuantity() + quantity;
            validateAvailableQuantity(product, newQuantity);
            item.setQuantity(newQuantity);
        }

        return cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(Long customerId, Long cartItemId) {
        ShoppingCart cart = getOrCreateCart(customerId);
        CartItem item = findOwnedItem(cart, cartItemId);
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
    }

    @Transactional
    public CartItem updateQuantity(Long customerId, Long cartItemId, int quantity) {
        validateQuantity(quantity);
        ShoppingCart cart = getOrCreateCart(customerId);
        CartItem item = findOwnedItem(cart, cartItemId);
        validateAvailableQuantity(item.getProduct(), quantity);
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public ShoppingCart viewCart(Long customerId) {
        return shoppingCartRepository.findByCustomerCustomerId(customerId)
                .orElseGet(() -> emptyUnsavedCart(customerId));
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateSubtotal(Long customerId) {
        return viewCart(customerId).calculateSubtotal();
    }

    @Transactional
    public void clearCart(Long customerId) {
        ShoppingCart cart = getOrCreateCart(customerId);
        cart.getItems().clear();
        shoppingCartRepository.save(cart);
    }

    public List<Product> listProducts() {
        return productRepository.findAllByOrderByNameAsc();
    }

    private ShoppingCart getOrCreateCart(Long customerId) {
        return shoppingCartRepository.findByCustomerCustomerId(customerId)
                .orElseGet(() -> shoppingCartRepository.save(emptyUnsavedCart(customerId)));
    }

    private ShoppingCart emptyUnsavedCart(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer was not found."));
        ShoppingCart cart = new ShoppingCart();
        cart.setCustomer(customer);
        return cart;
    }

    private CartItem findOwnedItem(ShoppingCart cart, Long cartItemId) {
        return cart.getItems().stream()
                .filter(item -> item.getCartItemId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cart item was not found."));
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product was not found."));
    }

    private void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
    }

    private void validateAvailableQuantity(Product product, int quantity) {
        if (!product.isAvailable()) {
            throw new IllegalArgumentException(product.getName() + " is unavailable.");
        }
        if (quantity > product.getQuantityAvailable()) {
            throw new IllegalArgumentException(
                    "Only " + product.getQuantityAvailable() + " of " + product.getName() + " are available.");
        }
    }
}
