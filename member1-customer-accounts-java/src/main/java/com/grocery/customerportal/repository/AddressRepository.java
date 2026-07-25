package com.grocery.customerportal.repository;

import com.grocery.customerportal.model.Address;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomerCustomerIdOrderByAddressIdAsc(Long customerId);
}
