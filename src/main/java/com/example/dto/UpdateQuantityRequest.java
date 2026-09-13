package com.example.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class UpdateQuantityRequest {
    @NotNull
    @Min(0)
    @Max(9999)
    private Integer quantity;
}
