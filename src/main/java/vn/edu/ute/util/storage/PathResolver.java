package vn.edu.ute.util.storage;

import jakarta.servlet.ServletContext;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to resolve source and deployment paths for file storage.
 * Standardizes the way 'src' and 'target' directories are identified.
 */
public class PathResolver {

    /**
     * Resolves multiple root directories for a given static path.
     * Usually includes the deployment path (target) and, if possible, the source path (src).
     *
     * @param servletContext The servlet context to find the real path on disk.
     * @param staticPath The web-relative path (e.g., "/static/images/avatars").
     * @return List of File objects pointing to existing (or newly created) directories.
     */
    public static List<File> resolveRoots(ServletContext servletContext, String staticPath) {
        List<File> roots = new ArrayList<>();

        // 1. Get deployment path (target)
        String deployPath = servletContext.getRealPath(staticPath);
        if (deployPath != null) {
            File deployRoot = new File(deployPath);
            roots.add(deployRoot);

            // 2. Try to find 'src' by looking for 'target' folder and going up
            File current = deployRoot;
            while (current != null) {
                if (current.getName().equals("target")) {
                    File projectRoot = current.getParentFile();
                    if (projectRoot != null) {
                        File srcRoot = new File(projectRoot, "src" + File.separator + "main" + File.separator + "webapp" + staticPath.replace("/", File.separator));
                        if (!roots.contains(srcRoot)) {
                            // Prepend srcRoot to prioritize saving to source
                            roots.add(0, srcRoot);
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
            
            // Look for pom.xml or src folder near userDir or its parents
            while (projectRoot != null && !new File(projectRoot, "pom.xml").exists() && !new File(projectRoot, "src").exists()) {
                projectRoot = projectRoot.getParentFile();
            }

            if (projectRoot != null) {
                File srcRoot = new File(projectRoot, "src" + File.separator + "main" + File.separator + "webapp" + staticPath.replace("/", File.separator));
                if (!roots.contains(srcRoot)) {
                    roots.add(0, srcRoot);
                }
            }
        }

        return roots;
    }

    /**
     * Finds the project root directory.
     * Useful for initializing storage strategies that need a base path.
     *
     * @param servletContext The servlet context.
     * @return Path to the project root (absolute string).
     */
    public static String getProjectRoot(ServletContext servletContext) {
        String realPath = servletContext.getRealPath("/");
        if (realPath != null) {
            File current = new File(realPath);
            while (current != null) {
                if (current.getName().equals("target")) {
                    File projectRoot = current.getParentFile();
                    if (projectRoot != null && new File(projectRoot, "src").exists()) {
                        return projectRoot.getAbsolutePath();
                    }
                }
                current = current.getParentFile();
            }
        }
        
        // Fallback to user.dir and search upwards
        String userDir = System.getProperty("user.dir");
        File projectRoot = new File(userDir);
        while (projectRoot != null && !new File(projectRoot, "pom.xml").exists() && !new File(projectRoot, "src").exists()) {
            projectRoot = projectRoot.getParentFile();
        }
        
        return projectRoot != null ? projectRoot.getAbsolutePath() : userDir;
    }
}
