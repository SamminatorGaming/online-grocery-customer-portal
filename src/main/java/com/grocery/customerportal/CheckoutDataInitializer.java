package com.grocery.customerportal;

import com.grocery.customerportal.model.DeliveryOption;
import com.grocery.customerportal.model.DiscountCode;
import com.grocery.customerportal.repository.DeliveryOptionRepository;
import com.grocery.customerportal.repository.DiscountCodeRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// Seeds delivery options and discount codes on first startup
@Component
@Profile("!test")
public class CheckoutDataInitializer implements CommandLineRunner {

    private final DeliveryOptionRepository deliveryOptionRepository;
    private final DiscountCodeRepository discountCodeRepository;

    public CheckoutDataInitializer(
            DeliveryOptionRepository deliveryOptionRepository,
            DiscountCodeRepository discountCodeRepository) {
        this.deliveryOptionRepository = deliveryOptionRepository;
        this.discountCodeRepository = discountCodeRepository;
    }

    @Override
    public void run(String... args) {
        seedDeliveryOptions();
        seedDiscountCodes();
    }

    private void seedDeliveryOptions() {
        if (deliveryOptionRepository.count() > 0) return;

        deliveryOptionRepository.save(createDeliveryOption("Standard", "4.99", "5-7 business days"));
        deliveryOptionRepository.save(createDeliveryOption("Express", "9.99", "2-3 business days"));
        deliveryOptionRepository.save(createDeliveryOption("Overnight", "19.99", "Next business day"));
    }

    private void seedDiscountCodes() {
        if (discountCodeRepository.count() > 0) return;

        discountCodeRepository.save(createDiscount("SAVE10", "PERCENTAGE", "10", true));
        discountCodeRepository.save(createDiscount("GROCERY5", "FIXED", "5", true));
        discountCodeRepository.save(createDiscount("NEWUSER", "PERCENTAGE", "15", true));
    }

    private DeliveryOption createDeliveryOption(String type, String fee, String estimatedTime) {
        DeliveryOption option = new DeliveryOption();
        option.setType(type);
        option.setFee(new BigDecimal(fee));
        option.setEstimatedTime(estimatedTime);
        return option;
    }

    private DiscountCode createDiscount(String code, String type, String value, boolean active) {
        DiscountCode dc = new DiscountCode();
        dc.setCode(code);
        dc.setDiscountType(type);
        dc.setDiscountValue(new BigDecimal(value));
        dc.setActive(active);
        return dc;
    }
}
