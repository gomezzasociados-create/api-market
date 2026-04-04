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
        Ajuste encendido = ajusteRepository.findById("ROBOT_MKT_ACTIVO").orElse(new Ajuste("ROBOT_MKT_ACTIVO", "OFF"));
        if (!"ON".equalsIgnoreCase(encendido.getValor())) {
            return;
        }

        Ajuste horaAjuste = ajusteRepository.findById("ROBOT_MKT_HORA").orElse(new Ajuste("ROBOT_MKT_HORA", "18:00"));
        
        LocalTime horaActual = LocalTime.now();
        LocalTime horaConfigurada;
        try {
            horaConfigurada = LocalTime.parse(horaAjuste.getValor());
        } catch (Exception e) {
            horaConfigurada = LocalTime.of(18, 0);
        }

        if (horaActual.getHour() == horaConfigurada.getHour() && horaActual.getMinute() == horaConfigurada.getMinute()) {
            System.out.println("🤖 ROBOT MKT: Iniciando bombardeo de marketing psicológico...");
            ejecutarCampanaVip();
        }
    }

    public void ejecutarCampanaVip() {
        Ajuste etiquetaAjuste = ajusteRepository.findById("ROBOT_MKT_TARGET").orElse(new Ajuste("ROBOT_MKT_TARGET", "VIP"));
        Ajuste plantillaAjuste = ajusteRepository.findById("ROBOT_MKT_PLANTILLA")
                .orElse(new Ajuste("ROBOT_MKT_PLANTILLA", "¡Hola [NOMBRE]! Te seleccionamos por ser cliente [ETIQUETA]. Tenemos una oferta relámpago con un 20% de descuento solo por hoy."));
                
        String etiquetaTarget = etiquetaAjuste.getValor().trim().toUpperCase();
        String plantilla = plantillaAjuste.getValor();

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
