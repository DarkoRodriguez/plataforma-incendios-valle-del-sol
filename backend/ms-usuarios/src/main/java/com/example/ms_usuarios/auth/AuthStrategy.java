package com.example.ms_usuarios.auth;

import java.util.Optional;

import com.example.ms_usuarios.model.User;

public interface AuthStrategy {
    String name();
    Optional<User> authenticate(String username, String password);
}
