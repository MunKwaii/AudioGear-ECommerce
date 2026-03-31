package vn.edu.ute.homepage.facade;

import vn.edu.ute.dto.PageDTO;
import vn.edu.ute.dto.ProductDTO;

public interface ProductFacadeService {
    /**
     * Tìm kiếm và phân trang sản phẩm theo từ khóa, danh mục.
     */
    PageDTO<ProductDTO> searchAndPaginate(String keyword, Long categoryId, int page, int pageSize);
}
