package com.example.flashbid.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductDto {

    @NotBlank(message = "Product must have a name!")
    private String name;

    @NotNull(message = "Product must have a starting price!")
    @Positive(message = "Product starting price must be greater than 0!")
    private Long startingPrice;

    @NotNull(message = "Minimum increment is required!")
    @Positive(message = "Minimum increment must be greater than 0!")
    private Long minimumIncrement;

    @Future(message = "Starting time must be in future!")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "Ending time cannot be null!")
    @Future(message = "Ending time must be in future!")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private String description;

    @AssertTrue(message = "Ending time must be after starting time!")
    public boolean isEndTimeAfterStartTime() {
        if (startTime == null || endTime == null) {
            return true;
        }
        return endTime.isAfter(startTime);
    }
}
