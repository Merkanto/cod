package com.finance.cod.repository;

import com.finance.cod.entity.Product;
import com.finance.cod.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameIgnoreCase(String name);

    List<Product> findByCategoryAndInStockTrue(ProductCategory category);

    List<Product> findByPriceLessThanEqual(BigDecimal maxPrice);

}
