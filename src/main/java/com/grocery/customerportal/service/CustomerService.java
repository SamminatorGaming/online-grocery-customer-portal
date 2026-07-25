package com.grocery.customerportal.service;

import com.grocery.customerportal.model.Address;
import com.grocery.customerportal.model.Customer;
import com.grocery.customerportal.repository.AddressRepository;
import com.grocery.customerportal.repository.CustomerRepository;
import com.grocery.customerportal.util.PasswordUtil;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;

    public CustomerService(CustomerRepository customerRepository, AddressRepository addressRepository) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
    }

    public Customer register(String name, String email, String password) {
        validateRequired(name, "Name");
        validateRequired(email, "Email");
        validateRequired(password, "Password");

        String cleanEmail = email.trim().toLowerCase();
        if (customerRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("That email is already in use.");
        }

        Customer customer = new Customer();
        customer.setName(name.trim());
        customer.setEmail(cleanEmail);
        customer.setPasswordHash(PasswordUtil.hash(password));
        return customerRepository.save(customer);
    }

    public Optional<Customer> login(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }

        String cleanEmail = email.trim().toLowerCase();
        return customerRepository.findByEmail(cleanEmail)
                .filter(customer -> PasswordUtil.matches(password, customer.getPasswordHash()));
    }

    public Optional<Customer> findCustomer(Long customerId) {
        if (customerId == null) {
            return Optional.empty();
        }
        return customerRepository.findById(customerId);
    }

    public List<Address> findAddresses(Long customerId) {
        return addressRepository.findByCustomerCustomerIdOrderByAddressIdAsc(customerId);
    }

    @Transactional
    public Address addAddress(Long customerId, String street, String city, String state, String zipCode) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer was not found."));

        Address address = new Address();
        fillAddress(address, street, city, state, zipCode);
        address.setCustomer(customer);
        return addressRepository.save(address);
    }

    @Transactional
    public Address updateAddress(Long customerId, Long addressId, String street, String city, String state, String zipCode) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address was not found."));

        if (!address.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Address does not belong to this customer.");
        }

        fillAddress(address, street, city, state, zipCode);
        return addressRepository.save(address);
    }

    public Optional<Address> findAddressForCustomer(Long customerId, Long addressId) {
        return addressRepository.findById(addressId)
                .filter(address -> address.getCustomer().getCustomerId().equals(customerId));
    }

    private void fillAddress(Address address, String street, String city, String state, String zipCode) {
        validateRequired(street, "Street");
        validateRequired(city, "City");
        validateRequired(state, "State");
        validateRequired(zipCode, "Zip code");

        address.setStreet(street.trim());
        address.setCity(city.trim());
        address.setState(state.trim());
        address.setZipCode(zipCode.trim());
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }
}
