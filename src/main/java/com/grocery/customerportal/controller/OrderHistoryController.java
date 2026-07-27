package com.grocery.customerportal.controller;

import com.grocery.customerportal.model.Order;
import com.grocery.customerportal.repository.OrderRepository;
import com.grocery.customerportal.service.OrderHistoryService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/orders")
public class OrderHistoryController {

    private final OrderHistoryService orderHistoryService;
    private final OrderRepository orderRepository;

    public OrderHistoryController(OrderHistoryService orderHistoryService,
                                  OrderRepository orderRepository) {
        this.orderHistoryService = orderHistoryService;
        this.orderRepository = orderRepository;
    }

    private Long getCustomerId(HttpSession session) {
        Object id = session.getAttribute("customerId");
        return id instanceof Long ? (Long) id : null;
    }

    // US-17, US-18, US-19: View and sort order history
    @GetMapping
    public String orderHistory(@RequestParam(required = false) String sort,
                               HttpSession session, Model model) {
        Long customerId = getCustomerId(session);
        if (customerId == null) return "redirect:/login";

        List<Order> orders;
        if ("date_asc".equals(sort)) {
            orders = orderHistoryService.sortByDate(customerId, false);
        } else if ("total_desc".equals(sort)) {
            orders = orderHistoryService.sortByTotal(customerId, true);
        } else if ("total_asc".equals(sort)) {
            orders = orderHistoryService.sortByTotal(customerId, false);
        } else {
            orders = orderHistoryService.getOrders(customerId);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("sort", sort);
        return "order-history";
    }

    // Order confirmation page shown after a successful order
    @GetMapping("/{orderId}")
    public String orderConfirmation(@PathVariable Long orderId,
                                    HttpSession session, Model model) {
        Long customerId = getCustomerId(session);
        if (customerId == null) return "redirect:/login";

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getCustomer().getCustomerId().equals(customerId)) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        return "order-confirmation";
    }
}
