package com.grocery.customerportal.service;

import com.grocery.customerportal.model.Address;
import com.grocery.customerportal.model.CartItem;
import com.grocery.customerportal.model.DeliveryOption;
import com.grocery.customerportal.model.DiscountCode;
import com.grocery.customerportal.model.Order;
import com.grocery.customerportal.model.OrderItem;
import com.grocery.customerportal.model.ShoppingCart;
import com.grocery.customerportal.repository.AddressRepository;
import com.grocery.customerportal.repository.CustomerRepository;
import com.grocery.customerportal.repository.DeliveryOptionRepository;
import com.grocery.customerportal.repository.DiscountCodeRepository;
import com.grocery.customerportal.repository.OrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.0825");

    private final OrderRepository orderRepository;
    private final DiscountCodeRepository discountCodeRepository;
    private final DeliveryOptionRepository deliveryOptionRepository;
    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;
    private final ShoppingCartService cartService;

    public CheckoutService(
            OrderRepository orderRepository,
            DiscountCodeRepository discountCodeRepository,
            DeliveryOptionRepository deliveryOptionRepository,
            AddressRepository addressRepository,
            CustomerRepository customerRepository,
            ShoppingCartService cartService) {
        this.orderRepository = orderRepository;
        this.discountCodeRepository = discountCodeRepository;
        this.deliveryOptionRepository = deliveryOptionRepository;
        this.addressRepository = addressRepository;
        this.customerRepository = customerRepository;
        this.cartService = cartService;
    }

    // US-12: Returns the DiscountCode if the code string is valid and active, null otherwise
    public DiscountCode validateDiscountCode(String code) {
        if (code == null || code.isBlank()) return null;
        return discountCodeRepository.findByCodeAndActiveTrue(code.trim().toUpperCase()).orElse(null);
    }

    // US-13: Tax is 8.25% of (subtotal - discount), rounded to 2 decimal places
    public BigDecimal calculateTax(BigDecimal subtotal, BigDecimal discount) {
        BigDecimal taxable = subtotal.subtract(discount).max(BigDecimal.ZERO);
        return taxable.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public List<DeliveryOption> getAllDeliveryOptions() {
        return deliveryOptionRepository.findAll();
    }

    public List<Address> getAddressesForCustomer(Long customerId) {
        return addressRepository.findByCustomerCustomerIdOrderByAddressIdAsc(customerId);
    }

    // US-16: Creates and saves the order, then clears the cart
    @Transactional
    public Order placeOrder(Long customerId, Long addressId,
                            Long deliveryOptionId, String discountCodeStr) {

        ShoppingCart cart = cartService.viewCart(customerId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place an order with an empty cart.");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found."));

        if (!address.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Address does not belong to this customer.");
        }

        DeliveryOption delivery = deliveryOptionRepository.findById(deliveryOptionId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery option not found."));

        DiscountCode discountCode = validateDiscountCode(discountCodeStr);

        BigDecimal subtotal = cart.calculateSubtotal();
        BigDecimal discount = (discountCode != null)
                ? discountCode.applyDiscount(subtotal)
                : BigDecimal.ZERO;
        BigDecimal tax = calculateTax(subtotal, discount);
        BigDecimal deliveryFee = delivery.getFee();
        BigDecimal total = subtotal.subtract(discount).add(tax).add(deliveryFee)
                .setScale(2, RoundingMode.HALF_UP);

        Order order = new Order();
        order.setCustomer(customerRepository.findById(customerId).orElseThrow());
        order.setAddress(address);
        order.setDeliveryOption(delivery);
        order.setDiscountCode(discountCode);
        order.setOrderDate(LocalDate.now());
        order.setStatus("PLACED");
        order.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        order.setDiscount(discount.setScale(2, RoundingMode.HALF_UP));
        order.setTax(tax);
        order.setDeliveryFee(deliveryFee);
        order.setTotal(total);

        Order saved = orderRepository.save(order);

        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(saved);
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setItemPrice(ci.getItemPrice());
            saved.getOrderItems().add(oi);
        }
        orderRepository.save(saved);

        cartService.clearCart(customerId);
        return saved;
    }
}
