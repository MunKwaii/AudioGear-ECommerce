package vn.edu.ute.util;

/**
 * Utility class to resolve and normalize image paths for the storefront.
 * Handles discrepancies between Database paths and Physical Static Resource paths.
 */
public class ImageUtil {

    private static final String DEFAULT_IMAGE = "/static/img/hero_bg.png";
    private static final String STATIC_PREFIX = "/static";

    /**
     * Resolves a raw image path from database to a valid web URL.
     * 
     * @param rawPath The path stored in DB (e.g. "/images/products/item.jpg" or "item.jpg")
     * @return Normalized path (e.g. "/static/images/item.jpg")
     */
    public static String resolveImageUrl(String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty()) {
            return DEFAULT_IMAGE;
        }

        String path = rawPath.trim();

        // 1. External links
        if (path.startsWith("http")) {
            return path;
        }

        // 2. Already contains /static/
        if (path.startsWith(STATIC_PREFIX + "/")) {
            return path;
        }

        // 3. Handle legacy Database paths (e.g. /images/products/...)
        // Mismatch: DB has /images/products/ but disk has /static/images/
        if (path.startsWith("/images/products/")) {
            return STATIC_PREFIX + "/images/" + path.substring("/images/products/".length());
        }

        // 4. Case where path starts with / (e.g. /images/logo.png) -> Just prepend /static
        if (path.startsWith("/")) {
            return STATIC_PREFIX + path;
        }

        // 5. Short filename (e.g. "product1.jpg") -> Default to /static/img/
        return STATIC_PREFIX + "/img/" + path;
    }
}
