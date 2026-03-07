package com.example.flashbid.common.util;

import com.example.flashbid.common.exception.ResourceNotFoundException;
import com.example.flashbid.common.exception.UserNotFoundException;
import com.example.flashbid.product.entity.Product;
import com.example.flashbid.product.repo.ProductRepo;
import com.example.flashbid.user.entity.User;
import com.example.flashbid.user.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EntityFetcher {

    private final UserRepo userRepo;
    private final ProductRepo productRepo;

    public User getUserById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public Product getProductById(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public User findUserByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    public User getCurrentUser() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return findUserByUsername(currentUsername);
    }

    public boolean existsByUsername(String username) {
        return userRepo.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }
}
