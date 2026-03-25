package vn.edu.ute.auth.adapter;

/**
 * Lớp Adaptee đại diện cho dữ liệu trả về từ Google (Bên thứ 3)
 */
public class GoogleProfile {
    private String googleId;
    private String email;
    private String name;
    private String givenName;
    private String familyName;
    private String pictureUrl;

    public GoogleProfile() {}

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGivenName() { return givenName; }
    public void setGivenName(String givenName) { this.givenName = givenName; }

    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }

    public String getPictureUrl() { return pictureUrl; }
    public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }
}
