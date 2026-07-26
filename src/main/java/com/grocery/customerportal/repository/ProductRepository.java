package com.grocery.customerportal.repository;

import com.grocery.customerportal.model.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByOrderByNameAsc();
}
