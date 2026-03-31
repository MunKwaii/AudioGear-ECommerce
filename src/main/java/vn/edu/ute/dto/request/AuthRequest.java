package vn.edu.ute.dto.request;

/**
 * DTO chứa thông tin yêu cầu đăng nhập chung (Generic Auth Request)
 * Có thể là username/password cho Local hoặc token/profileData cho Google.
 */
public class AuthRequest {
    private String usernameOrEmail;
    private String password;
    
    // Dành cho Google Login
    private String googleToken;
    private vn.edu.ute.auth.adapter.GoogleProfile googleProfile;

    public AuthRequest() {}

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGoogleToken() {
        return googleToken;
    }

    public void setGoogleToken(String googleToken) {
        this.googleToken = googleToken;
    }

    public vn.edu.ute.auth.adapter.GoogleProfile getGoogleProfile() {
        return googleProfile;
    }

    public void setGoogleProfile(vn.edu.ute.auth.adapter.GoogleProfile googleProfile) {
        this.googleProfile = googleProfile;
    }
}
