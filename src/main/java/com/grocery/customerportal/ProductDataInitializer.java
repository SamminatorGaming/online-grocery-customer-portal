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
                20,
                "/images/apples.jpg"
        ));

        productRepository.save(createProduct(
                "Bread",
                "White sandwich bread",
                "3.25",
                15,
                "/images/bread.jpg"
        ));

        productRepository.save(createProduct(
                "Milk",
                "One gallon of whole milk",
                "4.50",
                10,
                "/images/milk.jpg"
        ));

        productRepository.save(createProduct(
                "Eggs",
                "One dozen large eggs",
                "3.99",
                12,
                "/images/eggs.jpg"
        ));

        productRepository.save(createProduct(
                "Bananas",
                "Fresh bananas",
                "0.79",
                30,
                "/images/bananas.jpg"
        ));

        productRepository.save(createProduct(
                "Coffee",
                "Medium roast ground coffee, 12oz bag",
                "8.99",
                8,
                "/images/coffee.jpg"
        ));

        productRepository.save(createProduct(
                "Avocado",
                "Hass avocado, sold individually",
                "1.25",
                0,
                "/images/avocado.jpg"
        ));
    }

    private Product createProduct(
            String name,
            String description,
            String price,
            int quantityAvailable,
            String imageUrl) {

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setQuantityAvailable(quantityAvailable);
        product.setImageUrl(imageUrl);

        return product;
    }
}