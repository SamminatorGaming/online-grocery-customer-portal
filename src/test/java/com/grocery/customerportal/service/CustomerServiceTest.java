package com.grocery.customerportal.service;

import com.grocery.customerportal.model.Address;
import com.grocery.customerportal.model.Customer;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CustomerServiceTest {
    @Autowired
    private CustomerService customerService;

    @Test
    void registerCreatesCustomer() {
        Customer customer = customerService.register("Sam Green", "sam@example.com", "pass123");

        assertEquals("Sam Green", customer.getName());
        assertEquals("sam@example.com", customer.getEmail());
        assertFalse(customer.getPasswordHash().isBlank());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        customerService.register("Sam Green", "sam@example.com", "pass123");

        assertThrows(IllegalArgumentException.class, () ->
                customerService.register("Other User", "sam@example.com", "pass456"));
    }

    @Test
    void loginAcceptsCorrectPassword() {
        Customer customer = customerService.register("Sam Green", "sam@example.com", "pass123");

        Optional<Customer> loginResult = customerService.login("sam@example.com", "pass123");

        assertTrue(loginResult.isPresent());
        assertEquals(customer.getCustomerId(), loginResult.get().getCustomerId());
    }

    @Test
    void loginRejectsWrongPassword() {
        customerService.register("Sam Green", "sam@example.com", "pass123");

        Optional<Customer> loginResult = customerService.login("sam@example.com", "wrong");

        assertTrue(loginResult.isEmpty());
    }

    @Test
    void addAddressSavesAddressForCustomer() {
        Customer customer = customerService.register("Sam Green", "sam@example.com", "pass123");

        customerService.addAddress(customer.getCustomerId(), "100 Main St", "San Antonio", "TX", "78249");
        List<Address> addresses = customerService.findAddresses(customer.getCustomerId());

        assertEquals(1, addresses.size());
        assertEquals("100 Main St", addresses.get(0).getStreet());
    }

    @Test
    void addAddressRejectsMissingZipCode() {
        Customer customer = customerService.register("Sam Green", "sam@example.com", "pass123");

        assertThrows(IllegalArgumentException.class, () ->
                customerService.addAddress(customer.getCustomerId(), "100 Main St", "San Antonio", "TX", ""));
    }
}
