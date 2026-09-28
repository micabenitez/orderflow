package com.mb.orderflow.product.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    @Column(nullable = false)
    private int stock;

    protected Product(){}

    public Product(String name, BigDecimal price, int initialStock){
        this.name = Objects.requireNonNull(name, "El nombre es obligatorio");

        if(price == null || price.compareTo(BigDecimal.ZERO) <= 0 ) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo");
        }

        if(initialStock < 0) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo");
        }
        this.price = price;
        this.stock = initialStock;
    }

    public Product(UUID id, String name, BigDecimal price, int initialStock) {
        this(name, price, initialStock);
        this.id = id;
    }

    public void reduceStock(int quantity) {
        if(quantity <= 0) throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a cero");
        if(this.stock < quantity) throw new InsufficientStockException("Stock insuficiente para el producto: " + this.name);
        this.stock -= quantity;
    }

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad a agregar debe ser mayor a cero");
        }
        this.stock += quantity;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
}
