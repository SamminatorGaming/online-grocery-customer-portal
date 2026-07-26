package com.grocery.customerportal.service;

import com.grocery.customerportal.model.Product;
import com.grocery.customerportal.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> listProducts() {
        return productRepository.findAllByOrderByNameAsc();
    }

    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return listProducts();
        }
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }

    public List<Product> sortByPrice(boolean ascending) {
        return ascending ? productRepository.findAllByOrderByPriceAsc()
                : productRepository.findAllByOrderByPriceDesc();
    }

    public List<Product> sortByAvailability() {
        return productRepository.findAllByOrderByQuantityAvailableDesc();
    }
}
