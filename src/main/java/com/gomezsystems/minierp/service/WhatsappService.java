package com.gomezsystems.minierp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WhatsappService {

    @Autowired
    private EvolutionApiService evolutionApiService;

    public boolean enviarMensajeTexto(String numeroDestino, String mensaje) {
        System.out.println("🚀 [G.O.M.E.Z.] Despachando mensaje WhatsApp a: " + numeroDestino);
        return evolutionApiService.enviarMensajeFiado(numeroDestino, mensaje);
    }
}