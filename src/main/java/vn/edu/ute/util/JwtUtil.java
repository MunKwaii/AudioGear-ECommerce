package vn.edu.ute.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Utility
 * Xử lý tạo và verify JWT tokens
 */
public class JwtUtil {

    private static final Logger logger = LogManager.getLogger(JwtUtil.class);

    // JWT Configuration
    private static final String SECRET_KEY = "audiogear-ecommerce-super-secret-key-for-jwt-token-generation-secure";
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 hours
    private static final long REFRESH_EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000; // 7 days

    private final SecretKey key;

    public JwtUtil() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(Long userId, String email, String role) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

            return Jwts.builder()
                    .setSubject(email)
                    .claim("userId", userId)
                    .claim("role", role)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            logger.error("Error generating JWT token for user: {}", email, e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    public String generateRefreshToken(Long userId, String email, String role) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + REFRESH_EXPIRATION_TIME);

            return Jwts.builder()
                    .setSubject(email)
                    .claim("userId", userId)
                    .claim("role", role)
                    .claim("type", "refresh")
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            logger.error("Error generating refresh token for user: {}", email, e);
            throw new RuntimeException("Failed to generate refresh token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (Exception e) {
            logger.error("Error extracting email from token", e);
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
            return null;
        } catch (Exception e) {
            logger.error("Error extracting userId from token", e);
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
            return claims.get("role", String.class);
        } catch (Exception e) {
            logger.error("Error extracting role from token", e);
            return null;
        }
    }

    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
            return claims.getExpiration();
        } catch (Exception e) {
            logger.error("Error extracting expiration date from token", e);
            return null;
        }
    }
}
