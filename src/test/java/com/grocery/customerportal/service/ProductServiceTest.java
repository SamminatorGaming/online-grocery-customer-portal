package com.grocery.customerportal.service;

import com.grocery.customerportal.model.Product;
import com.grocery.customerportal.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:producttestdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductServiceTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private Product bananas;
    private Product milk;
    private Product avocado;

    @BeforeEach
    void setUp() {
        bananas = saveProduct("Bananas", "Fresh yellow bananas", "1.99", 150, "/images/bananas.jpg");
        milk = saveProduct("Whole Milk", "One gallon of whole milk", "3.49", 80, "/images/milk.jpg");
        avocado = saveProduct("Avocado", "Hass avocado, sold individually", "1.25", 0, "/images/avocado.jpg");
    }

    @Test
    void listProductsReturnsEveryProduct() {
        List<Product> result = productService.listProducts();

        assertEquals(3, result.size());
    }

    @Test
    void searchMatchesProductName() {
        List<Product> result = productService.searchProducts("banana");

        assertEquals(1, result.size());
        assertEquals("Bananas", result.get(0).getName());
    }

    @Test
    void searchMatchesProductDescription() {
        List<Product> result = productService.searchProducts("gallon");

        assertEquals(1, result.size());
        assertEquals("Whole Milk", result.get(0).getName());
    }

    @Test
    void searchWithNoMatchReturnsEmptyList() {
        List<Product> result = productService.searchProducts("not-a-real-product");

        assertTrue(result.isEmpty());
    }

    @Test
    void searchWithBlankKeywordReturnsAllProducts() {
        List<Product> result = productService.searchProducts("   ");

        assertEquals(3, result.size());
    }

    @Test
    void sortByPriceAscendingOrdersLowToHigh() {
        List<Product> result = productService.sortByPrice(true);

        assertEquals("Avocado", result.get(0).getName());
        assertEquals("Bananas", result.get(1).getName());
        assertEquals("Whole Milk", result.get(2).getName());
    }

    @Test
    void sortByPriceDescendingOrdersHighToLow() {
        List<Product> result = productService.sortByPrice(false);

        assertEquals("Whole Milk", result.get(0).getName());
        assertEquals("Bananas", result.get(1).getName());
        assertEquals("Avocado", result.get(2).getName());
    }

    @Test
    void sortByAvailabilityPutsMostStockedFirst() {
        List<Product> result = productService.sortByAvailability();

        assertEquals("Bananas", result.get(0).getName());
        assertEquals("Whole Milk", result.get(1).getName());
        assertEquals("Avocado", result.get(2).getName());
    }

    @Test
    void outOfStockProductIsNotAvailable() {
        assertFalse(avocado.isAvailable());
        assertTrue(bananas.isAvailable());
    }

    private Product saveProduct(String name, String description, String price, int quantityAvailable, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setQuantityAvailable(quantityAvailable);
        product.setImageUrl(imageUrl);
        return productRepository.save(product);
    }
}