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
import java.util.*;

@WebServlet("/admin/products/image-library")
public class AdminProductImageLibraryController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        List<String> images = new ArrayList<>();
        String staticPath = "/static/images/products";
        List<File> roots = new ArrayList<>();

        // 1. Get deployment path (target)
        String deployPath = getServletContext().getRealPath(staticPath);
        if (deployPath != null) {
            File deployRoot = new File(deployPath);
            roots.add(deployRoot);

            // 2. Try to find 'src' by looking for 'target' folder and going up
            File current = deployRoot;
            while (current != null) {
                if (current.getName().equals("target")) {
                    File projectRoot = current.getParentFile();
                    if (projectRoot != null) {
                        File srcRoot = new File(projectRoot, "src/main/webapp" + staticPath.replace("/", File.separator));
                        if (!roots.contains(srcRoot)) {
                            // Add src to roots
                            roots.add(srcRoot);
                        }
                    }
                    break;
                }
                current = current.getParentFile();
            }
        }

        // 3. Fallback: Check user.dir if src was not found via target
        if (roots.size() < 2) {
            String userDir = System.getProperty("user.dir");
            File projectRoot = new File(userDir);
            while (projectRoot != null && !new File(projectRoot, "pom.xml").exists() && !new File(projectRoot, "src").exists()) {
                projectRoot = projectRoot.getParentFile();
            }

            if (projectRoot != null) {
                File srcRoot = new File(projectRoot, "src/main/webapp" + staticPath.replace("/", File.separator));
                if (!roots.contains(srcRoot)) {
                    roots.add(srcRoot);
                }
            }
        }

        Set<String> imageNames = new TreeSet<>();
        for (File root : roots) {
            if (root.exists() && root.isDirectory()) {
                File[] files = root.listFiles();
                if (files != null) {
                    Arrays.stream(files)
                            .filter(File::isFile)
                            .map(File::getName)
                            .filter(this::isImageFile)
                            .forEach(imageNames::add);
                }
            }
        }

        imageNames.forEach(name -> images.add(staticPath + "/" + name));

        ApiResponse response = new ApiResponse(true, "OK", images);
        resp.getWriter().write(JsonUtil.toJson(response));
    }

    private boolean isImageFile(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".webp");
    }
}
