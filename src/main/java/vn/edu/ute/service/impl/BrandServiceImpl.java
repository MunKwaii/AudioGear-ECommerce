package vn.edu.ute.service.impl;

import vn.edu.ute.dao.BrandDao;
import vn.edu.ute.dao.impl.BrandDaoImpl;
import vn.edu.ute.dto.BrandDTO;
import vn.edu.ute.entity.Brand;
import vn.edu.ute.service.BrandService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BrandServiceImpl implements BrandService {

    private final BrandDao brandDao = BrandDaoImpl.getInstance();

    @Override
    public List<BrandDTO> getAllBrands() {
        List<Brand> brands = brandDao.findAll();
        return brands.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BrandDTO getBrandById(Long id) {
        return brandDao.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    @Override
    public BrandDTO createBrand(String name, String description, String logoUrl) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thương hiệu không được để trống");
        }

        Optional<Brand> existing = brandDao.findByName(name.trim());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Tên thương hiệu đã tồn tại");
        }

        Brand brand = new Brand();
        brand.setName(name.trim());
        brand.setDescription(description);
        brand.setLogoUrl(logoUrl);

        Brand saved = brandDao.save(brand);
        Brand reloaded = brandDao.findById(saved.getId()).orElse(saved);
        return toDTO(reloaded);
    }

    @Override
    public BrandDTO updateBrand(Long id, String name, String description, String logoUrl) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thương hiệu không được để trống");
        }

        Brand brand = brandDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thương hiệu"));

        Optional<Brand> existing = brandDao.findByName(name.trim());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new IllegalArgumentException("Tên thương hiệu đã tồn tại");
        }

        brand.setName(name.trim());
        brand.setDescription(description);
        brand.setLogoUrl(logoUrl);

        Brand updated = brandDao.update(brand);
        Brand reloaded = brandDao.findById(updated.getId()).orElse(updated);
        return toDTO(reloaded);
    }

    @Override
    public void deleteBrand(Long id) {
        Brand brand = brandDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thương hiệu"));

        long productCount = brandDao.countProductsByBrandId(id);
        if (productCount > 0) {
            throw new IllegalStateException("Không thể xóa thương hiệu đang có sản phẩm");
        }

        brandDao.delete(id);
    }

    private BrandDTO toDTO(Brand entity) {
        long productCount = brandDao.countProductsByBrandId(entity.getId());
        return new BrandDTO.Builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .logoUrl(entity.getLogoUrl())
                .productCount(productCount)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
