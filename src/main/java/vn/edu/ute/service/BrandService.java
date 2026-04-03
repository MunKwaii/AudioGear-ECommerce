package vn.edu.ute.service;

import vn.edu.ute.dto.BrandDTO;

import java.util.List;

public interface BrandService {
    List<BrandDTO> getAllBrands();
    BrandDTO getBrandById(Long id);
    BrandDTO createBrand(String name, String description, String logoUrl);
    BrandDTO updateBrand(Long id, String name, String description, String logoUrl);
    void deleteBrand(Long id);
}
