package com.valledelsol.usuarios.auth;

import java.util.Optional;

import com.valledelsol.usuarios.model.User;

public interface AuthStrategy {
    String name();
    Optional<User> authenticate(String username, String password);
}
