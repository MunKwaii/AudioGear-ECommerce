package vn.edu.ute.security;

public class CurrentUser {

    private final Long userId;
    private final String email;
    private final String role;

    public CurrentUser(Long userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}