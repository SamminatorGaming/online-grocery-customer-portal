package com.grocery.customerportal.repository;

import com.grocery.customerportal.model.DeliveryOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryOptionRepository extends JpaRepository<DeliveryOption, Long> {
}
