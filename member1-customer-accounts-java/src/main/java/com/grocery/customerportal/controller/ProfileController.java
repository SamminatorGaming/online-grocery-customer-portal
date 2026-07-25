package com.grocery.customerportal.controller;

import com.grocery.customerportal.model.Address;
import com.grocery.customerportal.model.Customer;
import com.grocery.customerportal.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProfileController {
    private final CustomerService customerService;

    public ProfileController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Long customerId = getLoggedInCustomerId(session);
        if (customerId == null) {
            return "redirect:/login";
        }

        Customer customer = customerService.findCustomer(customerId)
                .orElseThrow(() -> new IllegalStateException("Customer was not found."));
        List<Address> addresses = customerService.findAddresses(customerId);

        model.addAttribute("customer", customer);
        model.addAttribute("addresses", addresses);
        return "profile";
    }

    @GetMapping("/addresses/new")
    public String showNewAddressForm(HttpSession session, Model model) {
        if (getLoggedInCustomerId(session) == null) {
            return "redirect:/login";
        }

        model.addAttribute("formTitle", "Add address");
        model.addAttribute("formAction", "/addresses");
        model.addAttribute("address", new Address());
        return "address-form";
    }

    @PostMapping("/addresses")
    public String addAddress(
            @RequestParam String street,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String zipCode,
            HttpSession session,
            Model model) {
        Long customerId = getLoggedInCustomerId(session);
        if (customerId == null) {
            return "redirect:/login";
        }

        try {
            customerService.addAddress(customerId, street, city, state, zipCode);
            return "redirect:/profile";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("formTitle", "Add address");
            model.addAttribute("formAction", "/addresses");
            model.addAttribute("address", formAddress(street, city, state, zipCode));
            return "address-form";
        }
    }

    @GetMapping("/addresses/{addressId}/edit")
    public String showEditAddressForm(
            @PathVariable Long addressId,
            HttpSession session,
            Model model) {
        Long customerId = getLoggedInCustomerId(session);
        if (customerId == null) {
            return "redirect:/login";
        }

        Address address = customerService.findAddressForCustomer(customerId, addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address was not found."));

        model.addAttribute("formTitle", "Edit address");
        model.addAttribute("formAction", "/addresses/" + addressId + "/edit");
        model.addAttribute("address", address);
        return "address-form";
    }

    @PostMapping("/addresses/{addressId}/edit")
    public String editAddress(
            @PathVariable Long addressId,
            @RequestParam String street,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String zipCode,
            HttpSession session,
            Model model) {
        Long customerId = getLoggedInCustomerId(session);
        if (customerId == null) {
            return "redirect:/login";
        }

        try {
            customerService.updateAddress(customerId, addressId, street, city, state, zipCode);
            return "redirect:/profile";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("formTitle", "Edit address");
            model.addAttribute("formAction", "/addresses/" + addressId + "/edit");
            model.addAttribute("address", formAddress(street, city, state, zipCode));
            return "address-form";
        }
    }

    private Long getLoggedInCustomerId(HttpSession session) {
        Object customerId = session.getAttribute("customerId");
        return customerId instanceof Long ? (Long) customerId : null;
    }

    private Address formAddress(String street, String city, String state, String zipCode) {
        Address address = new Address();
        address.setStreet(street);
        address.setCity(city);
        address.setState(state);
        address.setZipCode(zipCode);
        return address;
    }
}
