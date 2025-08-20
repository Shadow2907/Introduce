package com.shino.vnpt.authentication.login;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    void logout(String refreshToken);
}
