package com.gomezsystems.minierp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Value("${app.security.admin.pin}")
    private String adminPin;

    @Value("${app.security.cajero.pin}")
    private String cajeroPin;

    @PostMapping("/unlock")
    public ResponseEntity<Map<String, String>> unlockPanel(@RequestBody Map<String, String> payload, HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        String pin = payload.get("pin");
        Map<String, String> res = new HashMap<>();

        if (adminPin.equals(pin)) {
            session.setAttribute("role", "ADMIN");
            session.setAttribute("username", "Gerente");
            registrarSpringSecurity("Gerente", "ROLE_ADMIN", request, response);

            res.put("status", "success");
            res.put("role", "ADMIN");
            return ResponseEntity.ok(res);
        } else if (cajeroPin.equals(pin)) {
            session.setAttribute("role", "CAJERO");
            session.setAttribute("username", "Vendedor");
            registrarSpringSecurity("Vendedor", "ROLE_CAJERO", request, response);

            res.put("status", "success");
            res.put("role", "CAJERO");
            return ResponseEntity.ok(res);
        }

        res.put("status", "error");
        res.put("mensaje", "PIN INCORRECTO");
        return ResponseEntity.status(401).body(res);
    }

    private void registrarSpringSecurity(String username, String role, HttpServletRequest request, HttpServletResponse response) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
        Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        
        SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
        securityContextRepository.saveContext(context, request, response);
    }
}

