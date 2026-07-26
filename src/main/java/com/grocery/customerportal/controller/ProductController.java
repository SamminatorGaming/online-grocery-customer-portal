package com.grocery.customerportal.controller;

import com.grocery.customerportal.model.Product;
import com.grocery.customerportal.service.ProductService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public String listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            Model model) {

        List<Product> products;
        if (keyword != null && !keyword.isBlank()) {
            products = productService.searchProducts(keyword);
        } else if ("price_asc".equals(sort)) {
            products = productService.sortByPrice(true);
        } else if ("price_desc".equals(sort)) {
            products = productService.sortByPrice(false);
        } else if ("availability".equals(sort)) {
            products = productService.sortByAvailability();
        } else {
            products = productService.listProducts();
        }

        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("sort", sort == null ? "" : sort);
        return "products";
    }
}
