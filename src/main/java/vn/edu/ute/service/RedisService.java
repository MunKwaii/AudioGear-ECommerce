package vn.edu.ute.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;
import vn.edu.ute.config.RedisConfig;

/**
 * Redis Service
 * Quản lý Token Blacklisting, lưu trữ OTP và logic Anti-spam.
 */
public class RedisService {

    private static final Logger logger = LogManager.getLogger(RedisService.class);
    
    // Tiền tố Key (Prefix)
    private static final String ACCESS_TOKEN_PREFIX = "access_token:";
    private static final String REGISTER_OTP_PREFIX = "register:otp:";
    private static final String FORGOT_PWD_OTP_PREFIX = "forgot_password:otp:";
    private static final String OTP_COOLDOWN_PREFIX = "otp:cooldown:";
    private static final String OTP_ATTEMPT_PREFIX = "otp:attempt:";
    private static final String OTP_BLOCK_PREFIX = "otp:block:";

    // Cấu hình Thời gian TTL
    private static final int OTP_TTL = 5 * 60; // 5 phút (giây)
    private static final int COOLDOWN_TTL = 60; // 1 phút (giây)
    private static final int BLOCK_TTL = 30 * 60; // 30 phút (giây)
    private static final int MAX_ATTEMPTS = 5;

    // ================= BLACKLIST TOKEN =================

    public void blacklistAccessToken(String token, long expirySeconds) {
        try (Jedis jedis = RedisConfig.getJedis()) {
            if (jedis == null) return;
            String key = ACCESS_TOKEN_PREFIX + "blacklist:" + token;
            jedis.setex(key, (int) expirySeconds, "1");
        } catch (Exception e) {
            logger.error("Lỗi khi đưa Token vào Blacklist", e);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        try (Jedis jedis = RedisConfig.getJedis()) {
            if (jedis == null) return false;
            String key = ACCESS_TOKEN_PREFIX + "blacklist:" + token;
            return jedis.exists(key);
        } catch (Exception e) {
            logger.error("Lỗi khi check Token Blacklist", e);
            return false;
        }
    }

    // ================= OTP LOGIC =================

    public void saveRegisterOtp(String email, String otp) {
        saveOtp(REGISTER_OTP_PREFIX + email, otp);
    }

    public void saveForgotPasswordOtp(String email, String otp) {
        saveOtp(FORGOT_PWD_OTP_PREFIX + email, otp);
    }

    private void saveOtp(String key, String otp) {
        try (Jedis jedis = RedisConfig.getJedis()) {
            if (jedis == null) throw new RuntimeException("Redis không hoạt động");
            jedis.setex(key, OTP_TTL, otp);
        } catch (Exception e) {
            logger.error("Lỗi khi lưu OTP vào Redis", e);
            throw new RuntimeException("Lỗi hệ thống lưu trữ mã OTP", e);
        }
    }

    public boolean verifyRegisterOtp(String email, String otp) {
        return verifyOtp(REGISTER_OTP_PREFIX + email, otp);
    }

    public boolean verifyForgotPasswordOtp(String email, String otp) {
        return verifyOtp(FORGOT_PWD_OTP_PREFIX + email, otp);
    }

    private boolean verifyOtp(String key, String inputOtp) {
        try (Jedis jedis = RedisConfig.getJedis()) {
            if (jedis == null) return false;
            String storedOtp = jedis.get(key);
            return storedOtp != null && storedOtp.equals(inputOtp);
        } catch (Exception e) {
            logger.error("Lỗi khi xác minh OTP", e);
            return false;
        }
    }

    public void deleteRegisterOtp(String email) {
        deleteKey(REGISTER_OTP_PREFIX + email);
    }

    public void deleteForgotPasswordOtp(String email) {
        deleteKey(FORGOT_PWD_OTP_PREFIX + email);
    }

    private void deleteKey(String key) {
        try (Jedis jedis = RedisConfig.getJedis()) {
            if (jedis != null) jedis.del(key);
        } catch (Exception e) {
            logger.error("Lỗi xóa key khỏi Redis", e);
        }
    }

    // ================= ANTI-SPAM LOGIC =================

    public boolean canSendOtp(String email) {
        try (Jedis jedis = RedisConfig.getJedis()) {
            if (jedis == null) return true; // Nếu redis chết, cho qua
            return !jedis.exists(OTP_COOLDOWN_PREFIX + email);
        }
    }

    public void setOtpCooldown(String email) {
        try (Jedis jedis = RedisConfig.getJedis()) {
            if (jedis != null) {
                jedis.setex(OTP_COOLDOWN_PREFIX + email, COOLDOWN_TTL, "1");
            }
        }
    }

    public boolean isOtpBlocked(String email) {
        try (Jedis jedis = RedisConfig.getJedis()) {
            if (jedis == null) return false;
            return jedis.exists(OTP_BLOCK_PREFIX + email);
        }
    }

    public int incrementOtpAttempt(String email) {
        try (Jedis jedis = RedisConfig.getJedis()) {
            if (jedis == null) return 1;
            String attemptKey = OTP_ATTEMPT_PREFIX + email;
            long attempts = jedis.incr(attemptKey);
            
            if (attempts == 1) {
                jedis.expire(attemptKey, OTP_TTL); // Reset attempt list after 5 min
            }

            if (attempts >= MAX_ATTEMPTS) {
                jedis.setex(OTP_BLOCK_PREFIX + email, BLOCK_TTL, "BLOCKED");
                jedis.del(attemptKey);
            }
            return (int) attempts;
        }
    }

    public void clearOtpAttempts(String email) {
        deleteKey(OTP_ATTEMPT_PREFIX + email);
    }
}
