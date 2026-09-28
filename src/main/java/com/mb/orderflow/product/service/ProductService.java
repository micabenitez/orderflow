package com.mb.orderflow.product.service;

import com.mb.orderflow.product.api.dto.ProductResponse;
import com.mb.orderflow.product.domain.Product;
import com.mb.orderflow.product.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        List<Product> productList = productRepository.findAll();

        return productList.stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }
}
