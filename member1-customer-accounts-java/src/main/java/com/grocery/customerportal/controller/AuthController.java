package com.grocery.customerportal.controller;

import com.grocery.customerportal.model.Customer;
import com.grocery.customerportal.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final CustomerService customerService;

    public AuthController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        return getLoggedInCustomerId(session) == null ? "redirect:/login" : "redirect:/profile";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {
        try {
            customerService.register(name, email, password);
            model.addAttribute("success", "Account created. Please log in.");
            return "login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        Optional<Customer> customer = customerService.login(email, password);
        if (customer.isEmpty()) {
            model.addAttribute("error", "Email or password is incorrect.");
            model.addAttribute("email", email);
            return "login";
        }

        session.setAttribute("customerId", customer.get().getCustomerId());
        return "redirect:/profile";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private Long getLoggedInCustomerId(HttpSession session) {
        Object customerId = session.getAttribute("customerId");
        return customerId instanceof Long ? (Long) customerId : null;
    }
}
