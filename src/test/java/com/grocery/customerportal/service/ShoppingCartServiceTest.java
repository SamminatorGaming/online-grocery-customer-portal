package com.grocery.customerportal.service;

import com.grocery.customerportal.model.CartItem;
import com.grocery.customerportal.model.Customer;
import com.grocery.customerportal.model.Product;
import com.grocery.customerportal.model.ShoppingCart;
import com.grocery.customerportal.repository.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:carttestdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ShoppingCartServiceTest {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductRepository productRepository;

    private Customer customer;
    private Product apples;

    @BeforeEach
    void setUp() {
        customer = customerService.register("Cart Tester", "cart@example.com", "pass123");
        apples = saveProduct("Apples", "1.50", 10);
    }

    @Test
    void addItemAddsProductToCart() {
        shoppingCartService.addItem(customer.getCustomerId(), apples.getProductId(), 2);

        ShoppingCart cart = shoppingCartService.viewCart(customer.getCustomerId());
        assertEquals(1, cart.getItems().size());
        assertEquals("Apples", cart.getItems().get(0).getProduct().getName());
        assertEquals(2, cart.getItems().get(0).getQuantity());
    }

    @Test
    void addingSameProductUpdatesExistingCartItem() {
        shoppingCartService.addItem(customer.getCustomerId(), apples.getProductId(), 2);
        shoppingCartService.addItem(customer.getCustomerId(), apples.getProductId(), 3);

        ShoppingCart cart = shoppingCartService.viewCart(customer.getCustomerId());
        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }

    @Test
    void addItemRejectsUnavailableQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                shoppingCartService.addItem(customer.getCustomerId(), apples.getProductId(), 11));
    }

    @Test
    void removeItemDeletesProductFromCart() {
        CartItem item = shoppingCartService.addItem(customer.getCustomerId(), apples.getProductId(), 1);

        shoppingCartService.removeItem(customer.getCustomerId(), item.getCartItemId());

        assertTrue(shoppingCartService.viewCart(customer.getCustomerId()).getItems().isEmpty());
    }

    @Test
    void updateQuantityChangesQuantity() {
        CartItem item = shoppingCartService.addItem(customer.getCustomerId(), apples.getProductId(), 1);

        shoppingCartService.updateQuantity(customer.getCustomerId(), item.getCartItemId(), 4);

        ShoppingCart cart = shoppingCartService.viewCart(customer.getCustomerId());
        assertEquals(4, cart.getItems().get(0).getQuantity());
    }

    @Test
    void updateQuantityRejectsQuantityAboveStock() {
        CartItem item = shoppingCartService.addItem(customer.getCustomerId(), apples.getProductId(), 1);

        assertThrows(IllegalArgumentException.class, () ->
                shoppingCartService.updateQuantity(customer.getCustomerId(), item.getCartItemId(), 20));
    }

    @Test
    void calculateSubtotalAddsAllLineTotals() {
        Product bread = saveProduct("Bread", "3.25", 10);
        shoppingCartService.addItem(customer.getCustomerId(), apples.getProductId(), 2);
        shoppingCartService.addItem(customer.getCustomerId(), bread.getProductId(), 1);

        assertEquals(new BigDecimal("6.25"), shoppingCartService.calculateSubtotal(customer.getCustomerId()));
    }

    @Test
    void emptyCartHasZeroSubtotal() {
        assertEquals(BigDecimal.ZERO, shoppingCartService.calculateSubtotal(customer.getCustomerId()));
    }

    @Test
    void clearCartRemovesAllItems() {
        Product bread = saveProduct("Bread", "3.25", 10);
        shoppingCartService.addItem(customer.getCustomerId(), apples.getProductId(), 1);
        shoppingCartService.addItem(customer.getCustomerId(), bread.getProductId(), 1);

        shoppingCartService.clearCart(customer.getCustomerId());

        assertTrue(shoppingCartService.viewCart(customer.getCustomerId()).getItems().isEmpty());
    }

    private Product saveProduct(String name, String price, int quantityAvailable) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(name + " description");
        product.setPrice(new BigDecimal(price));
        product.setQuantityAvailable(quantityAvailable);
        return productRepository.save(product);
    }
}
