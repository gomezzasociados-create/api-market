package com.gomezsystems.minierp.service;

import com.gomezsystems.minierp.model.Ajuste;
import com.gomezsystems.minierp.model.Cliente;
import com.gomezsystems.minierp.repository.AjusteRepository;
import com.gomezsystems.minierp.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class MarketingRobotService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AjusteRepository ajusteRepository;

    @Autowired
    private EvolutionApiService evolutionApiService;

    // Se ejecutará cada minuto cron para ver si la hora coincide EXACTAMENTE con lo programado.
    @Scheduled(cron = "0 * * * * *")
    public void escaneoRobotMarketing() {
        String estado = ajusteRepository.findById("ROBOT_MKT_ACTIVO")
                .map(Ajuste::getValor)
                .orElseGet(() -> ajusteRepository.findById("ROBOT_OFERTA_ACTIVO")
                        .map(Ajuste::getValor)
                        .orElse("OFF"));

        if (!"ON".equalsIgnoreCase(estado) && !"true".equalsIgnoreCase(estado)) {
            return;
        }

        String horaStr = ajusteRepository.findById("ROBOT_MKT_HORA")
                .map(Ajuste::getValor)
                .orElseGet(() -> ajusteRepository.findById("ROBOT_OFERTA_HORA")
                        .map(Ajuste::getValor)
                        .orElse("18:00"));
        
        LocalTime horaActual = LocalTime.now();
        LocalTime horaConfigurada;
        try {
            horaConfigurada = LocalTime.parse(horaStr.trim());
        } catch (Exception e) {
            horaConfigurada = LocalTime.of(18, 0);
        }

        if (horaActual.getHour() == horaConfigurada.getHour() && horaActual.getMinute() == horaConfigurada.getMinute()) {
            System.out.println("🤖 ROBOT MKT: Iniciando bombardeo de marketing psicológico...");
            ejecutarCampanaVip();
        }
    }

    public void ejecutarCampanaVip() {
        String etiquetaTarget = ajusteRepository.findById("ROBOT_MKT_TARGET")
                .map(Ajuste::getValor)
                .orElseGet(() -> ajusteRepository.findById("ROBOT_OFERTA_ETIQUETA")
                        .map(Ajuste::getValor)
                        .orElse("VIP")).trim().toUpperCase();

        String plantilla = ajusteRepository.findById("ROBOT_MKT_PLANTILLA")
                .map(Ajuste::getValor)
                .orElseGet(() -> ajusteRepository.findById("ROBOT_OFERTA_PLANTILLA")
                        .map(Ajuste::getValor)
                        .orElse("¡Hola [NOMBRE]! Te seleccionamos por ser cliente [ETIQUETA]. Tenemos una oferta relámpago con un 20% de descuento solo por hoy."));

        List<Cliente> clientes = clienteRepository.findAll();
        int count = 0;

        for (Cliente c : clientes) {
            if (c.getEtiqueta() != null && c.getEtiqueta().trim().toUpperCase().equals(etiquetaTarget)) {
                if (c.getTelefono() != null && !c.getTelefono().trim().isEmpty()) {
                    String msj = plantilla;
                    msj = msj.replace("[NOMBRE]", c.getNombre() != null ? c.getNombre() : "Cliente");
                    msj = msj.replace("[ETIQUETA]", etiquetaTarget);
                    
                    // Llama a WhatsApp para inyectar publicidad pasiva
                    evolutionApiService.enviarMensajeFiado(c.getTelefono(), msj);
                    count++;
                }
            }
        }
        System.out.println("🚀 ROBOT MKT: Ráfaga completada. " + count + " mensajes despachados al Tag: " + etiquetaTarget);
    }
}
