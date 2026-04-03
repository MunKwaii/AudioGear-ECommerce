package vn.edu.ute.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@WebServlet("/api/admin/brands/upload-logo")
@MultipartConfig
public class AdminBrandLogoUploadController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        Part part = req.getPart("logo");
        if (part == null || part.getSize() == 0) {
            sendError(resp, "Không có file nào được chọn", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String submitted = Paths.get(part.getSubmittedFileName()).getFileName().toString();
        if (!isImageFile(submitted)) {
            sendError(resp, "Chỉ chấp nhận file ảnh (jpg, jpeg, png, gif, webp)", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String extension = getExtension(submitted);
        String filename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        String staticPath = "/static/images/brands";
        List<File> roots = resolveRoots(staticPath);
        if (roots.isEmpty()) {
            sendError(resp, "Không xác định được thư mục lưu ảnh", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        for (File root : roots) {
            if (!root.exists() && !root.mkdirs()) {
                sendError(resp, "Không tạo được thư mục: " + root.getAbsolutePath(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }

        try {
            File firstOutput = new File(roots.get(0), filename);
            part.write(firstOutput.getAbsolutePath());

            for (int i = 1; i < roots.size(); i++) {
                Files.copy(firstOutput.toPath(), new File(roots.get(i), filename).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            String url = staticPath + "/" + filename;
            resp.getWriter().write("{\"success\":true,\"url\":\"" + url + "\"}");
        } catch (Exception e) {
            sendError(resp, "Lỗi khi lưu ảnh: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private List<File> resolveRoots(String staticPath) {
        List<File> roots = new ArrayList<>();

        String deployPath = getServletContext().getRealPath(staticPath);
        if (deployPath != null) {
            File deployRoot = new File(deployPath);
            roots.add(deployRoot);

            File current = deployRoot;
            while (current != null) {
                if (current.getName().equals("target")) {
                    File projectRoot = current.getParentFile();
                    if (projectRoot != null) {
                        File srcRoot = new File(projectRoot,
                                "src/main/webapp" + staticPath.replace("/", File.separator));
                        if (!roots.contains(srcRoot)) {
                            roots.add(0, srcRoot);
                        }
                    }
                    break;
                }
                current = current.getParentFile();
            }
        }

        if (roots.size() < 2) {
            String userDir = System.getProperty("user.dir");
            File projectRoot = new File(userDir);
            while (projectRoot != null
                    && !new File(projectRoot, "pom.xml").exists()
                    && !new File(projectRoot, "src").exists()) {
                projectRoot = projectRoot.getParentFile();
            }
            if (projectRoot != null) {
                File srcRoot = new File(projectRoot,
                        "src/main/webapp" + staticPath.replace("/", File.separator));
                if (!roots.contains(srcRoot)) {
                    roots.add(0, srcRoot);
                }
            }
        }

        return roots;
    }

    private boolean isImageFile(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".gif") || lower.endsWith(".webp");
    }

    private String getExtension(String name) {
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == name.length() - 1) return "";
        return name.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private void sendError(HttpServletResponse resp, String message, int status) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write("{\"success\":false,\"message\":\"" + message.replace("\"", "\\\"") + "\"}");
    }
}
