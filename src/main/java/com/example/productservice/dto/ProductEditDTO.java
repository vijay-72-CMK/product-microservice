package com.example.productservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductEditDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Min(value = 0, message = "Price must be non-negative")
    private double price;

    @Min(value = 0, message = "Quantity must be non-negative")
    private int availableQuantity;

    @NotBlank(message = "Description cannot be empty")
    private String description;
}
