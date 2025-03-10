package com.finance.cod.service;

import com.finance.cod.entity.Product;
import com.finance.cod.entity.ProductCategory;
import com.finance.cod.exception.ProductNotFoundException;
import com.finance.cod.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * ProductService is responsible for handling business logic
 * and orchestrating operations related to Product entities.
 * It provides methods to create, update, retrieve, and delete products.
 */
@Service
@RequiredArgsConstructor  // Lombok: generates a constructor for final fields (repository)
@Slf4j                   // Lombok: provides a 'log' static logger
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Creates a new product or throws an exception if the product name already exists.
     *
     * @param product The product to be created.
     * @return The newly created product, including generated ID, timestamps, etc.
     */
    @Transactional  // ensures that the operation is atomic
    public Product createProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
        if (!productRepository.findByNameIgnoreCase(product.getName()).isEmpty()) {
            log.error("Product creation failed. Name '{}' already exists.", product.getName());
            throw new RuntimeException("Product with name " + product.getName() + " already exists.");
        }

        log.info("Creating product: {}", product);
        Product savedProduct = productRepository.save(product);
        log.debug("Product created with ID: {}", savedProduct.getId());
        return savedProduct;
    }

    /**
     * Retrieves all products from the database.
     * Could be used for list pages or admin dashboards.
     *
     * @return A list of all product entities.
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        log.info("Retrieving all products...");
        List<Product> products = productRepository.findAll();
        log.debug("Number of products found: {}", products.size());
        return products;
    }

    /**
     * Retrieves a product by its ID. Throws a custom exception if not found.
     *
     * @param id The ID of the desired product.
     * @return The product with the given ID, if found.
     */
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        log.info("Fetching product with ID: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product with ID {} not found.", id);
                    return new ProductNotFoundException("Product not found for ID " + id);
                });
    }

    /**
     * Retrieves products cheaper than or equal to a given max price.
     *
     * @param maxPrice The upper bound for product price.
     * @return A list of products cheaper than or equal to maxPrice.
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByMaxPrice(BigDecimal maxPrice) {
        log.info("Fetching products with price <= {}", maxPrice);
        return productRepository.findByPriceLessThanEqual(maxPrice);
    }

    /**
     * Retrieves products by category if they are in stock.
     *
     * @param category The category string (should match an enum name).
     * @return A list of in-stock products for the given category.
     */
    @Transactional(readOnly = true)
    public List<Product> getInStockProductsByCategory(String category) {
        ProductCategory categoryEnum = ProductCategory.valueOf(category.toUpperCase());
        return productRepository.findByCategoryAndInStockTrue(categoryEnum);
    }

    /**
     * Updates an existing product's fields, including optional discount application.
     * Throws a ProductNotFoundException if the product does not exist.
     *
     * @param id The ID of the product to update.
     * @param productUpdates The updated product object containing new field values.
     * @return The updated product.
     */
    @Transactional
    public Product updateProduct(Long id, Product productUpdates) {
        log.info("Updating product with ID: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID " + id + " not found"));

        // Example: only update if the incoming value is not null or different
        Optional.ofNullable(productUpdates.getName()).ifPresent(existingProduct::setName);
        Optional.ofNullable(productUpdates.getDescription()).ifPresent(existingProduct::setDescription);
        Optional.ofNullable(productUpdates.getPrice()).ifPresent(existingProduct::setPrice);

        // If "true" or "false" is explicitly set in productUpdates
        existingProduct.setInStock(productUpdates.isInStock());

        // Apply discount logic if 'transientField' includes some keyword (just an example)
        if (productUpdates.getTransientField() != null
                && productUpdates.getTransientField().contains("DISCOUNT")) {
            // parse discount from the transient field, e.g. "DISCOUNT:0.10"
            double discountValue = parseDiscount(productUpdates.getTransientField());
            existingProduct.applyDiscount(discountValue);
            log.debug("Discount of {} applied to product ID {}", discountValue, id);
        }

        return productRepository.save(existingProduct);
    }

    /**
     * Deletes a product by its ID.
     * If the product doesn't exist, logs an error and throws a ProductNotFoundException.
     *
     * @param id The ID of the product to delete.
     */
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        if (!productRepository.existsById(id)) {
            log.error("Cannot delete. Product with ID {} not found.", id);
            throw new ProductNotFoundException("Product with ID " + id + " not found.");
        }
        productRepository.deleteById(id);
        log.debug("Product with ID {} deleted successfully.", id);
    }

    /**
     * Example method illustrating more complex logic.
     * Could parse the discount from a string, e.g. 'DISCOUNT:0.1' => 0.1
     */
    private double parseDiscount(String discountString) {
        // This is just a quick example of parsing some custom discount format from a string
        // In real usage, you'd have a safer parsing mechanism or a separate field in the request
        String[] parts = discountString.split(":");
        if (parts.length > 1 && parts[0].equalsIgnoreCase("DISCOUNT")) {
            return Double.parseDouble(parts[1]);
        }
        return 0.0;
    }
}
