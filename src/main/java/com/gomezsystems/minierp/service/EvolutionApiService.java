package com.gomezsystems.minierp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomezsystems.minierp.model.Ajuste;
import com.gomezsystems.minierp.repository.AjusteRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class EvolutionApiService {

    @Autowired
    private AjusteRepository ajusteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${evolution.url:https://api.gomezsystems.online}")
    private String defaultEvolutionUrl;

    @Value("${evolution.instance:systems}")
    private String defaultInstanceName;

    @Value("${evolution.apikey:9FCB1D6E24E9-4815-B408-2EC8C2735312}")
    private String defaultApikey;

    @PostConstruct
    public void autoSanarConfiguracion() {
        try {
            // 1. Auto-sanar URL si está vacía o contiene dominios caídos/obsoletos
            Ajuste ajusteUrl = ajusteRepository.findById("EVOLUTION_API_URL").orElse(null);
            boolean urlInvalida = (ajusteUrl == null || ajusteUrl.getValor() == null || ajusteUrl.getValor().trim().isEmpty()
                    || ajusteUrl.getValor().contains("gomezz.space") || ajusteUrl.getValor().contains("localhost"));
            if (urlInvalida) {
                if (ajusteUrl == null) ajusteUrl = new Ajuste("EVOLUTION_API_URL", "https://api.gomezsystems.online/manager/");
                else ajusteUrl.setValor("https://api.gomezsystems.online/manager/");
                ajusteRepository.save(ajusteUrl);
                System.out.println("🔄 [EVOLUTION CONFIG] URL auto-reparada en base de datos a: https://api.gomezsystems.online/manager/");
            }

            // 2. Auto-sanar Instancia
            Ajuste ajusteInst = ajusteRepository.findById("EVOLUTION_INSTANCE").orElse(null);
            if (ajusteInst == null || ajusteInst.getValor() == null || ajusteInst.getValor().trim().isEmpty()) {
                if (ajusteInst == null) ajusteInst = new Ajuste("EVOLUTION_INSTANCE", "systems");
                else ajusteInst.setValor("systems");
                ajusteRepository.save(ajusteInst);
            }

            // 3. Auto-sanar API Key
            Ajuste ajusteKey = ajusteRepository.findById("EVOLUTION_APIKEY").orElse(null);
            if (ajusteKey == null || ajusteKey.getValor() == null || ajusteKey.getValor().trim().isEmpty()) {
                if (ajusteKey == null) ajusteKey = new Ajuste("EVOLUTION_APIKEY", "9FCB1D6E24E9-4815-B408-2EC8C2735312");
                else ajusteKey.setValor("9FCB1D6E24E9-4815-B408-2EC8C2735312");
                ajusteRepository.save(ajusteKey);
            }
        } catch (Exception e) {
            System.err.println("⚠️ [EVOLUTION CONFIG] Error durante auto-sanación: " + e.getMessage());
        }
    }

    public String getEvolutionUrl() {
        String url = ajusteRepository.findById("EVOLUTION_API_URL")
                .map(Ajuste::getValor)
                .filter(v -> v != null && !v.trim().isEmpty())
                .orElse(defaultEvolutionUrl);
        if (url != null) {
            url = url.trim();
            // Si la base de datos guardó un dominio obsoleto que ya no responde
            if (url.contains("gomezz.space") || url.contains("localhost")) {
                url = defaultEvolutionUrl;
            }
            while (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
            if (url.toLowerCase().endsWith("/manager")) {
                url = url.substring(0, url.length() - 8);
                while (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }
            }
        }
        return url != null && !url.isEmpty() ? url : defaultEvolutionUrl;
    }

    public String getInstanceName() {
        return ajusteRepository.findById("EVOLUTION_INSTANCE")
                .map(Ajuste::getValor)
                .filter(v -> v != null && !v.trim().isEmpty())
                .orElse(defaultInstanceName);
    }

    public String getApikey() {
        return ajusteRepository.findById("EVOLUTION_APIKEY")
                .map(Ajuste::getValor)
                .filter(v -> v != null && !v.trim().isEmpty())
                .orElse(defaultApikey);
    }

    public String formatearNumeroWhatsApp(String numeroDestino) {
        if (numeroDestino == null || numeroDestino.trim().isEmpty()) {
            return null;
        }

        String numFinal = numeroDestino.replaceAll("[^0-9]", "");
        if (numFinal.startsWith("0")) {
            numFinal = numFinal.substring(1);
        }

        // Formatos habituales de Chile (+56 9)
        if (numFinal.length() == 8) {
            numFinal = "569" + numFinal;
        } else if (numFinal.length() == 9) {
            if (numFinal.startsWith("9")) {
                numFinal = "56" + numFinal; // 56 + 9xxxxxxxx = 569xxxxxxxx
            } else {
                numFinal = "569" + numFinal;
            }
        } else if (numFinal.length() == 10) {
            if (numFinal.startsWith("56")) {
                numFinal = "569" + numFinal.substring(2);
            } else if (numFinal.startsWith("3")) {
                numFinal = "57" + numFinal; // Colombia celular
            }
        }

        return numFinal;
    }

    public boolean enviarMensajeFiado(String numeroDestino, String mensajeCompleto) {
        String numFinal = formatearNumeroWhatsApp(numeroDestino);
        if (numFinal == null || numFinal.isEmpty()) {
            System.err.println("❌ EVOLUTION API: Número destino nulo o inválido (" + numeroDestino + ").");
            return false;
        }

        String url = getEvolutionUrl();
        if (url != null && url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        String endpoint = url + "/message/sendText/" + getInstanceName();

        try {
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("number", numFinal);
            bodyMap.put("text", mensajeCompleto);
            String jsonPayload = objectMapper.writeValueAsString(bodyMap);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("apikey", getApikey())
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("✅ EVOLUTION API: Mensaje enviado exitosamente a " + numFinal + " (HTTP " + response.statusCode() + ")");
                return true;
            } else {
                System.err.println("❌ EVOLUTION API ERROR al enviar a " + numFinal + " : (" + response.statusCode() + ") " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ EVOLUTION API EXCEPTION: " + e.getMessage());
            return false;
        }
    }
}
