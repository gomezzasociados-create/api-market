package com.gomezsystems.minierp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Permitimos POST desde las APIs JS vanilla
            .headers(headers -> headers.frameOptions(f -> f.disable())) // Permite h2-console
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/img/**", "/*.json", "/*.png", "/*.ico", "/*.mp4").permitAll()
                .requestMatchers("/api/auth/unlock", "/api/admin/robot/test").permitAll()
                .requestMatchers("/", "/menu", "/api/productos", "/api/checkout", "/api/checkout/**").permitAll()
                .requestMatchers("/h2-console/**").hasRole("ADMIN")
                .requestMatchers("/admin", "/admin/**", "/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/pos", "/pos/**", "/api/**").hasAnyRole("ADMIN", "CAJERO")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(401);
                    } else {
                        response.sendRedirect("/login");
                    }
                })
            )
            .formLogin(form -> form.disable())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );
        return http.build();
    }
}
