package vn.edu.ute.dao;

import vn.edu.ute.entity.Brand;

import java.util.List;
import java.util.Optional;

public interface BrandDao {
    Optional<Brand> findById(Long id);
    List<Brand> findAll();
}
