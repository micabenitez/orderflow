package com.mb.orderflow.infrastructure.config;

import com.mb.orderflow.product.domain.Product;
import com.mb.orderflow.product.domain.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final ProductRepository productRepository;

    public DataSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            log.info("Iniciando sembrado de productos de prueba...");

            List<Product> initialProducts = List.of(
                    new Product("Teclado Mecánico RGB", new BigDecimal("120.00"), 15),
                    new Product("Mouse Inalámbrico Pro", new BigDecimal("75.50"), 20),
                    new Product("Monitor 27 Pulgadas 144Hz", new BigDecimal("350.00"), 8),
                    new Product("Auriculares Cancelación de Ruido", new BigDecimal("180.00"), 5)
            );

            List<Product> savedProducts = productRepository.saveAll(initialProducts);

            log.info("Productos sembrados exitosamente:");
            savedProducts.forEach(p ->
                    log.info("ID: {} | Nombre: {} | Precio: ${} | Stock: {}",
                            p.getId(), p.getName(), p.getPrice(), p.getStock())
            );
        } else {
            log.info("La base de datos ya contiene productos. Omitiendo sembrado.");
        }
    }
}