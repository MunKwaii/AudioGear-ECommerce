package vn.edu.ute.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.dto.response.ApiResponse;
import vn.edu.ute.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@WebServlet("/admin/products/image-library")
public class AdminProductImageLibraryController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        List<String> images = new ArrayList<>();
        String rootPath = getServletContext().getRealPath("/static/images/products");
        if (rootPath != null) {
            File root = new File(rootPath);
            if (root.exists() && root.isDirectory()) {
                File[] files = root.listFiles();
                if (files != null) {
                    Arrays.stream(files)
                            .filter(File::isFile)
                            .map(File::getName)
                            .filter(this::isImageFile)
                            .forEach(name -> images.add("/images/products/" + name));
                }
            }
        }

        ApiResponse response = new ApiResponse(true, "OK", images);
        resp.getWriter().write(JsonUtil.toJson(response));
    }

    private boolean isImageFile(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".webp");
    }
}
