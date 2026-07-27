package com.grocery.customerportal.service;

import com.grocery.customerportal.model.Address;
import com.grocery.customerportal.model.Customer;
import com.grocery.customerportal.model.DeliveryOption;
import com.grocery.customerportal.model.DiscountCode;
import com.grocery.customerportal.model.Order;
import com.grocery.customerportal.model.Product;
import com.grocery.customerportal.repository.AddressRepository;
import com.grocery.customerportal.repository.DeliveryOptionRepository;
import com.grocery.customerportal.repository.DiscountCodeRepository;
import com.grocery.customerportal.repository.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:checkouttestdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CheckoutServiceTest {

    @Autowired private CheckoutService checkoutService;
    @Autowired private CustomerService customerService;
    @Autowired private ShoppingCartService cartService;
    @Autowired private ProductRepository productRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private DeliveryOptionRepository deliveryOptionRepository;
    @Autowired private DiscountCodeRepository discountCodeRepository;

    private Customer customer;
    private Address address;
    private DeliveryOption standardDelivery;

    @BeforeEach
    void setup() {
        customer = customerService.register("Test User", "checkout@test.com", "password");

        address = new Address();
        address.setStreet("100 Main St");
        address.setCity("San Antonio");
        address.setState("TX");
        address.setZipCode("78201");
        address.setCustomer(customer);
        addressRepository.save(address);

        standardDelivery = new DeliveryOption();
        standardDelivery.setType("Standard");
        standardDelivery.setFee(new BigDecimal("4.99"));
        standardDelivery.setEstimatedTime("5-7 business days");
        deliveryOptionRepository.save(standardDelivery);

        DiscountCode code = new DiscountCode();
        code.setCode("SAVE10");
        code.setDiscountType("PERCENTAGE");
        code.setDiscountValue(new BigDecimal("10"));
        code.setActive(true);
        discountCodeRepository.save(code);

        Product product = new Product();
        product.setName("Apples");
        product.setDescription("Fresh apples");
        product.setPrice(new BigDecimal("2.00"));
        product.setQuantityAvailable(10);
        product.setImageUrl("/images/apples.jpg");
        productRepository.save(product);

        cartService.addItem(customer.getCustomerId(), product.getProductId(), 3);
    }

    // US-12: Valid discount code is applied — returns the code object
    @Test
    void testValidDiscountCodeReturnsCode() {
        DiscountCode result = checkoutService.validateDiscountCode("SAVE10");
        assertNotNull(result, "A valid code should be returned");
        assertEquals("SAVE10", result.getCode());
    }

    // US-12: Invalid discount code returns null — no discount applied
    @Test
    void testInvalidDiscountCodeReturnsNull() {
        DiscountCode result = checkoutService.validateDiscountCode("BADCODE");
        assertNull(result, "Invalid code should return null");
    }

    // US-13: Tax on $100 with no discount is $8.25
    @Test
    void testTaxCalculationNoDiscount() {
        BigDecimal tax = checkoutService.calculateTax(new BigDecimal("100.00"), BigDecimal.ZERO);
        assertEquals(new BigDecimal("8.25"), tax, "Tax should be 8.25% of 100");
    }

    // US-13: Tax is calculated on subtotal after discount — $90 taxable = $7.43
    @Test
    void testTaxCalculationAfterDiscount() {
        BigDecimal tax = checkoutService.calculateTax(new BigDecimal("100.00"), new BigDecimal("10.00"));
        assertEquals(new BigDecimal("7.43"), tax, "Tax should be 8.25% of 90");
    }

    // US-14: Order is placed with the selected delivery option and it is saved
    @Test
    void testOrderSavesSelectedDeliveryOption() {
        Order order = checkoutService.placeOrder(
                customer.getCustomerId(),
                address.getAddressId(),
                standardDelivery.getDeliveryOptionId(),
                null);
        assertNotNull(order);
        assertEquals("Standard", order.getDeliveryOption().getType());
        assertEquals(new BigDecimal("4.99"), order.getDeliveryFee());
    }

    // US-14: Order placement fails when an invalid delivery option ID is given
    @Test
    void testInvalidDeliveryOptionThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                checkoutService.placeOrder(
                        customer.getCustomerId(),
                        address.getAddressId(),
                        9999L,
                        null));
    }

    // US-15: Order summary contains subtotal, tax, delivery fee, and total
    @Test
    void testOrderSummaryContainsAllPriceFields() {
        Order order = checkoutService.placeOrder(
                customer.getCustomerId(),
                address.getAddressId(),
                standardDelivery.getDeliveryOptionId(),
                null);
        assertTrue(order.getSubtotal().compareTo(BigDecimal.ZERO) > 0, "Subtotal should be positive");
        assertTrue(order.getTax().compareTo(BigDecimal.ZERO) > 0, "Tax should be positive");
        assertTrue(order.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0, "Delivery fee should be positive");
        assertTrue(order.getTotal().compareTo(BigDecimal.ZERO) > 0, "Total should be positive");
    }

    // US-15: Discount is reflected in the saved order when a valid code is used
    @Test
    void testOrderSummaryReflectsDiscount() {
        Order order = checkoutService.placeOrder(
                customer.getCustomerId(),
                address.getAddressId(),
                standardDelivery.getDeliveryOptionId(),
                "SAVE10");
        assertTrue(order.getDiscount().compareTo(BigDecimal.ZERO) > 0, "Discount should be applied");
    }

    // US-16: Cart is emptied after an order is placed
    @Test
    void testCartClearedAfterOrder() {
        checkoutService.placeOrder(
                customer.getCustomerId(),
                address.getAddressId(),
                standardDelivery.getDeliveryOptionId(),
                null);
        assertTrue(cartService.viewCart(customer.getCustomerId()).getItems().isEmpty(),
                "Cart should be empty after placing order");
    }

    // US-16: Placing an order with an empty cart throws an exception
    @Test
    void testEmptyCartCannotPlaceOrder() {
        cartService.clearCart(customer.getCustomerId());
        assertThrows(IllegalStateException.class, () ->
                checkoutService.placeOrder(
                        customer.getCustomerId(),
                        address.getAddressId(),
                        standardDelivery.getDeliveryOptionId(),
                        null));
    }
}
