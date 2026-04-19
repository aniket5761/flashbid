package com.example.flashbid.user.repo;

import com.example.flashbid.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndDeletedFalse(String username);
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
