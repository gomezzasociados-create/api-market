package com.gomezsystems.minierp.service;

import com.gomezsystems.minierp.model.Ajuste;
import com.gomezsystems.minierp.repository.AjusteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class EvolutionApiService {

    @Autowired
    private AjusteRepository ajusteRepository;

    @Value("${evolution.url:https://api.gomezz.space}")
    private String defaultEvolutionUrl;

    @Value("${evolution.instance:systems}")
    private String defaultInstanceName;

    @Value("${evolution.apikey:9FCB1D6E24E9-4815-B408-2EC8C2735312}")
    private String defaultApikey;

    public String getEvolutionUrl() {
        return ajusteRepository.findById("EVOLUTION_API_URL")
                .map(Ajuste::getValor)
                .filter(v -> v != null && !v.trim().isEmpty())
                .orElse(defaultEvolutionUrl);
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

    public boolean enviarMensajeFiado(String numeroDestino, String mensajeCompleto) {
        
        // Limpiamos o formateamos el numero en caso de que venga sin código de país.
        // Asumiendo formato de Chile +56 si no trae.
        if (numeroDestino == null || numeroDestino.trim().isEmpty()) {
            System.err.println("❌ EVOLUTION API: Número destino nulo o vacío.");
            return false;
        }

        String numFinal = numeroDestino.replaceAll("[^0-9]", "");
        if (numFinal.length() == 8 || numFinal.length() == 9) {
            numFinal = "569" + (numFinal.length() == 9 ? numFinal.substring(1) : numFinal); // Chile
        }

        String jsonPayload = String.format("{\"number\": \"%s\", \"text\": \"%s\"}", numFinal, MensajeEscape(mensajeCompleto));

        String url = getEvolutionUrl();
        if (url != null && url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        String endpoint = url + "/message/sendText/" + getInstanceName();

        try {
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
                System.out.println("✅ EVOLUTION API: Mensaje enviado exitosamente a " + numFinal);
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

    private String MensajeEscape(String msj){
        if (msj == null) return "";
        return msj.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
