package com.valledelsol.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

        @NotBlank
        private String username;

        @NotBlank
        @Size(min = 8, max = 72)
        private String password;

        public LoginRequest() {}

        public String getUsername() {
                return username;
        }

        public void setUsername(String username) {
                this.username = username;
        }

        public String getPassword() {
                return password;
        }

        public void setPassword(String password) {
                this.password = password;
        }
}
