package com.grocery.customerportal.controller;

import com.grocery.customerportal.model.ShoppingCart;
import com.grocery.customerportal.service.ShoppingCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        Long customerId = loggedInCustomerId(session);
        if (customerId == null) {
            return "redirect:/login";
        }

        ShoppingCart cart = shoppingCartService.viewCart(customerId);
        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", cart.calculateSubtotal());
        model.addAttribute("products", shoppingCartService.listProducts());
        return "cart";
    }

    @PostMapping("/cart/items")
    public String addItem(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long customerId = loggedInCustomerId(session);
        if (customerId == null) {
            return "redirect:/login";
        }

        try {
            shoppingCartService.addItem(customerId, productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Item added to cart.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/items/{cartItemId}/quantity")
    public String updateQuantity(
            @PathVariable Long cartItemId,
            @RequestParam int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long customerId = loggedInCustomerId(session);
        if (customerId == null) {
            return "redirect:/login";
        }

        try {
            shoppingCartService.updateQuantity(customerId, cartItemId, quantity);
            redirectAttributes.addFlashAttribute("success", "Quantity updated.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/items/{cartItemId}/remove")
    public String removeItem(
            @PathVariable Long cartItemId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long customerId = loggedInCustomerId(session);
        if (customerId == null) {
            return "redirect:/login";
        }

        try {
            shoppingCartService.removeItem(customerId, cartItemId);
            redirectAttributes.addFlashAttribute("success", "Item removed from cart.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        Long customerId = loggedInCustomerId(session);
        if (customerId == null) {
            return "redirect:/login";
        }
        shoppingCartService.clearCart(customerId);
        redirectAttributes.addFlashAttribute("success", "Cart cleared.");
        return "redirect:/cart";
    }

    private Long loggedInCustomerId(HttpSession session) {
        Object customerId = session.getAttribute("customerId");
        return customerId instanceof Long ? (Long) customerId : null;
    }
}