package com.rithickmart.controller;

import com.rithickmart.model.Product;
import com.rithickmart.repo.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository products;

    public ProductController(ProductRepository products) {
        this.products = products;
    }

    @GetMapping
    public List<Product> all(
            @RequestParam(required = false) String q) {

        if (q == null || q.isBlank()) {
            return products.findAll();
        }

        return products
                .findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(q, q);
    }

    @GetMapping("/{id}")
    public Product one(@PathVariable Long id) {
        return products.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Product not found"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Product add(@Valid @RequestBody Product product) {
        return products.save(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Product update(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {

        Product old = one(id);

        old.setName(product.getName());
        old.setCategory(product.getCategory());
        old.setPrice(product.getPrice());
        old.setStock(product.getStock());
        old.setImage(product.getImage());
        old.setDescription(product.getDescription());

        return products.save(old);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> delete(@PathVariable Long id) {
        products.delete(one(id));

        return Map.of("message", "Product removed");
    }
}