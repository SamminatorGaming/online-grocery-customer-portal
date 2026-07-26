package com.grocery.customerportal;

import com.grocery.customerportal.model.Product;
import com.grocery.customerportal.repository.ProductRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ProductDataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    public ProductDataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        productRepository.save(createProduct(
                "Apples",
                "Fresh red apples",
                "1.50",
                20
        ));

        productRepository.save(createProduct(
                "Bread",
                "White sandwich bread",
                "3.25",
                15
        ));

        productRepository.save(createProduct(
                "Milk",
                "One gallon of whole milk",
                "4.50",
                10
        ));

        productRepository.save(createProduct(
                "Eggs",
                "One dozen large eggs",
                "3.99",
                12
        ));

        productRepository.save(createProduct(
                "Bananas",
                "Fresh bananas",
                "0.79",
                30
        ));
    }

    private Product createProduct(
            String name,
            String description,
            String price,
            int quantityAvailable) {

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setQuantityAvailable(quantityAvailable);
        product.setImageUrl("");

        return product;
    }
}