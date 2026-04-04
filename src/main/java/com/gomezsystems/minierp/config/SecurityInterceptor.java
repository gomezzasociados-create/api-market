package com.gomezsystems.minierp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        String uri = request.getRequestURI();
        
        // Rutas liberadas
        if(uri.equals("/login") || uri.equals("/menu") || 
           uri.startsWith("/css") || uri.startsWith("/js") || 
           uri.equals("/api/auth/unlock") || uri.startsWith("/api/admin/robot/test") ||
           uri.endsWith(".js") || uri.endsWith(".json") || uri.endsWith(".mp4") || uri.endsWith(".png") || uri.endsWith(".ico")) {
            return true; 
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            // Usuario sin sesión bloqueado
            if (uri.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            response.sendRedirect("/login");
            return false;
        }

        String rol = (String) session.getAttribute("role");

        // Reglas de Bóveda: El cajero NO puede entrar a Admin, y APIs de Admin solo para "ADMIN"
        if ((uri.startsWith("/admin") || uri.startsWith("/api/admin")) && !"ADMIN".equals(rol)) {
            if (uri.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
            response.sendRedirect("/pos");
            return false;
        }

        return true; 
    }
}
