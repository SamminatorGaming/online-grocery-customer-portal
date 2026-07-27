package com.grocery.customerportal.service;

import com.grocery.customerportal.model.Address;
import com.grocery.customerportal.model.Customer;
import com.grocery.customerportal.model.DeliveryOption;
import com.grocery.customerportal.model.Order;
import com.grocery.customerportal.model.Product;
import com.grocery.customerportal.repository.AddressRepository;
import com.grocery.customerportal.repository.DeliveryOptionRepository;
import com.grocery.customerportal.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:historytestdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderHistoryServiceTest {

    @Autowired private OrderHistoryService orderHistoryService;
    @Autowired private CheckoutService checkoutService;
    @Autowired private CustomerService customerService;
    @Autowired private ShoppingCartService cartService;
    @Autowired private ProductRepository productRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private DeliveryOptionRepository deliveryOptionRepository;

    private Customer customer;
    private Address address;
    private Long deliveryId;

    @BeforeEach
    void setup() {
        customer = customerService.register("History User", "history@test.com", "password");

        address = new Address();
        address.setStreet("200 Oak Ave");
        address.setCity("Austin");
        address.setState("TX");
        address.setZipCode("73301");
        address.setCustomer(customer);
        addressRepository.save(address);

        DeliveryOption delivery = new DeliveryOption();
        delivery.setType("Standard");
        delivery.setFee(new BigDecimal("4.99"));
        delivery.setEstimatedTime("5-7 business days");
        deliveryOptionRepository.save(delivery);
        deliveryId = delivery.getDeliveryOptionId();
    }

    private void placeOrderWithPrice(String price) {
        Product product = new Product();
        product.setName("Item");
        product.setDescription("Test item");
        product.setPrice(new BigDecimal(price));
        product.setQuantityAvailable(20);
        product.setImageUrl("/images/apples.jpg");
        productRepository.save(product);
        cartService.addItem(customer.getCustomerId(), product.getProductId(), 1);
        checkoutService.placeOrder(customer.getCustomerId(), address.getAddressId(), deliveryId, null);
    }

    // US-17: Customer with placed orders sees them all in history
    @Test
    void testViewOrderHistory() {
        placeOrderWithPrice("5.00");
        placeOrderWithPrice("15.00");
        List<Order> orders = orderHistoryService.getOrders(customer.getCustomerId());
        assertEquals(2, orders.size(), "Both orders should appear in history");
    }

    // US-17: Customer with no orders sees an empty history
    @Test
    void testEmptyOrderHistory() {
        List<Order> orders = orderHistoryService.getOrders(customer.getCustomerId());
        assertTrue(orders.isEmpty(), "New customer should have empty order history");
    }

    // US-18: Sort by newest date — most recent order is first
    @Test
    void testSortByNewestFirst() {
        placeOrderWithPrice("5.00");
        placeOrderWithPrice("15.00");
        List<Order> orders = orderHistoryService.sortByDate(customer.getCustomerId(), true);
        for (int i = 0; i < orders.size() - 1; i++) {
            assertTrue(!orders.get(i).getOrderDate().isBefore(orders.get(i + 1).getOrderDate()),
                    "Newer orders should come first");
        }
    }

    // US-18: Sort by oldest date — earliest order is first
    @Test
    void testSortByOldestFirst() {
        placeOrderWithPrice("5.00");
        placeOrderWithPrice("15.00");
        List<Order> orders = orderHistoryService.sortByDate(customer.getCustomerId(), false);
        for (int i = 0; i < orders.size() - 1; i++) {
            assertTrue(!orders.get(i).getOrderDate().isAfter(orders.get(i + 1).getOrderDate()),
                    "Older orders should come first");
        }
    }

    // US-19: Sort by highest total — most expensive order is first
    @Test
    void testSortByHighestTotal() {
        placeOrderWithPrice("2.00");
        placeOrderWithPrice("20.00");
        List<Order> orders = orderHistoryService.sortByTotal(customer.getCustomerId(), true);
        for (int i = 0; i < orders.size() - 1; i++) {
            assertTrue(orders.get(i).getTotal().compareTo(orders.get(i + 1).getTotal()) >= 0,
                    "Higher totals should come first");
        }
    }

    // US-19: Sort by lowest total — cheapest order is first
    @Test
    void testSortByLowestTotal() {
        placeOrderWithPrice("2.00");
        placeOrderWithPrice("20.00");
        List<Order> orders = orderHistoryService.sortByTotal(customer.getCustomerId(), false);
        for (int i = 0; i < orders.size() - 1; i++) {
            assertTrue(orders.get(i).getTotal().compareTo(orders.get(i + 1).getTotal()) <= 0,
                    "Lower totals should come first");
        }
    }
}
