package com.example.flashbid.product.service;

import com.example.flashbid.common.exception.UserAccessDeniedException;
import com.example.flashbid.common.util.EntityFetcher;
import com.example.flashbid.product.dto.CreateProductDto;
import com.example.flashbid.product.dto.EditProductDto;
import com.example.flashbid.product.dto.ProductDto;
import com.example.flashbid.product.entity.Product;
import com.example.flashbid.product.entity.ProductStatus;
import com.example.flashbid.product.repo.ProductRepo;
import com.example.flashbid.user.dto.UserDto;
import com.example.flashbid.user.entity.Role;
import com.example.flashbid.user.entity.User;
import com.example.flashbid.user.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepo productRepo;
    private final EntityFetcher entityFetcher;
    private final UserRepo userRepo;

    @CacheEvict(value = "products", allEntries = true)
    public ProductDto addProduct(CreateProductDto createProductDto) {
        User currentUser = entityFetcher.getCurrentUser();

        Product product = new Product();
        product.setName(createProductDto.getName());
        product.setStartingPrice(createProductDto.getStartingPrice());
        product.setStartTime(createProductDto.getStartTime());
        product.setEndTime(createProductDto.getEndTime());
        product.setDescription(createProductDto.getDescription());
        product.setUser(currentUser);
        product.setProductStatus(ProductStatus.SCHEDULED);

        return mapToDto(productRepo.save(product));
    }

    @Cacheable(value = "products", key = "#productId")
    public ProductDto getProductById(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        return mapToDto(product);
    }

    @Cacheable(value = "products", key = "{#page, #sortBy, #sortDir, #name, #productStatus}")
    public Page<ProductDto> getAllProducts(Optional<Integer> page, Optional<String> sortBy, 
                                          Optional<String> sortDir,
                                          Optional<String> name, Optional<ProductStatus> productStatus) {
        Sort.Direction direction = sortDir.orElse("desc").equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        PageRequest pageRequest = PageRequest.of(
                page.orElse(0),
                12,
                direction,
                sortBy.orElse("createdAt")
        );

        Page<Product> products;
        if (name.isPresent() && productStatus.isPresent()) {
            products = productRepo.findByNameContainingIgnoreCaseAndProductStatus(name.get(), productStatus.get(), pageRequest);
        } else if (name.isPresent()) {
            products = productRepo.findByNameContainingIgnoreCase(name.get(), pageRequest);
        } else if (productStatus.isPresent()) {
            products = productRepo.findByProductStatus(productStatus.get(), pageRequest);
        } else {
            products = productRepo.findAll(pageRequest);
        }

        return products.map(this::mapToDto);
    }

    public List<ProductDto> getProductsByUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return productRepo.findByUser(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductDto editProduct(Long id, EditProductDto editProductDto) {
        User currentUser = entityFetcher.getCurrentUser();
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (!currentUser.getRole().equals(Role.ADMIN) && !product.getUser().getId().equals(currentUser.getId())) {
            throw new UserAccessDeniedException("You do not have permission to edit this product.");
        }

        if (editProductDto.getName() != null) product.setName(editProductDto.getName());
        if (editProductDto.getDescription() != null) product.setDescription(editProductDto.getDescription());

        return mapToDto(productRepo.save(product));
    }

    @CacheEvict(value = "products", allEntries = true)
    public String deleteProduct(Long productId) {
        User currentUser = entityFetcher.getCurrentUser();
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (!currentUser.getRole().equals(Role.ADMIN) && !product.getUser().getId().equals(currentUser.getId())) {
            throw new UserAccessDeniedException("You do not have permission to delete this product.");
        }

        productRepo.delete(product);
        return "Product deleted successfully.";
    }

    private ProductDto mapToDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .startingPrice(product.getStartingPrice())
                .startTime(product.getStartTime())
                .endTime(product.getEndTime())
                .description(product.getDescription())
                .createdAt(product.getCreatedAt())
                .productStatus(product.getProductStatus())
                .user(UserDto.builder()
                        .id(product.getUser().getId())
                        .username(product.getUser().getUsername())
                        .firstName(product.getUser().getFirstName())
                        .lastName(product.getUser().getLastName())
                        .email(product.getUser().getEmail())
                        .role(product.getUser().getRole())
                        .registrationDate(product.getUser().getRegistrationDate())
                        .build())
                .build();
    }
}
