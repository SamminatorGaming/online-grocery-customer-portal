package com.grocery.customerportal.repository;

import com.grocery.customerportal.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerCustomerIdOrderByOrderDateDesc(Long customerId);
    List<Order> findByCustomerCustomerIdOrderByOrderDateAsc(Long customerId);
    List<Order> findByCustomerCustomerIdOrderByTotalDesc(Long customerId);
    List<Order> findByCustomerCustomerIdOrderByTotalAsc(Long customerId);
}
