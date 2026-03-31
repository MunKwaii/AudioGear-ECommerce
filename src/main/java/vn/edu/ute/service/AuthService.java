package vn.edu.ute.service;

import vn.edu.ute.auth.LoginType;
import vn.edu.ute.dto.request.AuthRequest;
import vn.edu.ute.entity.User;

public interface AuthService {
    User login(LoginType loginType, AuthRequest request);
}
