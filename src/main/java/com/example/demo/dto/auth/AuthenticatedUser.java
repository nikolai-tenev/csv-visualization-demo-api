package com.example.demo.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AuthenticatedUser {
    private String jwt;
    private String email;
    private List<String> roles;
}
