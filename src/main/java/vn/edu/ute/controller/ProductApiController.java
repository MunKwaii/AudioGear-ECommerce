package vn.edu.ute.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.dto.response.ApiResponse;
import vn.edu.ute.dto.response.ProductDetailDTO;
import vn.edu.ute.entity.Product;
import vn.edu.ute.service.ProductService;
import vn.edu.ute.service.impl.ProductServiceImpl;
import vn.edu.ute.util.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet(name = "ProductApiController", urlPatterns = {"/api/v1/products/*"})
public class ProductApiController extends HttpServlet {

    private ProductService productService;

    @Override
    public void init() throws ServletException {
        this.productService = new ProductServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID sản phẩm");
            return;
        }

        String[] parts = pathInfo.split("/");
        
        try {
            Long productId = Long.parseLong(parts[1]);
            
            // GET /api/v1/products/{id}/related
            if (parts.length > 2 && parts[2].equals("related")) {
                handleGetRelatedProducts(productId, response);
                return;
            }
            
            // GET /api/v1/products/{id}
            handleGetProductDetail(productId, response);
            
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "ID sản phẩm không hợp lệ");
        }
    }

    private void handleGetProductDetail(Long productId, HttpServletResponse response) throws IOException {
        Optional<Product> productOpt = productService.getProductById(productId);
        
        if (productOpt.isPresent()) {
            // Sử dụng Lambda/Stream (trong ProductDetailDTO.fromEntity)
            ProductDetailDTO dto = ProductDetailDTO.fromEntity(productOpt.get());
            sendSuccess(response, "Lấy thông tin sản phẩm thành công", dto);
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy sản phẩm");
        }
    }

    private void handleGetRelatedProducts(Long productId, HttpServletResponse response) throws IOException {
        Optional<Product> productOpt = productService.getProductById(productId);
        
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            Long categoryId = product.getCategory().getId();
            
            List<Product> relatedProducts = productService.getRelatedProducts(categoryId, productId, 4);
            
            // Sử dụng Java Stream và Lambda để chuyển sang ProductDTO thay vì ProductDetailDTO (tránh Lazy Loading hình ảnh phụ)
            List<vn.edu.ute.dto.ProductDTO> dtos = relatedProducts.stream()
                    .map(p -> new vn.edu.ute.dto.ProductDTO(
                            p.getId(),
                            p.getName(),
                            p.getPrice(),
                            p.getThumbnailUrl(),
                            p.getCategory() != null ? p.getCategory().getName() : null,
                            p.getBrand() != null ? p.getBrand().getName() : null,
                            p.getStockQuantity()
                    ))
                    .collect(Collectors.toList());
            
            sendSuccess(response, "Lấy danh sách sản phẩm liên quan thành công", dtos);
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy sản phẩm gốc");
        }
    }

    private void sendSuccess(HttpServletResponse response, String message, Object data) throws IOException {
        ApiResponse apiResponse = new ApiResponse(true, message, data);
        response.getWriter().write(JsonUtil.toJson(apiResponse));
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        ApiResponse apiResponse = new ApiResponse(false, message, null);
        response.getWriter().write(JsonUtil.toJson(apiResponse));
    }
}
