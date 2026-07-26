package com.grocery.customerportal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;

@Entity
public class DiscountCode {

    @Id
    @Column(nullable = false, unique = true)
    private String code;

    // "PERCENTAGE" or "FIXED"
    @Column(nullable = false)
    private String discountType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(nullable = false)
    private boolean active;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // Returns the discount amount to subtract from the subtotal
    public BigDecimal applyDiscount(BigDecimal subtotal) {
        if ("PERCENTAGE".equals(discountType)) {
            return subtotal.multiply(discountValue).divide(BigDecimal.valueOf(100));
        }
        return discountValue.min(subtotal);
    }
}
