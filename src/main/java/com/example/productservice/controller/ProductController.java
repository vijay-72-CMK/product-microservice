package com.example.productservice.controller;

import com.example.productservice.dto.ProductEditDTO;
import com.example.productservice.entity.Product;
import com.example.productservice.service.ProductService;
import com.mongodb.client.model.Collation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("add-product/{categoryName}")
    public ResponseEntity<String> addProduct(@Valid @RequestBody Product product, @NotBlank @PathVariable String categoryName) {        String id = productService.addProduct(product, categoryName);
        return ResponseEntity.ok("Added product successfully! Id is: " + id);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> allProducts = productService.getAllProducts();
        return ResponseEntity.ok(allProducts);
    }
    @DeleteMapping("/remove-product/{id}")
    public ResponseEntity<String> removeProduct(@PathVariable @NotBlank String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Deleted successfully!");
    }

//    @GetMapping
//    public ResponseEntity<List<Product>> filterProducts(
//            @RequestParam
//    )

    @GetMapping("/search")
    public Page<Product> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "") String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer page,
            @RequestParam(defaultValue = "10") @Positive Integer size,
            @RequestParam(required = false, defaultValue = "") String boardSize,
            @RequestParam(required = false, defaultValue = "") String brand,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {


        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        System.out.println(pageable.getSort());
        return productService.searchProducts(keyword, category, minPrice, maxPrice, sortBy, sortDirection, boardSize, brand, pageable);
    }

    @GetMapping("/get-quantity/{productId}")
    public Integer getAvailableQuantity(@NotBlank @PathVariable String productId) {
        return productService.getAvailableQuantity(productId);
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable @NotBlank String id) {
        return productService.getProductById(id);
    }


    @PatchMapping("/edit-product/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable @NotBlank  String id,
                                                 @RequestBody @Validated ProductEditDTO productData) {
        productService.updateProduct(id, productData);
        return ResponseEntity.ok("Updated successfully!");
    }

}
