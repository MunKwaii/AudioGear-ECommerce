package vn.edu.ute.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis Configuration
 * Thread-Safe Singleton pattern (Double-Checked Locking) cho Redis connection management, hỗ trợ fallback nếu không có Redis.
 */
public class RedisConfig {

    private static final Logger logger = LogManager.getLogger(RedisConfig.class);
    
    // volatile ngăn chặn việc reordering khi multi-thread đọc ghi biến instance
    private static volatile RedisConfig instance;
    
    private JedisPool jedisPool;
    private boolean isRedisAvailable = false;

    // Private constructor ngăn cấm việc khởi tạo từ bên ngoài
    private RedisConfig() {
        initializeJedisPool();
    }

    // Double-Checked Locking đảm bảo chuẩn Thread-Safe
    public static RedisConfig getInstance() {
        if (instance == null) {
            synchronized (RedisConfig.class) {
                if (instance == null) {
                    instance = new RedisConfig();
                }
            }
        }
        return instance;
    }

    private void initializeJedisPool() {
        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(20);
            poolConfig.setMaxIdle(10);
            poolConfig.setMaxWaitMillis(2000); // Đợi max 2s nếu không có kết nối

            String redisHost = "localhost";
            int redisPort = 6379;

            jedisPool = new JedisPool(poolConfig, redisHost, redisPort, 2000);

            // Test kết nối 
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
                isRedisAvailable = true;
                logger.info("✓ Redis connection established successfully!");
            }
        } catch (Exception e) {
            logger.warn("⚠ Chú ý: Không thể kết nối tới Redis Server trên localhost:6379. Ứng dụng vẫn chạy bình thường nhưng tính năng Blacklist Token sẽ bị vô hiệu hóa.");
            isRedisAvailable = false;
        }
    }

    /**
     * Lấy Jedis resource. Trả về null nếu Redis không hoạt động.
     */
    public Jedis getJedis() {
        if (!isRedisAvailable || jedisPool == null) {
            return null;
        }
        try {
            return jedisPool.getResource();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isAvailable() {
        return isRedisAvailable;
    }

    public void closePool() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
        }
    }
}
