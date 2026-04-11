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
 * Áp dụng EAGER INITIALIZATION SINGLETON PATTERN
 */
public class DatabaseConfig {

    private static final Logger logger = LogManager.getLogger(DatabaseConfig.class);
    private static final String PERSISTENCE_UNIT_NAME = "audiogear-pu";

    // 1. TẠO NGAY INSTANCE (Eager Initialization) tại thời điểm bốc class vào bộ nhớ
    private static final DatabaseConfig uniqueInstance = new DatabaseConfig();

    private EntityManagerFactory entityManagerFactory;
    private final ThreadLocal<EntityManager> entityManagerThreadLocal = new ThreadLocal<>();

    // 2. Private Constructor: Chặn không cho khởi tạo từ bên ngoài bằng "new"
    private DatabaseConfig() {
        initializeEntityManagerFactory();
    }

    // 3. Hàm public để bên ngoài lấy instance duy nhất
    public static DatabaseConfig getInstance() {
        return uniqueInstance;
    }

    /**
     * Khởi tạo EntityManagerFactory
     */
    private void initializeEntityManagerFactory() {
        try {
            logger.info("Initializing EntityManagerFactory for AudioGear Project...");

            Map<String, String> properties = new HashMap<>();

            // Cấu hình Database Online (Host on Render)
            String dbHost = getEnvOrProperty("DB_HOST", "dpg-d71n9u24d50c73bs0h3g-a.oregon-postgres.render.com");
            String dbPort = getEnvOrProperty("DB_PORT", "5432");
            String dbName = getEnvOrProperty("DB_NAME", "audiogear_ecommerce");
            String dbUser = getEnvOrProperty("DB_USER", "audiogear_ecommerce_user");
            String dbPassword = getEnvOrProperty("DB_PASSWORD", "o82tvnOhhn6TbgArdqhyiFS1YXcRTGxP");

            // Cấu hình Database Local (Bỏ comment để dùng Local)
            // String dbHost = getEnvOrProperty("DB_HOST", "localhost");
            // String dbPort = getEnvOrProperty("DB_PORT", "5432");
            // String dbName = getEnvOrProperty("DB_NAME", "audiogear_db");
            // String dbUser = getEnvOrProperty("DB_USER", "postgres");
            // String dbPassword = getEnvOrProperty("DB_PASSWORD", "12345");

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

    private String getEnvOrProperty(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    public EntityManager getEntityManager() {
        EntityManager em = entityManagerThreadLocal.get();
        if (em == null || !em.isOpen()) {
            em = entityManagerFactory.createEntityManager();
            entityManagerThreadLocal.set(em);
            logger.debug("Created new EntityManager for thread: {}", Thread.currentThread().getName());
        }
        return em;
    }

    public void closeEntityManager() {
        EntityManager em = entityManagerThreadLocal.get();
        if (em != null && em.isOpen()) {
            em.close();
            entityManagerThreadLocal.remove();
            logger.debug("Closed EntityManager for thread: {}", Thread.currentThread().getName());
        }
    }

    public void beginTransaction() {
        EntityManager em = getEntityManager();
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
            logger.debug("Transaction started");
        }
    }

    public void commitTransaction() {
        EntityManager em = getEntityManager();
        if (em.getTransaction().isActive()) {
            em.getTransaction().commit();
            logger.debug("Transaction committed");
        }
    }

    public void rollbackTransaction() {
        EntityManager em = getEntityManager();
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
            logger.debug("Transaction rolled back");
        }
    }

    public void shutdown() {
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

    public boolean testConnection() {
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
