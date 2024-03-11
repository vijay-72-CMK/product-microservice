package com.example.productservice.service;

import com.example.productservice.controller.ProductController;
import com.example.productservice.entity.Category;
import com.example.productservice.entity.Product;
import com.example.productservice.exception.GeneralInternalException;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    public void testAddProduct_Successful() {
        Product product = createValidProduct();
        String categoryName = "TestCategory";

        Category category = new Category();
        category.setName(categoryName);
        Set<String> requiredAttributes = new HashSet<>(Arrays.asList("brand", "price"));
        category.setRequiredAttributes(requiredAttributes);

        when(categoryService.getCategoryByName(anyString())).thenReturn(category);

        Product savedProduct = new Product();
        savedProduct.setId("productId");
        when(productRepository.save(any())).thenReturn(savedProduct);

        String id = productService.addProduct(product, categoryName);

        assertNotNull(id);
        assertEquals("productId", id);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    public void testAddProduct_CategoryNotFound() {
        Product product = createValidProduct();
        String categoryName = "TestCategory";

        when(categoryService.getCategoryByName(categoryName.toLowerCase())) // Lowercase conversion
                .thenThrow(new GeneralInternalException("Category name not found", HttpStatus.NOT_FOUND));

        assertThrows(GeneralInternalException.class, () -> productService.addProduct(product, categoryName),
                "Category name not found");
        verify(productRepository, never()).save(any());
    }

    @Test
    public void testAddProduct_MissingRequiredAttributes() {
        Product product = createValidProduct();
        String categoryName = "TestCategory";

        Map<String, String> attributes = product.getAttributes();
        attributes.remove("brand");

        Category category = new Category();
        category.setName(categoryName);
        Set<String> requiredAttributes = new HashSet<>(Arrays.asList("brand", "price"));
        category.setRequiredAttributes(requiredAttributes);

        when(categoryService.getCategoryByName(categoryName.toLowerCase())).thenReturn(category);

        assertThrows(GeneralInternalException.class, () -> productService.addProduct(product, categoryName),
                "Missing required attributes for category: " + category.getName());

        verify(productRepository, never()).save(any());
    }

    @Test
    public void testAddProduct_DatabaseError() {
        Product product = createValidProduct();
        String categoryName = "TestCategory";

        Category category = new Category();
        category.setName(categoryName);
        Set<String> requiredAttributes = new HashSet<>(Arrays.asList("brand", "price"));
        category.setRequiredAttributes(requiredAttributes);

        when(categoryService.getCategoryByName(categoryName.toLowerCase())).thenReturn(category);

        when(productRepository.save(product)).thenThrow(new DataAccessException("DB Error") {
        });

        assertThrows(GeneralInternalException.class, () -> productService.addProduct(product, categoryName),
                "Some database error while adding product");

        verify(productRepository, times(1)).save(product);
    }

    @Test
    public void testGetAllProducts_Successful() {
        List<Product> expectedProducts = Arrays.asList(
                createValidProduct(),
                createValidProduct()
        );

        when(productRepository.findAll()).thenReturn(expectedProducts);

        List<Product> actualProducts = productService.getAllProducts();

        assertNotNull(actualProducts);
        assertEquals(expectedProducts.size(), actualProducts.size());

        for (int i = 0; i < expectedProducts.size(); i++) {
            Product expected = expectedProducts.get(i);
            Product actual = actualProducts.get(i);

            assertEquals(expected.getName(), actual.getName());
            assertEquals(expected.getBrand(), actual.getBrand());
            assertEquals(expected.getPrice(), actual.getPrice());
            assertEquals(expected.getAvailableQuantity(), actual.getAvailableQuantity());
            assertEquals(expected.getImages(), actual.getImages());
            assertEquals(expected.getCategoryName(), actual.getCategoryName());
            assertEquals(expected.getAttributes(), actual.getAttributes());
            assertEquals(expected.getDescription(), actual.getDescription());
            assertEquals(expected.getTags(), actual.getTags());
            assertEquals(expected.getAverageRating(), actual.getAverageRating());
            assertEquals(expected.getBoardSize(), actual.getBoardSize());
        }


        verify(productRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllProducts_DataAccessError() {
        when(productRepository.findAll()).thenThrow(new DataAccessException("Simulated database error") {
        });

        assertThrows(GeneralInternalException.class, () -> productService.getAllProducts());

        verify(productRepository, times(1)).findAll();
    }

    @Test
    public void testDeleteProduct_Success() {
        String productId = "testId";
        when(productRepository.deleteProductById(productId)).thenReturn(1L);

        productService.deleteProduct(productId);

        verify(productRepository, times(1)).deleteProductById(productId);
    }

    @Test
    public void testDeleteProduct_ProductNotFound() {
        String productId = "nonExistentId";
        when(productRepository.deleteProductById(productId)).thenReturn(0L);

        GeneralInternalException exception = assertThrows(GeneralInternalException.class,
                () -> productService.deleteProduct(productId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(productRepository, times(1)).deleteProductById(productId);
        verify(productRepository, times(1)).deleteProductById(productId);
    }

    @Test
    public void testDeleteProduct_DataAccessError() {
        String productId = "someId";
        when(productRepository.deleteProductById(productId)).thenThrow(new DataAccessException("Database issue") {
        });

        GeneralInternalException exception = assertThrows(GeneralInternalException.class,
                () -> productService.deleteProduct(productId));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        verify(productRepository, times(1)).deleteProductById(productId);
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