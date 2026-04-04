package com.gomezsystems.minierp.service;

import com.gomezsystems.minierp.model.Ajuste;
import com.gomezsystems.minierp.model.Cliente;
import com.gomezsystems.minierp.repository.AjusteRepository;
import com.gomezsystems.minierp.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CobranzaRobotService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AjusteRepository ajusteRepository;

    @Autowired
    private EvolutionApiService evolutionApiService;

    // Se ejecutará cada minuto cron para ver si la hora coincide EXACTAMENTE con lo que quiere el usuario.
    @Scheduled(cron = "0 * * * * *")
    public void escaneoRobotCobranzas() {
        Ajuste encendido = ajusteRepository.findById("ROBOT_COBRANZA_ACTIVO").orElse(new Ajuste("ROBOT_COBRANZA_ACTIVO", "false"));
        if (!"true".equalsIgnoreCase(encendido.getValor())) {
            return;
        }

        Ajuste horaAjuste = ajusteRepository.findById("ROBOT_HORA_ENVIO").orElse(new Ajuste("ROBOT_HORA_ENVIO", "10:00"));
        
        LocalTime horaActual = LocalTime.now();
        LocalTime horaConfigurada;
        try {
            horaConfigurada = LocalTime.parse(horaAjuste.getValor());
        } catch (Exception e) {
            horaConfigurada = LocalTime.of(10, 0);
        }

        // Si es el minuto exacto
        if (horaActual.getHour() == horaConfigurada.getHour() && horaActual.getMinute() == horaConfigurada.getMinute()) {
            System.out.println("🤖 ROBOT COBRANZAS: Hora coincidente. Iniciando despachos...");
            ejecutarRondaMensajes();
        }
    }

    public void ejecutarRondaMensajes() {
        Ajuste plantillaAjuste = ajusteRepository.findById("ROBOT_PLANTILLA_MSJ")
                .orElse(new Ajuste("ROBOT_PLANTILLA_MSJ", "⚠️ Hola [NOMBRE], le informamos que su saldo de $[MONTO] vence hoy [FECHA]. Por favor acerquese a nuestra sucursal."));
                
        String plantilla = plantillaAjuste.getValor();
        LocalDate hoy = LocalDate.now();

        List<Cliente> deudores = clienteRepository.findAll();

        for (Cliente c : deudores) {
            // Filtrar reales deudores con fecha limite igual a Hoy
            if (c.getDeudaActiva() != null && c.getDeudaActiva() > 0 && c.getFechaLimitePago() != null) {
                if (c.getFechaLimitePago().isEqual(hoy) || c.getFechaLimitePago().isBefore(hoy)) {
                    
                    if (c.getTelefono() != null && !c.getTelefono().trim().isEmpty()) {
                        String msj = plantilla;
                        msj = msj.replace("[NOMBRE]", c.getNombre() != null ? c.getNombre() : "Cliente");
                        msj = msj.replace("[MONTO]", String.valueOf(Math.round(c.getDeudaActiva())));
                        msj = msj.replace("[FECHA]", c.getFechaLimitePago().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                        
                        // Llama a WhatsApp
                        evolutionApiService.enviarMensajeFiado(c.getTelefono(), msj);
                    }
                }
            }
        }
    }
}
