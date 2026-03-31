package vn.edu.ute.security;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JwtUserParser {

    private final Gson gson = new Gson();

    /**
     * Đọc Authorization header và parse thông tin user từ JWT payload.
     *
     * Format header:
     * Authorization: Bearer <token>
     */
    public CurrentUser parseFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }

        if (!authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7).trim();
        if (token.isBlank()) {
            return null;
        }

        return parseToken(token);
    }

    /**
     * Parse payload của JWT mà không verify chữ ký.
     * Dùng được khi project hiện tại chưa có JwtUtil hoàn chỉnh trong branch này.
     *
     * Lưu ý:
     * - Đây là cách tiện để nối flow checkout.
     * - Về production nên verify signature + expiration đầy đủ.
     */
    public CurrentUser parseToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String payload = parts[1];
            byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
            String jsonPayload = new String(decodedBytes, StandardCharsets.UTF_8);

            JsonObject payloadObject = gson.fromJson(jsonPayload, JsonObject.class);

            Long userId = payloadObject.has("id") && !payloadObject.get("id").isJsonNull()
                    ? payloadObject.get("id").getAsLong()
                    : null;

            String email = payloadObject.has("email") && !payloadObject.get("email").isJsonNull()
                    ? payloadObject.get("email").getAsString()
                    : null;

            String role = payloadObject.has("role") && !payloadObject.get("role").isJsonNull()
                    ? payloadObject.get("role").getAsString()
                    : null;

            if (userId == null) {
                return null;
            }

            return new CurrentUser(userId, email, role);

        } catch (Exception e) {
            return null;
        }
    }
}