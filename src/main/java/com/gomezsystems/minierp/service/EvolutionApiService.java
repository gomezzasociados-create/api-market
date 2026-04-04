package com.gomezsystems.minierp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EvolutionApiService {

    @Value("${evolution.url:https://api.gomezz.space}")
    private String evolutionUrl;

    @Value("${evolution.instance:systems}")
    private String instanceName;

    @Value("${evolution.apikey:9FCB1D6E24E9-4815-B408-2EC8C2735312}")
    private String apikey;

    public void enviarMensajeFiado(String numeroDestino, String mensajeCompleto) {
        
        // Limpiamos o formateamos el numero en caso de que venga sin código de país.
        // Asumiendo formato de Chile +56 si no trae.
        if (numeroDestino == null || numeroDestino.trim().isEmpty()) {
            System.err.println("❌ EVOLUTION API: Número destino nulo o vacío.");
            return;
        }

        String numFinal = numeroDestino.replaceAll("[^0-9]", "");
        if (numFinal.length() == 8 || numFinal.length() == 9) {
            numFinal = "569" + (numFinal.length() == 9 ? numFinal.substring(1) : numFinal); // Chile
        }

        String jsonPayload = String.format("{\"number\": \"%s\", \"text\": \"%s\"}", numFinal, MensajeEscape(mensajeCompleto));

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(evolutionUrl + "/message/sendText/" + instanceName))
                    .header("Content-Type", "application/json")
                    .header("apikey", apikey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("✅ EVOLUTION API: Mensaje de Cobro enviado exitosamente a " + numFinal);
            } else {
                System.err.println("❌ EVOLUTION API ERROR al enviar a " + numFinal + " : " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ EVOLUTION API EXCEPTION: " + e.getMessage());
        }
    }

    private String MensajeEscape(String msj){
        if (msj == null) return "";
        return msj.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
