package com.UrbanVogue.admin.addProduct.service;

import com.UrbanVogue.admin.Inventory.entity.Inventory;
import com.UrbanVogue.admin.Inventory.repository.InventoryRepository;
import com.UrbanVogue.admin.addProduct.dto.ProductRequestDTO;
import com.UrbanVogue.admin.addProduct.dto.ProductResponseDTO;
import com.UrbanVogue.admin.addProduct.entity.Product;
import com.UrbanVogue.admin.addProduct.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    public ProductResponseDTO addProduct(ProductRequestDTO request) {

        // DTO → Entity
        Product product = new Product();
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setCategory(request.getCategory());
        product.setSize(request.getSize());
        product.setColor(request.getColor());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setDescription(request.getDescription());
       // product.setNumberOfPieces(request.getNumberOfPieces());
        // Save to DB
        Product savedProduct = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProductId(savedProduct.getId());
        inventory.setNumberOfPieces(Math.toIntExact(request.getNumberOfPieces())); // jo request me aaya

        inventoryRepository.save(inventory);

        // Entity → Response DTO
        return new ProductResponseDTO(
                savedProduct.getId(),
                savedProduct.getName(),
                "Product added successfully"
        );
    }

    public String deleteProduct(Long id) {

        // 1. Check if product exists
        if (!productRepository.existsById(id)) {
            return "Product not found";
        }

        // 2. Delete from Inventory FIRST (important)
        Inventory inventory = inventoryRepository.findByProductId(id);
        if (inventory != null) {
            inventoryRepository.delete(inventory);
        }

        // 3. Delete from Product table
        productRepository.deleteById(id);

        return "Product deleted successfully";
    }

}