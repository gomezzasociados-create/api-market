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
            cliente.setPlantillaMsgCompra(datosActualizados.getPlantillaMsgCompra());
            cliente.setPlantillaMsgAbono(datosActualizados.getPlantillaMsgAbono());
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

        // Calculamos la deuda ANTES del abono
        Double deudaAnterior = cliente.getDeudaActiva() != null ? cliente.getDeudaActiva() : 0.0;

        // Calculamos la NUEVA deuda
        Double nuevaDeuda = deudaAnterior - montoAbono;

        // Guardamos el nuevo saldo en el perfil
        cliente.setDeudaActiva(nuevaDeuda);
        clienteRepository.save(cliente);

        // =================================================================
        // 👇 ESTA ES LA PIEZA CLAVE: GUARDAR EL ABONO EN EL HISTORIAL 👇
        // =================================================================
        try {
            Venta registroAbono = new Venta();
            registroAbono.setMontoTotal(montoAbono);

            // Si el frontend envía el método (Efectivo, Transf, etc), lo tomamos, si no, es Efectivo
            String metodo = payload.get("metodoPago") != null ? payload.get("metodoPago").toString().toUpperCase() : "EFECTIVO";

            // Le ponemos ABONO_ para que el frontend lo reconozca y pinte de verde
            registroAbono.setTipoPago("ABONO_" + metodo);
            registroAbono.setEstado("PAGADO");
            registroAbono.setCliente(cliente);
            registroAbono.setDetalle("Abono a deuda VIP (" + metodo + ")");

            ventaRepository.save(registroAbono);
            System.out.println("✅ [DEBUG] Historial de abono guardado correctamente en Caja.");
        } catch (Exception ex) {
            System.err.println("⚠️ [DEBUG] Error guardando historial de abono: " + ex.getMessage());
        }
        // =================================================================

        // --- MOTOR G.O.M.E.Z. ---
        try {
            if (cliente.getTelefono() != null && !cliente.getTelefono().trim().isEmpty()) {

                String plantillaAUsar = (cliente.getPlantillaMsgAbono() != null && !cliente.getPlantillaMsgAbono().trim().isEmpty())
                        ? cliente.getPlantillaMsgAbono()
                        : ajusteRepository.findById("ROBOT_PLANTILLA_MSJ").map(a -> a.getValor()).orElse("✅ Hola [NOMBRE], recibimos tu abono de $[MONTO].");

                String mensajeFinal = plantillaAUsar
                        .replace("[NOMBRE]", cliente.getNombre() != null ? cliente.getNombre() : "Cliente")
                        .replace("[MONTO]", String.valueOf(Math.round(montoAbono)))
                        .replace("[DEUDA_ANTERIOR]", String.valueOf(Math.round(deudaAnterior)))
                        .replace("[DEUDA]", String.valueOf(Math.round(nuevaDeuda)));

                whatsappService.enviarMensajeTexto(cliente.getTelefono(), mensajeFinal);
            }
        } catch (Exception e) {
            System.err.println("⚠️ [G.O.M.E.Z.] Error enviando WhatsApp: " + e.getMessage());
        }

        return ResponseEntity.ok("OK");
    }
}