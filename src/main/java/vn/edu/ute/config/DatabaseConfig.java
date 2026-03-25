package vn.edu.ute.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Database Configuration Manager for AudioGear-ECommerce
 * Quản lý kết nối database và EntityManager
 */
public class DatabaseConfig {
    
    private static final Logger logger = LogManager.getLogger(DatabaseConfig.class);
    private static final String PERSISTENCE_UNIT_NAME = "audiogear-pu";
    
    private static EntityManagerFactory entityManagerFactory;
    private static final ThreadLocal<EntityManager> entityManagerThreadLocal = new ThreadLocal<>();
    
    static {
        initializeEntityManagerFactory();
    }
    
    /**
     * Khởi tạo EntityManagerFactory
     */
    private static void initializeEntityManagerFactory() {
        try {
            logger.info("Initializing EntityManagerFactory for AudioGear Project...");
            
            // Generate properties map with specific credentials
            Map<String, String> properties = new HashMap<>();
            
            // Use environment variables if set, otherwise fallback to the provided Render PostgreSQL credentials
            String dbHost = getEnvOrProperty("DB_HOST", "dpg-d71n9u24d50c73bs0h3g-a.oregon-postgres.render.com");
            String dbPort = getEnvOrProperty("DB_PORT", "5432");
            String dbName = getEnvOrProperty("DB_NAME", "audiogear_ecommerce");
            String dbUser = getEnvOrProperty("DB_USER", "audiogear_ecommerce_user");
            String dbPassword = getEnvOrProperty("DB_PASSWORD", "o82tvnOhhn6TbgArdqhyiFS1YXcRTGxP");

            // Construct JDBC URL with SSL settings specifically recommended for Render Postgres external connections
            String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName);
            
            properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
            properties.put("jakarta.persistence.jdbc.user", dbUser);
            properties.put("jakarta.persistence.jdbc.password", dbPassword);
            
            logger.info("Using database URL: {}", jdbcUrl);
            logger.info("Using database user: {}", dbUser);
            
            entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
            logger.info("EntityManagerFactory initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize EntityManagerFactory", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Get environment variable or system property with fallback
     */
    private static String getEnvOrProperty(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
    
    /**
     * Lấy EntityManager cho thread hiện tại
     * @return EntityManager instance
     */
    public static EntityManager getEntityManager() {
        EntityManager em = entityManagerThreadLocal.get();
        if (em == null || !em.isOpen()) {
            em = entityManagerFactory.createEntityManager();
            entityManagerThreadLocal.set(em);
            logger.debug("Created new EntityManager for thread: {}", Thread.currentThread().getName());
        } else {
            // Clear cache để tránh stale data
            em.clear();
            logger.debug("Cleared EntityManager cache for thread: {}", Thread.currentThread().getName());
        }
        return em;
    }
    
    /**
     * Đóng EntityManager cho thread hiện tại
     */
    public static void closeEntityManager() {
        EntityManager em = entityManagerThreadLocal.get();
        if (em != null && em.isOpen()) {
            em.close();
            entityManagerThreadLocal.remove();
            logger.debug("Closed EntityManager for thread: {}", Thread.currentThread().getName());
        }
    }
    
    /**
     * Bắt đầu transaction
     */
    public static void beginTransaction() {
        EntityManager em = getEntityManager();
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
            logger.debug("Transaction started");
        }
    }
    
    /**
     * Commit transaction
     */
    public static void commitTransaction() {
        EntityManager em = getEntityManager();
        if (em.getTransaction().isActive()) {
            em.getTransaction().commit();
            logger.debug("Transaction committed");
        }
    }
    
    /**
     * Rollback transaction
     */
    public static void rollbackTransaction() {
        EntityManager em = getEntityManager();
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
            logger.debug("Transaction rolled back");
        }
    }
    
    /**
     * Đóng EntityManagerFactory
     */
    public static void shutdown() {
        try {
            closeEntityManager();
            if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
                logger.info("EntityManagerFactory closed successfully");
            }
        } catch (Exception e) {
            logger.error("Error closing EntityManagerFactory", e);
        }
    }
    
    /**
     * Kiểm tra kết nối database
     * @return true nếu kết nối thành công
     */
    public static boolean testConnection() {
        try {
            EntityManager em = getEntityManager();
            em.createNativeQuery("SELECT 1").getSingleResult();
            logger.info("Database connection test successful");
            return true;
        } catch (Exception e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
}
