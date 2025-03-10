package com.finance.cod.controller;

import com.finance.cod.entity.Product;
import com.finance.cod.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * ProductController manages REST endpoints for Product operations.
 * It delegates business logic to the ProductService.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated  // Optional: Enables JSR-303/JSR-380 validation on parameters if using @Valid
public class ProductController {

    private final ProductService productService;

    /**
     * Retrieves a list of all products.
     * GET /api/products
     */
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * Retrieves a product by its ID.
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Creates a new product.
     * POST /api/products
     * Example JSON body:
     * {
     *   "name": "Smartphone",
     *   "description": "A brand new smartphone",
     *   "price": 699.99,
     *   "inStock": true,
     *   "category": "ELECTRONICS"
     * }
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product created = productService.createProduct(product);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Updates an existing product by its ID.
     * PUT /api/products/{id}
     * {
     *   "price": 599.99,
     *   "inStock": false,
     *   "transientField": "DISCOUNT:0.10"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product productUpdates
    ) {
        Product updated = productService.updateProduct(id, productUpdates);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a product by its ID.
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * Retrieves products cheaper than or equal to a given max price.
     * GET /api/products/price?max=1000
     */
    @GetMapping("/price")
    public ResponseEntity<List<Product>> getProductsByMaxPrice(@RequestParam("max") BigDecimal maxPrice) {
        List<Product> products = productService.getProductsByMaxPrice(maxPrice);
        return ResponseEntity.ok(products);
    }

    /**
     * Retrieves in-stock products for a given category.
     * GET /api/products/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getInStockByCategory(@PathVariable String category) {
        List<Product> products = productService.getInStockProductsByCategory(category);
        return ResponseEntity.ok(products);
    }
}
