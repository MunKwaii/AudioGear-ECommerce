package vn.edu.ute.util.storage;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Disk Storage Strategy: Lưu trữ tập tin trên đĩa cứng cục bộ.
 * Phục vụ cả việc hiển thị nhanh (Target) và lưu giữ vĩnh viễn (Source).
 */
public class DiskStorageStrategy implements StorageStrategy {

    private final String rootProjectPath;
    private final ServletContext servletContext;

    public DiskStorageStrategy(String rootProjectPath, ServletContext servletContext) {
        this.rootProjectPath = rootProjectPath;
        this.servletContext = servletContext;
    }

    @Override
    public String store(Part filePart, String fileName, String subFolder) throws IOException {
        String baseSubPath = "/static/images/" + subFolder;
        
        // 1. Đường dẫn Deployment (Target)
        String deployPath = servletContext.getRealPath(baseSubPath);
        
        // 2. Đường dẫn Source (Vĩnh viễn)
        String srcPath = rootProjectPath + File.separator + "src/main/webapp" + baseSubPath;

        // Đảm bảo các thư mục tồn tại
        File srcDir = new File(srcPath);
        if (!srcDir.exists()) srcDir.mkdirs();

        File deployDir = (deployPath == null) ? null : new File(deployPath);
        if (deployDir != null && !deployDir.exists()) deployDir.mkdirs();

        // Bước 1: Lưu vào thư mục SRC (Vĩnh viễn)
        File srcFile = new File(srcDir, fileName);
        try (InputStream input = filePart.getInputStream()) {
            Files.copy(input, srcFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // Bước 2: Sao chép sang DEPLOY (Để hiển thị ngay lập tức)
        if (deployDir != null && !srcPath.equals(deployPath)) {
            File deployFile = new File(deployDir, fileName);
            Files.copy(srcFile.toPath(), deployFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return baseSubPath + "/" + fileName;
    }
}
