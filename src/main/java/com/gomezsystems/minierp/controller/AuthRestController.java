package com.gomezsystems.minierp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Value("${app.security.admin.pin}")
    private String adminPin;

    @Value("${app.security.cajero.pin}")
    private String cajeroPin;

    @PostMapping("/unlock")
    public ResponseEntity<Map<String, String>> unlockPanel(@RequestBody Map<String, String> payload) {
        String pin = payload.get("pin");
        Map<String, String> response = new HashMap<>();

        if (adminPin.equals(pin)) {
            response.put("status", "success");
            response.put("role", "ADMIN");
            return ResponseEntity.ok(response);
        } else if (cajeroPin.equals(pin)) {
            response.put("status", "success");
            response.put("role", "CAJERO");
            return ResponseEntity.ok(response);
        }

        response.put("status", "error");
        response.put("mensaje", "PIN INCORRECTO");
        return ResponseEntity.status(401).body(response);
    }
}
