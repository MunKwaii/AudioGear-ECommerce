package vn.edu.ute.util.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class CloudinaryStorageStrategy implements StorageStrategy {

    private final Cloudinary cloudinary;

    public CloudinaryStorageStrategy() {
        String cloudinaryUrl = System.getenv("CLOUDINARY_URL");
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            cloudinaryUrl = System.getProperty("CLOUDINARY_URL");
        }
        
        // Cấu hình mặc định (Fallback)
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            cloudinaryUrl = "cloudinary://412898692979119:ao6oeG29rAyudJoUtax3UcU85qA@dneg3vaiq";
        }
        
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }
    
    public CloudinaryStorageStrategy(String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

    @Override
    public String store(Part filePart, String fileName, String subFolder) throws IOException {
        try (InputStream inputStream = filePart.getInputStream()) {
            byte[] fileBytes = inputStream.readAllBytes();
            
            String publicId = fileName;
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                publicId = fileName.substring(0, dotIndex);
            }
            
            Map uploadResult = cloudinary.uploader().upload(fileBytes,
                    ObjectUtils.asMap(
                            "folder", subFolder,
                            "public_id", publicId,
                            "use_filename", true,
                            "unique_filename", false
                    ));
                    
            return (String) uploadResult.get("secure_url");
        }
    }
}
