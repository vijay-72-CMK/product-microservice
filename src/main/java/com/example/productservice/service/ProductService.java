package com.example.productservice.service;

import com.example.productservice.entity.Category;
import com.example.productservice.entity.Product;
import com.example.productservice.exception.GeneralInternalException;
import com.example.productservice.repository.ProductRepository;
import lombok.Data;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final CategoryService categoryService;
    private final ProductRepository productRepository;

    private final MongoTemplate mongoTemplate;

    public ProductService(CategoryService categoryService, ProductRepository productRepository, MongoTemplate mongoTemplate) {
        this.categoryService = categoryService;
        this.productRepository = productRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public String addProduct(Product product, String categoryName) {
        try {
            String lowerCaseCategoryName = categoryName.toLowerCase();
            Category category = categoryService.getCategoryByName(lowerCaseCategoryName);
            product.setCategoryName(lowerCaseCategoryName);

            Set<String> requiredAttributes = category.getRequiredAttributes();
            Map<String, String> productAttributes = product.getAttributes();
            if (!productAttributes.keySet().containsAll(requiredAttributes)) {
                throw new GeneralInternalException("Missing required attributes for category: " + category.getName(), HttpStatus.BAD_REQUEST);
            }
            return productRepository.save(product).getId();
        } catch (DataAccessException ex) {
            throw new GeneralInternalException("Some database error while adding product");
        }
    }

    public List<Product> getAllProducts() {
        try {
            return productRepository.findAll();
        } catch (DataAccessException ex) {
            throw new GeneralInternalException("Some database error while fetching all products");
        }
    }

    public void deleteProduct(String id) {
        try {
            if (productRepository.deleteProductById(id) == 0)
                throw new GeneralInternalException("Cannot delete product with id: " + id + " as id does not exist",
                        HttpStatus.NOT_FOUND);
        } catch (DataAccessException ex) {
            throw new GeneralInternalException("Some database error while deleting product with id: " + id);
        }
    }

    public Page<Product> searchProducts(String keyword, String category, Double minPrice, Double maxPrice, String sortBy, String sortDirection, Pageable pageable) {
        if (!isValidSortByField(sortBy)) {
            String availableSortOptions = "Available sorting options: name, price, brand, categoryName";
            throw new GeneralInternalException("Invalid sortBy value: " + sortBy + ". " + availableSortOptions, HttpStatus.BAD_REQUEST);
        }
        if (!(sortDirection.equalsIgnoreCase("asc") || sortDirection.equalsIgnoreCase("desc"))) {
            throw new GeneralInternalException("Direction must only be 'asc' or 'desc'", HttpStatus.BAD_REQUEST);
        }

        List<String> categoryArray;
        categoryArray = Arrays.stream(category.split(","))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // Build criteria list
        List<Criteria> criteriaList = new ArrayList<>();
        if (keyword != null && !keyword.isEmpty()) {
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("name").regex(keyword, "i"),
                    Criteria.where("brand").regex(keyword, "i"),
                    Criteria.where("categoryName").regex(keyword, "i"),
                    Criteria.where("description").regex(keyword, "i"),
                    Criteria.where("tags").in(keyword)
            ));
        }

        if (!categoryArray.isEmpty()) {
            criteriaList.add(Criteria.where("categoryName").in(categoryArray));
        }

        if (minPrice != null) {
            criteriaList.add(Criteria.where("price").gte(minPrice));
        }
        if (maxPrice != null) {
            criteriaList.add(Criteria.where("price").lte(maxPrice));
        }

        Criteria matchCriteria = new Criteria();
        if (!criteriaList.isEmpty()) {
            matchCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        }
        Result result = executePaged(matchCriteria, pageable);

        return new PageImpl<>(result.getProducts(), pageable, result.getTotalCount());
    }

    public Result executePaged(Criteria criteria, Pageable pageable) {
        var match = Aggregation.match(criteria);
        var facets = Aggregation
                .facet(
                        Aggregation.sort(pageable.getSort()),
                        Aggregation.skip(pageable.getOffset()),
                        Aggregation.limit(pageable.getPageSize())
                ).as("products")
                .and(
                        Aggregation.count().as("count")
                ).as("countFacet");
        var project = Aggregation
                .project("products")
                .and("$countFacet.count").arrayElementAt(0).as("totalCount");

        var aggregation = Aggregation.newAggregation(match, facets, project);
        AggregationResults<Result> aggregationResults = mongoTemplate.aggregate(aggregation, Product.class, Result.class);
        return aggregationResults.getUniqueMappedResult();
    }

    private boolean isValidSortByField(String sortBy) {
        return sortBy.equals("name") || sortBy.equals("price") || sortBy.equals("brand") || sortBy.equals("categoryName");
    }

    public Product getProductById(String id) {
        try {
            return productRepository.findById(id)
                    .orElseThrow(() -> new GeneralInternalException("id does not exist", HttpStatus.NOT_FOUND));
        } catch (DataAccessException e) {
            throw new GeneralInternalException("Some database error while getting product with id: " + id);
        }
    }

    @Data
    public static class Result {
        private int totalCount;
        private List<Product> products;
    }

    public Integer getAvailableQuantity(String productId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new GeneralInternalException("Cannot get quantity as product id: " + productId + "does not exist", HttpStatus.NOT_FOUND));
            return product.getQuantity();
        } catch (DataAccessException ex) {
            throw new GeneralInternalException("Some database error while trying to get quantity from product id: " + productId);
        }
    }
}
