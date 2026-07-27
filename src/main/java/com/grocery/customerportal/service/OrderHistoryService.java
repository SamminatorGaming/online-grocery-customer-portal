package com.grocery.customerportal.service;

import com.grocery.customerportal.model.Order;
import com.grocery.customerportal.repository.OrderRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderHistoryService {

    private final OrderRepository orderRepository;

    public OrderHistoryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // US-17: Returns all orders for a customer, newest first by default
    public List<Order> getOrders(Long customerId) {
        return orderRepository.findByCustomerCustomerIdOrderByOrderDateDesc(customerId);
    }

    // US-18: Sort by date - newest or oldest first
    public List<Order> sortByDate(Long customerId, boolean newestFirst) {
        if (newestFirst) {
            return orderRepository.findByCustomerCustomerIdOrderByOrderDateDesc(customerId);
        }
        return orderRepository.findByCustomerCustomerIdOrderByOrderDateAsc(customerId);
    }

    // US-19: Sort by total amount - highest or lowest first
    public List<Order> sortByTotal(Long customerId, boolean highestFirst) {
        if (highestFirst) {
            return orderRepository.findByCustomerCustomerIdOrderByTotalDesc(customerId);
        }
        return orderRepository.findByCustomerCustomerIdOrderByTotalAsc(customerId);
    }
}
