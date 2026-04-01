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

        // 3. Allow /images/products/... or images/products/...
        if (path.startsWith("/images/products/") || path.startsWith("images/products/")) {
            String clean = path.startsWith("/") ? path.substring(1) : path;
            return STATIC_PREFIX + "/" + clean;
        }

        // 4. Allow products/... -> map to /static/images/products/...
        if (path.startsWith("products/")) {
            return STATIC_PREFIX + "/images/" + path;
        }

        // 5. Database paths starting with images/ without leading slash
        if (path.startsWith("images/")) {
            return STATIC_PREFIX + "/" + path;
        }

        // 6. Case where path starts with / (e.g. /images/logo.png) -> Just prepend /static
        if (path.startsWith("/")) {
            return STATIC_PREFIX + path;
        }

        // 7. Short filename (e.g. "product1.jpg") -> Default to /static/img/
        return STATIC_PREFIX + "/img/" + path;
    }
}
