package com.finance.cod.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    private boolean inStock;

    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    private ProductCategory category; // REQUIRES your "ProductCategory" enum

    @Transient
    private String transientField;

    // Lifecycle callback (optional)
    @PrePersist
    public void onPrePersist() {
        this.createdDate = LocalDateTime.now();
    }

    public BigDecimal applyDiscount(double discountPercentage) {
        if (discountPercentage < 0 || discountPercentage > 1) {
            throw new IllegalArgumentException("Discount must be between 0 and 1");
        }
        BigDecimal discountFactor = BigDecimal.valueOf(discountPercentage);
        BigDecimal discountAmount = this.price.multiply(discountFactor);
        this.price = this.price.subtract(discountAmount);
        return this.price;
    }
}
