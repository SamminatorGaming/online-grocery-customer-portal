package com.grocery.customerportal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;

@Entity
public class DeliveryOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deliveryOptionId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(nullable = false)
    private String estimatedTime;

    public Long getDeliveryOptionId() { return deliveryOptionId; }
    public void setDeliveryOptionId(Long deliveryOptionId) { this.deliveryOptionId = deliveryOptionId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }
    public String getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }
}
