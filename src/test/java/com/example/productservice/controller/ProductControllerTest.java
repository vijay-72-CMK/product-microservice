package com.example.productservice.controller;

import com.example.productservice.entity.Product;
import com.example.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testAddProduct_Successful() throws Exception {
        Product product = createValidProduct();
        String categoryName = "TestCategory";
        String productId = "productId";
        when(productService.addProduct(any(Product.class), any(String.class))).thenReturn(productId);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(productController).build();

        mockMvc.perform(post("/api/products/add-product/{categoryName}", categoryName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(content().string("Added product successfully! Id is: " + productId));
    }

    private Product createValidProduct() {
        Product product = new Product();
        product.setName("ValidProductName");
        product.setBrand("ValidBrand");
        product.setPrice(20.0);
        product.setAvailableQuantity(10);
        product.setImages(new ArrayList<>(Arrays.asList("image1.jpg", "image2.jpg")));
        product.setCategoryName("TestCategory");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("brand", "ValidBrand");
        attributes.put("price", "20.0");
        product.setAttributes(attributes);
        product.setDescription("Product description");
        product.setTags(new HashSet<>(Arrays.asList("tag1", "tag2")));
        product.setAverageRating(4.5);
        product.setBoardSize("Large");
        return product;
    }
}