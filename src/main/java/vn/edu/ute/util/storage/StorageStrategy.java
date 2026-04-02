package vn.edu.ute.util.storage;

import jakarta.servlet.http.Part;
import java.io.IOException;

/**
 * Strategy Pattern: Định nghĩa giao diện lưu trữ tập tin.
 */
public interface StorageStrategy {
    /**
     * Lưu trữ tập tin và trả về đường dẫn URL để truy cập.
     * @param filePart Tập tin từ Request.
     * @param fileName Tên tập tin muốn lưu.
     * @param subFolder Thư mục con hỗ trợ (ví dụ: avatar, product).
     * @return URL dẫn đến tập tin.
     */
    String store(Part filePart, String fileName, String subFolder) throws IOException;
}
