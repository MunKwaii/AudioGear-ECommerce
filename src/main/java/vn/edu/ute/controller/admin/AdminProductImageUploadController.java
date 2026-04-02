package vn.edu.ute.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import vn.edu.ute.dto.response.ApiResponse;
import vn.edu.ute.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@WebServlet("/admin/products/upload-images")
@MultipartConfig
public class AdminProductImageUploadController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        List<String> stored = new ArrayList<>();
        String rootPath = "/home/okarin/Documents/Arch_Programming/AudioGear-ECommerce";
        String srcPath = rootPath + File.separator + "src/main/webapp/static/images/products";
        String deployPath = getServletContext().getRealPath("/static/images/products");
        
        File srcDir = new File(srcPath);
        if (!srcDir.exists()) srcDir.mkdirs();
        
        File deployDir = new File(deployPath);
        if (deployPath != null && !deployDir.exists()) deployDir.mkdirs();


        for (Part part : req.getParts()) {
            if (!"images".equals(part.getName()) || part.getSize() == 0) {
                continue;
            }

            String submitted = Paths.get(part.getSubmittedFileName()).getFileName().toString();
            if (!isImageFile(submitted)) {
                continue;
            }

            String extension = getExtension(submitted);
            String filename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
            
            // 1. Save to src directory (for persistence)
            File srcFile = new File(srcDir, filename);
            try (java.io.InputStream input = part.getInputStream()) {
                Files.copy(input, srcFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            
            // 2. Also save to deploy directory (for immediate display)
            if (deployPath != null && !srcPath.equals(deployPath)) {
                File deployFile = new File(deployDir, filename);
                Files.copy(srcFile.toPath(), deployFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            
            stored.add("/static/images/products/" + filename);
        }

        ApiResponse response = new ApiResponse(true, "OK", stored);
        resp.getWriter().write(JsonUtil.toJson(response));
    }

    private boolean isImageFile(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif")
                || lower.endsWith(".webp");
    }

    private String getExtension(String name) {
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == name.length() - 1) {
            return "";
        }
        return name.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
