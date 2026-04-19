package com.example.flashbid.product.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditProductDto {
    @Size(max = 255, message = "Product name must not exceed 255 characters.")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters.")
    private String description;
}
