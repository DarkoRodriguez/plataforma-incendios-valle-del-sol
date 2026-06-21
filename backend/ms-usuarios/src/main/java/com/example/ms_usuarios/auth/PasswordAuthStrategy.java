package com.example.ms_usuarios.auth;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.ms_usuarios.model.User;
import com.example.ms_usuarios.repository.UserRepository;

@Component
public class PasswordAuthStrategy implements AuthStrategy {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String name() {
        return "password";
    }

    @Override
    public Optional<User> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
            .filter(u -> passwordEncoder.matches(password, u.getPassword()));
    }
}
