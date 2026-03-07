package com.example.flashbid.user.dto;

import com.example.flashbid.user.entity.Role;
import lombok.Data;

@Data
public class EditUserDto {
    private String firstName;
    private String lastName;
    private Role role;
}
