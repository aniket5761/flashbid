package com.example.flashbid.user.service;

import com.example.flashbid.common.exception.UserAccessDeniedException;
import com.example.flashbid.common.util.EntityFetcher;
import jakarta.persistence.criteria.Predicate;
import com.example.flashbid.user.dto.EditUserDto;
import com.example.flashbid.user.dto.AdminUserActionDto;
import com.example.flashbid.user.dto.UserDto;
import com.example.flashbid.user.entity.Role;
import com.example.flashbid.user.entity.User;
import com.example.flashbid.user.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final EntityFetcher entityFetcher;

    public UserDto getUser(Long userId) {
        User currentUser = entityFetcher.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(userId)) {
            throw new UserAccessDeniedException("You do not have permission to view this user.");
        }
        User user = entityFetcher.getUserById(userId);
        return mapToDto(user);
    }

    public UserDto getCurrentUserProfile() {
        return mapToDto(entityFetcher.getCurrentUser());
    }

    public Page<UserDto> getAllUsers(Optional<Integer> page, Optional<String> sortBy, Optional<String> username,
                                     Optional<Role> role, Optional<Boolean> sellerRequested, Optional<Boolean> banned) {
        PageRequest pageRequest = PageRequest.of(
                page.orElse(0),
                12,
                Sort.Direction.ASC,
                sortBy.orElse("username")
        );

        Specification<User> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));
            username.ifPresent(value -> predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + value.toLowerCase() + "%")));
            role.ifPresent(value -> predicates.add(criteriaBuilder.equal(root.get("role"), value)));
            sellerRequested.ifPresent(value -> predicates.add(criteriaBuilder.equal(root.get("sellerRequested"), value)));
            banned.ifPresent(value -> predicates.add(criteriaBuilder.equal(root.get("banned"), value)));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> users = userRepo.findAll(specification, pageRequest);
        return users.map(this::mapToDto);
    }

    public UserDto editUser(Long userId, EditUserDto editUserDto) {
        User currentUser = entityFetcher.getCurrentUser();
        User userToEdit = entityFetcher.getUserById(userId);

        if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(userId)) {
            throw new UserAccessDeniedException("You do not have permission to edit this user.");
        }

        if (editUserDto.getEmail() != null && !editUserDto.getEmail().equalsIgnoreCase(userToEdit.getEmail()) && userRepo.existsByEmail(editUserDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (currentUser.getRole().equals(Role.ADMIN) && editUserDto.getRole() != null) {
            userToEdit.setRole(editUserDto.getRole());
        }
        if (editUserDto.getFirstName() != null) {
            userToEdit.setFirstName(editUserDto.getFirstName());
        }
        if (editUserDto.getLastName() != null) {
            userToEdit.setLastName(editUserDto.getLastName());
        }
        if (editUserDto.getEmail() != null) {
            userToEdit.setEmail(editUserDto.getEmail());
        }

        return mapToDto(userRepo.save(userToEdit));
    }

    public String deleteUser(Long id) {
        User currentUser = entityFetcher.getCurrentUser();
        User userToDelete = entityFetcher.getUserById(id);

        if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(id)) {
            throw new UserAccessDeniedException("You do not have permission to delete this user.");
        }

        userToDelete.setDeleted(true);
        userToDelete.setBanned(true);
        userToDelete.setSellerRequested(false);
        userRepo.save(userToDelete);
        return "User deleted successfully.";
    }

    public UserDto requestSellerAccess() {
        User currentUser = entityFetcher.getCurrentUser();
        if (currentUser.getRole() == Role.SELLER || currentUser.getRole() == Role.ADMIN) {
            throw new UserAccessDeniedException("This account already has seller access.");
        }
        currentUser.setSellerRequested(true);
        return mapToDto(userRepo.save(currentUser));
    }

    public UserDto setSellerApproval(Long userId, AdminUserActionDto actionDto) {
        User user = entityFetcher.getUserById(userId);
        if (actionDto.isValue()) {
            user.setRole(Role.SELLER);
            user.setSellerRequested(false);
        } else {
            user.setRole(Role.USER);
            user.setSellerRequested(false);
        }
        return mapToDto(userRepo.save(user));
    }

    public UserDto setBanStatus(Long userId, AdminUserActionDto actionDto) {
        User currentUser = entityFetcher.getCurrentUser();
        User user = entityFetcher.getUserById(userId);
        if (currentUser.getId().equals(userId)) {
            throw new UserAccessDeniedException("You cannot ban your own account.");
        }
        user.setBanned(actionDto.isValue());
        return mapToDto(userRepo.save(user));
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
                .sellerRequested(user.isSellerRequested())
                .banned(user.isBanned())
                .deleted(user.isDeleted())
                .build();
    }

}
