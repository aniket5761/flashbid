package com.example.flashbid.user.dto;

import com.example.flashbid.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EditUserDto {
    @Size(max = 100, message = "First name must not exceed 100 characters.")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters.")
    private String lastName;

    @Email(message = "Email must be valid.")
    private String email;

    private Role role;
}
