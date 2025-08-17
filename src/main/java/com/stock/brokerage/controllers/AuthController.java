package com.stock.brokerage.controllers;

import com.stock.brokerage.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final CustomerRepository repo;

    @GetMapping("/login")
    public Map<String,Object> login(Authentication auth){
        var c = repo.findByUsername(auth.getName()).orElseThrow();
        return Map.of(
                "username", c.getUsername(),
                "customerId", c.getId(),
                "role", c.getRole().name()
        );
    }
}