package com.example.flashbid.product.controller;

import com.example.flashbid.product.dto.CreateProductDto;
import com.example.flashbid.product.dto.EditProductDto;
import com.example.flashbid.product.dto.ProductDto;
import com.example.flashbid.product.entity.ProductStatus;
import com.example.flashbid.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @PostMapping
    public ResponseEntity<ProductDto> addProduct(@Valid @RequestBody CreateProductDto createProductDto) {
        return ResponseEntity.ok(productService.addProduct(createProductDto));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long productId){
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAllProducts(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<String> sortBy,
            @RequestParam Optional<String> sortDir,
            @RequestParam Optional<String> name,
            @RequestParam Optional<ProductStatus> productStatus) {

        return ResponseEntity.ok(productService.getAllProducts(page, sortBy, sortDir, name, productStatus));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProductDto>> getProductByUser(@PathVariable Long userId){
        return ResponseEntity.ok(productService.getProductsByUser(userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<ProductDto> editProduct(@PathVariable("id") Long id,
                                           @Valid @RequestBody EditProductDto editProductDto) {
        return ResponseEntity.ok(productService.editProduct(id, editProductDto));
    }

    @PreAuthorize("hasAnyRole('USER','SELLER','ADMIN')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(productService.deleteProduct(productId));
    }
}
