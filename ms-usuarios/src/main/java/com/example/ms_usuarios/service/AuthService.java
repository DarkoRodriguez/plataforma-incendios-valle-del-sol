package com.example.ms_usuarios.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ms_usuarios.auth.AuthStrategy;
import com.example.ms_usuarios.model.User;

@Service
public class AuthService {

    private final List<AuthStrategy> strategies;

    @Autowired
    public AuthService(List<AuthStrategy> strategies) {
        this.strategies = strategies;
    }

    public Optional<User> authenticate(String username, String password) {
        // Choose password strategy by name (simple selection)
        return strategies.stream()
                .filter(s -> s.name().equals("password"))
                .findFirst()
                .flatMap(s -> s.authenticate(username, password));
    }
}
