package vn.edu.ute.dao;

import vn.edu.ute.entity.Product;

import java.util.Optional;

public interface ProductDao {
    Product save(Product product);
    Optional<Product> findById(Long id);
}
