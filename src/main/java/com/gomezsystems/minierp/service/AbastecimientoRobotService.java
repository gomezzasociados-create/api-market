package com.gomezsystems.minierp.service;

import com.gomezsystems.minierp.model.Ajuste;
import com.gomezsystems.minierp.model.Producto;
import com.gomezsystems.minierp.repository.AjusteRepository;
import com.gomezsystems.minierp.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AbastecimientoRobotService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private AjusteRepository ajusteRepository;

    @Autowired
    private EvolutionApiService evolutionApiService;

    // Cron checking every minute for the admin configured hour (08:00 AM)
    @Scheduled(cron = "0 * * * * *")
    public void vigilarInventario() {
        boolean robotAlertaActivo = "ON".equalsIgnoreCase(ajusteRepository.findById("ROBOT_ALERTA_ESTADO").map(Ajuste::getValor).orElse("OFF"));
        if (!robotAlertaActivo) return;

        String horaAviso = ajusteRepository.findById("ROBOT_ALERTA_HORA").map(Ajuste::getValor).orElse("08:00");
        LocalTime ahora = LocalTime.now();
        LocalTime horaTarget = LocalTime.parse(horaAviso);

        // Si estamos en el mismo minuto
        if (ahora.getHour() == horaTarget.getHour() && ahora.getMinute() == horaTarget.getMinute()) {
            
            // Buscar productos donde stock <= 5
            List<Producto> depleting = productoRepository.findAll().stream()
                    .filter(p -> p.getStock() != null && p.getStock() <= 5)
                    .collect(Collectors.toList());

            if (depleting.isEmpty()) return;

            String telefonoAdmin = ajusteRepository.findById("ROBOT_ALERTA_TELEFONO").map(Ajuste::getValor).orElse(null);
            if(telefonoAdmin == null || telefonoAdmin.isEmpty()) return;

            StringBuilder msg = new StringBuilder();
            msg.append("🚨 *REPORTE CENTINELA GOMEZ MARKET* 🚨\n\n");
            msg.append("Jefe, los siguientes artículos están quebrando stock:\n\n");
            
            for(Producto p : depleting) {
                msg.append("📦 *").append(p.getNombre()).append("* (Quedan: ").append(p.getStock()).append(")\n");
                if (p.getProveedor() != null) {
                    msg.append("   📞 Sugerencia: Contactar a ").append(p.getProveedor().getEmpresa());
                    if (p.getProveedor().getVendedorContacto() != null && !p.getProveedor().getVendedorContacto().isEmpty()) {
                        msg.append(" (").append(p.getProveedor().getVendedorContacto()).append(")");
                    }
                    if (p.getProveedor().getTelefono() != null && !p.getProveedor().getTelefono().isEmpty()) {
                        msg.append("\n   📲 wa.me/").append(p.getProveedor().getTelefono().replace("+", "").replace(" ", ""));
                    }
                    msg.append("\n");
                } else {
                    msg.append("   ⚠️ (Sin proveedor enlazado)\n");
                }
                msg.append("\n");
            }
            
            msg.append("_Robot ERP automatizado._");

            // Enviar WhatsApp al Admin
            evolutionApiService.enviarMensajeFiado(telefonoAdmin, msg.toString());
            System.out.println("Robot Centinela: Reporte enviado exitosamente.");
        }
    }
}
