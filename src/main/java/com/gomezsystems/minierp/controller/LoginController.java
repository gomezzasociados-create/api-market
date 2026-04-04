package com.gomezsystems.minierp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
public class LoginController {

    @Value("${app.security.admin.pin}")
    private String adminPin;

    @Value("${app.security.cajero.pin}")
    private String cajeroPin;

    @GetMapping("/login")
    public String renderLogin(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam("pin") String pin, HttpSession session, Model model, HttpServletRequest request, HttpServletResponse response) {
        
        if (adminPin.equals(pin)) {
            session.setAttribute("role", "ADMIN");
            session.setAttribute("username", "Gerente");
            registrarSpringSecurity("Gerente", "ROLE_ADMIN", request, response);
            return "redirect:/admin";
        } else if (cajeroPin.equals(pin)) {
            session.setAttribute("role", "CAJERO");
            session.setAttribute("username", "Vendedor");
            registrarSpringSecurity("Vendedor", "ROLE_CAJERO", request, response);
            return "redirect:/pos";
        } else {
            model.addAttribute("error", "⚠️ PIN INCORRECTO. INTENTE NUEVAMENTE.");
            return "login";
        }
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

    @GetMapping("/logout")
    public String salir(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
