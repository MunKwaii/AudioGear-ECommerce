package vn.edu.ute.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;
import vn.edu.ute.config.RedisConfig;

/**
 * Redis Service
 * Quản lý Token Blacklisting, thiết kế gracefully fallback chống crash.
 */
public class RedisService {

    private static final Logger logger = LogManager.getLogger(RedisService.class);
    private static final String ACCESS_TOKEN_PREFIX = "access_token:";

    /**
     * Chặn Token (khi người dùng đăng xuất)
     */
    public void blacklistAccessToken(String token, long expirySeconds) {
        Jedis jedis = RedisConfig.getJedis();
        if (jedis == null) return; // Fallback
        
        try {
            String key = ACCESS_TOKEN_PREFIX + "blacklist:" + token;
            jedis.setex(key, (int) expirySeconds, "1");
        } catch (Exception e) {
            logger.error("Lỗi khi đưa Token vào Blacklist", e);
        } finally {
            if (jedis != null) jedis.close();
        }
    }

    /**
     * Kiểm tra xem Token có bị Blacklist không
     */
    public boolean isTokenBlacklisted(String token) {
        Jedis jedis = RedisConfig.getJedis();
        if (jedis == null) return false; // Fallback nếu Redis chưa cài đặt
        
        try {
            String key = ACCESS_TOKEN_PREFIX + "blacklist:" + token;
            return jedis.exists(key);
        } catch (Exception e) {
            logger.error("Lỗi khi check Token Blacklist", e);
            return false;
        } finally {
            if (jedis != null) jedis.close();
        }
    }
}
