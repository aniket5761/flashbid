package com.example.flashbid.user.controller;

import com.example.flashbid.user.dto.EditUserDto;
import com.example.flashbid.user.dto.AdminUserActionDto;
import com.example.flashbid.user.dto.UserDto;
import com.example.flashbid.user.entity.Role;
import com.example.flashbid.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> editUser(@PathVariable("userId") Long userId,
                                           @Valid @RequestBody EditUserDto editUserDto) {
        UserDto userDto = userService.editUser(userId, editUserDto);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        UserDto userDto = userService.getUser(id);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(@RequestParam Optional<Integer> page,
                                                    @RequestParam Optional<String> sortBy,
                                                    @RequestParam Optional<String> username,
                                                    @RequestParam Optional<Role> role,
                                                    @RequestParam Optional<Boolean> sellerRequested,
                                                    @RequestParam Optional<Boolean> banned) {
        return ResponseEntity.ok(userService.getAllUsers(page, sortBy, username, role, sellerRequested, banned));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','SELLER','ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        String message = userService.deleteUser(id);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/me/seller-request")
    public ResponseEntity<UserDto> requestSellerAccess() {
        return ResponseEntity.ok(userService.requestSellerAccess());
    }

    @PatchMapping("/{id}/seller-approval")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> setSellerApproval(@PathVariable Long id, @RequestBody AdminUserActionDto actionDto) {
        return ResponseEntity.ok(userService.setSellerApproval(id, actionDto));
    }

    @PatchMapping("/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> setBanStatus(@PathVariable Long id, @RequestBody AdminUserActionDto actionDto) {
        return ResponseEntity.ok(userService.setBanStatus(id, actionDto));
    }
}
