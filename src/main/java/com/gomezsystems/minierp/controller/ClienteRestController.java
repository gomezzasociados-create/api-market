package com.gomezsystems.minierp.controller;

import com.gomezsystems.minierp.model.Cliente;
import com.gomezsystems.minierp.model.Venta; // <-- NUEVO: Para guardar el historial
import com.gomezsystems.minierp.repository.ClienteRepository;
import com.gomezsystems.minierp.repository.VentaRepository; // <-- NUEVO: Para guardar el ticket
import com.gomezsystems.minierp.repository.AjusteRepository;
import com.gomezsystems.minierp.service.WhatsappService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clientes")
public class ClienteRestController {

    @Autowired
    private ClienteRepository clienteRepository;

    // 👇 NUEVO: Inyectamos la caja registradora para guardar el historial
    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private WhatsappService whatsappService;

    @Autowired
    private AjusteRepository ajusteRepository;

    // 1. Obtener TODOS los clientes
    @GetMapping
    public List<Cliente> obtenerClientes() {
        return clienteRepository.findAll();
    }

    // 2. Obtener SOLO LOS VIP
    @GetMapping("/autorizados")
    public List<Cliente> obtenerClientesAutorizados() {
        return clienteRepository.findAll().stream()
                .filter(c -> c.getEtiqueta() != null && (c.getEtiqueta().toUpperCase().contains("VIP") || c.getEtiqueta().toUpperCase().contains("CREDITO")))
                .collect(Collectors.toList());
    }

    // 3. Guardar nuevo
    @PostMapping("/guardar")
    public Cliente guardarCliente(@RequestBody Cliente nuevoCliente) {
        return clienteRepository.save(nuevoCliente);
    }

    // 4. Actualizar Perfil
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCliente(@PathVariable Long id, @RequestBody Cliente datosActualizados) {
        return clienteRepository.findById(id).map(cliente -> {
            cliente.setNombre(datosActualizados.getNombre());
            cliente.setTelefono(datosActualizados.getTelefono());
            cliente.setEmail(datosActualizados.getEmail());
            cliente.setDni(datosActualizados.getDni());
            cliente.setDeudaActiva(datosActualizados.getDeudaActiva());
            cliente.setCupoMaximo(datosActualizados.getCupoMaximo());
            cliente.setFechaLimitePago(datosActualizados.getFechaLimitePago());
            cliente.setPlantillaMsgCompra(datosActualizados.getPlantillaMsgCompra());
            cliente.setPlantillaMsgAbono(datosActualizados.getPlantillaMsgAbono());
            cliente.setPlantillaMsgOferta(datosActualizados.getPlantillaMsgOferta());
            cliente.setEtiqueta(datosActualizados.getEtiqueta());
            clienteRepository.save(cliente);
            return ResponseEntity.ok("OK");
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. Borrar Cliente
    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<String> borrarCliente(@PathVariable Long id) {
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            return ResponseEntity.ok("Cliente eliminado con éxito");
        }
        return ResponseEntity.badRequest().body("No se encontró el cliente");
    }

    // 6. Abonar con Motor WhatsApp e HISTORIAL (Corregido)
    @PostMapping("/{id}/abonar")
    public ResponseEntity<?> registrarAbono(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente == null) return ResponseEntity.badRequest().body("Cliente no encontrado");

        Double montoAbono = Double.valueOf(payload.get("abono").toString());
        String metodo = payload.get("metodoPago") != null ? payload.get("metodoPago").toString().toUpperCase() : "EFECTIVO";

        // Calculamos la deuda ANTES del abono
        Double deudaAnterior = cliente.getDeudaActiva() != null ? cliente.getDeudaActiva() : 0.0;

        // Calculamos la NUEVA deuda (sin bajar de 0)
        Double nuevaDeuda = Math.max(0.0, deudaAnterior - montoAbono);

        // Guardamos el nuevo saldo en el perfil
        cliente.setDeudaActiva(nuevaDeuda);
        clienteRepository.save(cliente);

        // =================================================================
        // 👇 ESTA ES LA PIEZA CLAVE: GUARDAR EL ABONO EN EL HISTORIAL 👇
        // =================================================================
        try {
            Venta registroAbono = new Venta();
            registroAbono.setMontoTotal(montoAbono);
            registroAbono.setTipoPago("ABONO_" + metodo);
            registroAbono.setEstado("PAGADO");
            registroAbono.setCliente(cliente);
            registroAbono.setDetalle("Abono a deuda VIP (" + metodo + ")");
            registroAbono.setFechaHora(java.time.LocalDateTime.now());
            registroAbono.setCierreAplicado(false);

            ventaRepository.save(registroAbono);
            System.out.println("✅ [DEBUG] Historial de abono guardado correctamente en Caja (Ticket ID: " + registroAbono.getId() + ").");
        } catch (Exception ex) {
            System.err.println("⚠️ [DEBUG] Error guardando historial de abono: " + ex.getMessage());
        }
        // =================================================================

        // --- MOTOR G.O.M.E.Z. ---
        try {
            if (cliente.getTelefono() != null && !cliente.getTelefono().trim().isEmpty()) {
                Double limitePersonalizado = cliente.getCupoMaximo() != null ? cliente.getCupoMaximo() : 100000.0;
                Double cupoDisponible = Math.max(0.0, limitePersonalizado - nuevaDeuda);

                String defaultMsg = "🤖 *MARKET GOMEZ SYSTEMS INFORMA*\n" +
                        "━━━━━━━━━━━━━━━━━━━━\n" +
                        "✅ *COMPROBANTE DE ABONO / PAGO VIP* ✅\n\n" +
                        "Hola *[NOMBRE]*, hemos procesado tu abono exitosamente:\n\n" +
                        "💵 *Monto abonado:* $[MONTO] ([METODO])\n" +
                        "📊 *Deuda anterior:* $[DEUDA_ANTERIOR]\n" +
                        "📉 *NUEVO SALDO PENDIENTE:* $[DEUDA]\n" +
                        "🟢 *CUPO DISPONIBLE:* $[CUPO_DISPONIBLE]\n" +
                        "━━━━━━━━━━━━━━━━━━━━\n" +
                        "_¡Muchas gracias por tu pago y puntualidad!_";

                String plantillaAUsar = (cliente.getPlantillaMsgAbono() != null && !cliente.getPlantillaMsgAbono().trim().isEmpty())
                        ? cliente.getPlantillaMsgAbono()
                        : ajusteRepository.findById("ROBOT_MSG_ABONO")
                                .map(a -> a.getValor())
                                .filter(val -> val != null && !val.trim().isEmpty())
                                .orElse(defaultMsg);

                String mensajeFinal = plantillaAUsar
                        .replace("[NOMBRE]", cliente.getNombre() != null ? cliente.getNombre() : "Cliente")
                        .replace("[MONTO]", String.valueOf(Math.round(montoAbono)))
                        .replace("[ABONO]", String.valueOf(Math.round(montoAbono)))
                        .replace("[METODO]", metodo)
                        .replace("[DEUDA_ANTERIOR]", String.valueOf(Math.round(deudaAnterior)))
                        .replace("[DEUDA]", String.valueOf(Math.round(nuevaDeuda)))
                        .replace("[CUPO_DISPONIBLE]", String.valueOf(Math.round(cupoDisponible)))
                        .replace("[CUPO_MAXIMO]", String.valueOf(Math.round(limitePersonalizado)));

                whatsappService.enviarMensajeTexto(cliente.getTelefono(), mensajeFinal);
            }
        } catch (Exception e) {
            System.err.println("⚠️ [G.O.M.E.Z.] Error enviando WhatsApp de abono: " + e.getMessage());
        }

        return ResponseEntity.ok("OK");
    }
}