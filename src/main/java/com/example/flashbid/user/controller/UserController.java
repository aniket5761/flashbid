package com.example.flashbid.user.controller;

import com.example.flashbid.user.dto.EditUserDto;
import com.example.flashbid.user.dto.UserDto;
import com.example.flashbid.user.service.UserService;
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

    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> editUser(@PathVariable("userId") Long userId,
                                           @RequestBody EditUserDto editUserDto) {
        UserDto userDto = userService.editUser(userId, editUserDto);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        UserDto userDto = userService.getUser(id);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<UserDto>> getAllUsers(@RequestParam Optional<Integer> page,
                                                    @RequestParam Optional<String> sortBy,
                                                    @RequestParam Optional<String> username) {
        return ResponseEntity.ok(userService.getAllUsers(page, sortBy, username));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        String message = userService.deleteUser(id);
        return ResponseEntity.ok(message);
    }
}
