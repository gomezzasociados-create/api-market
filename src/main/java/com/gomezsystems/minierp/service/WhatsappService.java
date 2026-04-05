package com.gomezsystems.minierp.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsappService {

    // ========================================================
    // ⚠️ PEGA TUS DATOS REALES DE EVOLUTION API AQUÍ:
    // ========================================================
    private final String apiUrl = "https://api.gomezz.space";          // Ej: http://tu-ip:8080 (Sin la barra / al final)
    private final String instancia = "systems"; // Ej: GomezMarket
    private final String token = "9FCB1D6E24E9-4815-B408-2EC8C2735312";         // Tu Global Apikey o el de la instancia
    // ========================================================

    private final RestTemplate restTemplate = new RestTemplate();

    public void enviarMensajeTexto(String numeroDestino, String mensaje) {
        // 1. Armamos la URL exacta hacia tu Evolution API
        String endpoint = apiUrl + "/message/sendText/" + instancia;

        // 2. Configuramos los Headers (Aquí va tu Token de seguridad)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", token);

        // 3. Armamos el cuerpo de la petición (JSON)
        Map<String, Object> body = new HashMap<>();

        // Formateo simple: Nos aseguramos de limpiar el número de espacios o signos '+'
        String numeroLimpio = numeroDestino.replaceAll("[^0-9]", "");

        body.put("number", numeroLimpio);
        body.put("text", mensaje);

        // Opcional pero recomendado: un pequeño retraso para simular escritura humana
        Map<String, Object> options = new HashMap<>();
        options.put("delay", 1500);
        body.put("options", options);

        // 4. Empaquetamos todo
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            System.out.println("🚀 [G.O.M.E.Z.] Intentando disparar mensaje a: " + numeroLimpio);

            // 5. ¡G.O.M.E.Z. dispara el mensaje!
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ [G.O.M.E.Z.] ¡WhatsApp Enviado exitosamente!");
            } else {
                System.err.println("⚠️ [G.O.M.E.Z.] Evolution respondió con error. Código: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ [G.O.M.E.Z.] Error físico de conexión con Evolution API: " + e.getMessage());
        }
    }
}