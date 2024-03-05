package com.example.productservice.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;


import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Data
@Document("products")
public class Product {
    @Id
    private String id;

    @NotBlank
    @Indexed
    private String name;

    @NotBlank
    @Indexed
    private String brand;

    @NotNull
    private Double price;

    @NotNull
    private Integer availableQuantity;

    private ArrayList<String> images;

    @Indexed
    private String categoryName;

    private Map<String, String> attributes;

    private String description;

    @Indexed
    private Set<String> tags;

    private Double averageRating;

    @CreatedDate
    private Date createdAt;
}
