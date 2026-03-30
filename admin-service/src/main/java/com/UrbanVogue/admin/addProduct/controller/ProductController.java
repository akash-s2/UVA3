package com.UrbanVogue.admin.addProduct.controller;

import com.UrbanVogue.admin.addProduct.dto.ProductRequestDTO;
import com.UrbanVogue.admin.addProduct.dto.ProductResponseDTO;
import com.UrbanVogue.admin.addProduct.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // product add route we can hit from here
    @PostMapping("/add")
    public ProductResponseDTO addProduct(@RequestBody ProductRequestDTO request) {
        return productService.addProduct(request);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }


}