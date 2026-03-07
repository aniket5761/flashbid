package com.example.flashbid.user.service;

import com.example.flashbid.common.exception.UserAccessDeniedException;
import com.example.flashbid.common.util.EntityFetcher;
import com.example.flashbid.user.dto.EditUserDto;
import com.example.flashbid.user.dto.UserDto;
import com.example.flashbid.user.entity.Role;
import com.example.flashbid.user.entity.User;
import com.example.flashbid.user.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final EntityFetcher entityFetcher;

    public UserDto getUser(Long userId) {
        User user = entityFetcher.getUserById(userId);
        return mapToDto(user);
    }

    public Page<UserDto> getAllUsers(Optional<Integer> page, Optional<String> sortBy, Optional<String> username) {
        PageRequest pageRequest = PageRequest.of(
                page.orElse(0),
                12,
                Sort.Direction.ASC,
                sortBy.orElse("username")
        );

        Page<User> users;
        if (username.isPresent()) {
            users = userRepo.findByUsernameContainingIgnoreCase(username.get(), pageRequest);
        } else {
            users = userRepo.findAll(pageRequest);
        }

        return users.map(this::mapToDto);
    }

    public UserDto editUser(Long userId, EditUserDto editUserDto) {
        User currentUser = entityFetcher.getCurrentUser();
        User userToEdit = entityFetcher.getUserById(userId);

        if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(userId)) {
            throw new UserAccessDeniedException("You do not have permission to edit this user.");
        }

        if (currentUser.getRole().equals(Role.ADMIN)) {
            userToEdit.setRole(editUserDto.getRole());
        }
        userToEdit.setFirstName(editUserDto.getFirstName());
        userToEdit.setLastName(editUserDto.getLastName());

        return mapToDto(userRepo.save(userToEdit));
    }

    public String deleteUser(Long id) {
        User currentUser = entityFetcher.getCurrentUser();
        User userToDelete = entityFetcher.getUserById(id);

        if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(id)) {
            throw new UserAccessDeniedException("You do not have permission to delete this user.");
        }

        userRepo.delete(userToDelete);
        return "User deleted successfully.";
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .registrationDate(user.getRegistrationDate())
                .build();
    }
}
