package vn.edu.ute.service;

import vn.edu.ute.dto.request.CreateProductRequest;
import vn.edu.ute.entity.Brand;
import vn.edu.ute.entity.Category;
import vn.edu.ute.entity.Product;

import java.util.List;

public interface ProductService {
    Product createProduct(CreateProductRequest request);
    List<Category> getAllCategories();
    List<Brand> getAllBrands();
}
