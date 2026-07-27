package com.grocery.customerportal.controller;

import com.grocery.customerportal.model.Address;
import com.grocery.customerportal.model.DeliveryOption;
import com.grocery.customerportal.model.DiscountCode;
import com.grocery.customerportal.model.Order;
import com.grocery.customerportal.model.ShoppingCart;
import com.grocery.customerportal.service.CheckoutService;
import com.grocery.customerportal.service.ShoppingCartService;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final ShoppingCartService cartService;

    public CheckoutController(CheckoutService checkoutService,
                              ShoppingCartService cartService) {
        this.checkoutService = checkoutService;
        this.cartService = cartService;
    }

    private Long getCustomerId(HttpSession session) {
        Object id = session.getAttribute("customerId");
        return id instanceof Long ? (Long) id : null;
    }

    @GetMapping
    public String checkoutPage(HttpSession session, Model model) {
        Long customerId = getCustomerId(session);
        if (customerId == null) return "redirect:/login";

        ShoppingCart cart = cartService.viewCart(customerId);
        if (cart.getItems().isEmpty()) return "redirect:/cart";

        List<Address> addresses = checkoutService.getAddressesForCustomer(customerId);
        List<DeliveryOption> deliveryOptions = checkoutService.getAllDeliveryOptions();

        BigDecimal subtotal = cart.calculateSubtotal();
        String appliedCode = (String) session.getAttribute("discountCode");
        DiscountCode dc = checkoutService.validateDiscountCode(appliedCode);
        BigDecimal discount = (dc != null) ? dc.applyDiscount(subtotal) : BigDecimal.ZERO;
        BigDecimal tax = checkoutService.calculateTax(subtotal, discount);

        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", discount);
        model.addAttribute("tax", tax);
        model.addAttribute("addresses", addresses);
        model.addAttribute("deliveryOptions", deliveryOptions);
        model.addAttribute("appliedCode", appliedCode);
        return "checkout";
    }

    @PostMapping("/apply-discount")
    public String applyDiscount(@RequestParam String discountCode,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        DiscountCode dc = checkoutService.validateDiscountCode(discountCode);
        if (dc == null) {
            redirectAttributes.addFlashAttribute("discountError", "Invalid or expired discount code.");
        } else {
            session.setAttribute("discountCode", discountCode.trim().toUpperCase());
            redirectAttributes.addFlashAttribute("discountSuccess", "Discount applied successfully.");
        }
        return "redirect:/checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder(@RequestParam Long addressId,
                             @RequestParam Long deliveryOptionId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Long customerId = getCustomerId(session);
        if (customerId == null) return "redirect:/login";

        try {
            String discountCode = (String) session.getAttribute("discountCode");
            Order order = checkoutService.placeOrder(customerId, addressId, deliveryOptionId, discountCode);
            session.removeAttribute("discountCode");
            return "redirect:/orders/" + order.getOrderId();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/checkout";
        }
    }
}
