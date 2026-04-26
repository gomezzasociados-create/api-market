package com.gomezsystems.minierp.controller;

import com.gomezsystems.minierp.model.CheckoutRequest;
import com.gomezsystems.minierp.model.CartItemDTO;
import com.gomezsystems.minierp.model.Producto;
import com.gomezsystems.minierp.model.Cliente;
import com.gomezsystems.minierp.model.Venta;
import com.gomezsystems.minierp.repository.ProductoRepository;
import com.gomezsystems.minierp.repository.ClienteRepository;
import com.gomezsystems.minierp.repository.VentaRepository;
import com.gomezsystems.minierp.service.EvolutionApiService;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private EvolutionApiService evolutionApiService; // G.O.M.E.Z. WhatsApp

    @Value("${mercadopago.access-token}")
    private String accessToken;

    // Límite base establecido para los VIP (puedes cambiarlo aquí)
    private final Double CUPO_MAXIMO_DEFECTO = 100000.0;

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, String>> procesarCheckout(@RequestBody CheckoutRequest request) {
        Map<String, String> response = new HashMap<>();
        int totalVenta = 0;
        StringBuilder detalleProductos = new StringBuilder();

        // 1. Descontar Stock, Calcular Total y Armar Lista de Productos
        if (request.getItems() != null) {
            for (CartItemDTO item : request.getItems()) {
                Optional<Producto> optProd = productoRepository.findByIdForUpdate(item.getIdProducto());
                if (optProd.isPresent()) {
                    Producto p = optProd.get();
                    try {
                        Double stockActual = p.getStock() != null ? p.getStock() : 0.0;
                        p.setStock(stockActual - item.getCantidad());
                        productoRepository.save(p);

                        int subtotal = 0;
                        String cantidadStr = "";
                        if (p.isEsPesable()) {
                            subtotal = (int)((item.getPrecio() / 1000.0) * item.getCantidad());
                            cantidadStr = String.valueOf((int)item.getCantidad().doubleValue()) + "gr ";
                        } else {
                            subtotal = (int)(item.getPrecio() * item.getCantidad());
                            cantidadStr = item.getCantidad() % 1 == 0 ? String.valueOf((int)item.getCantidad().doubleValue()) + "x " : String.valueOf(item.getCantidad()) + "x ";
                        }
                        totalVenta += subtotal;

                        detalleProductos.append("🔸 ").append(cantidadStr).append(item.getTitulo())
                                .append(" ($").append(subtotal).append(")\n");

                    } catch (Exception e) {
                        System.out.println("Error stock: " + p.getNombre());
                    }
                }
            }
        }

        String ticketFinalStr = detalleProductos.toString().trim();
        String metodo = request.getMetodoPago() != null ? request.getMetodoPago().toUpperCase() : "DESCONOCIDO";

        // 2. Crear el objeto Venta Histórico
        Venta nuevaVenta = new Venta();
        nuevaVenta.setMontoTotal((double) totalVenta);
        nuevaVenta.setTipoPago(metodo);
        nuevaVenta.setDetalle(ticketFinalStr);
        nuevaVenta.setFechaHora(LocalDateTime.now());

        // 3. Procesar Fiado y Enviar WhatsApp
        if ("FIADO".equals(metodo)) {
            if (request.getClienteId() != null) {
                Optional<Cliente> optCliente = clienteRepository.findById(request.getClienteId());
                if (optCliente.isPresent()) {
                    Cliente c = optCliente.get();

                    Double deudaAnterior = c.getDeudaActiva() != null ? c.getDeudaActiva() : 0.0;
                    Double deudaNueva = deudaAnterior + totalVenta;
                    Double limitePersonalizado = c.getCupoMaximo() != null ? c.getCupoMaximo() : CUPO_MAXIMO_DEFECTO;
                    Double cupoDisponible = Math.max(0, limitePersonalizado - deudaNueva); // Calcula lo que le queda

                    c.setDeudaActiva(deudaNueva);
                    clienteRepository.save(c);

                    nuevaVenta.setCliente(c);
                    nuevaVenta.setEstado("PENDIENTE");

                    // G.O.M.E.Z. ENVÍA WHATSAPP INMEDIATO
                    if(c.getTelefono() != null && !c.getTelefono().isEmpty()) {
                        String msjWhatsApp = "🤖 *MARKET GOMEZ SYSTEMS INFORMA*\n" +
                                "_Comprobante de compra a Crédito_\n\n" +
                                "Hola *" + c.getNombre() + "*. Se ha registrado un nuevo cargo en tu cuenta VIP:\n\n" +
                                "*🛒 Detalle de compra:*\n" + ticketFinalStr + "\n\n" +
                                "💵 *Monto de esta compra:* $" + totalVenta + "\n" +
                                "📊 *Deuda Anterior:* $" + Math.round(deudaAnterior) + "\n\n" +
                                "🔴 *NUEVA DEUDA TOTAL:* $" + Math.round(deudaNueva) + "\n" +
                                "🟢 *CUPO DISPONIBLE:* $" + Math.round(cupoDisponible) + "\n\n" +
                                "_Gracias por confiar en nosotros. Cuentas claras conservan amistades._";

                        evolutionApiService.enviarMensajeFiado(c.getTelefono(), msjWhatsApp);
                    }

                    ventaRepository.save(nuevaVenta);

                    response.put("status", "ok");
                    response.put("mensaje", "Venta registrada con éxito. Detalle enviado al WhatsApp del cliente.");
                    return ResponseEntity.ok(response);
                }
            }
            response.put("status", "error");
            response.put("mensaje", "Seleccione un cliente VIP válido.");
            return ResponseEntity.badRequest().body(response);
        }

        // 4. Guardar Venta Contado / Transferencia
        if (totalVenta > 0 && ("CONTADO".equals(metodo) || "TRANSFERENCIA".equals(metodo))) {
            nuevaVenta.setEstado("PAGADO");
            ventaRepository.save(nuevaVenta);

            response.put("status", "ok");
            response.put("mensaje", "Venta cobrada correctamente.");
            return ResponseEntity.ok(response);
        }

        // 5. MercadoPago
        if ("MERCADOPAGO".equals(metodo)) {
            try {
                MercadoPagoConfig.setAccessToken(accessToken);
                List<PreferenceItemRequest> itemsMp = new ArrayList<>();
                for (CartItemDTO item : request.getItems()) {
                    int subtotalItem = 0;
                    Optional<Producto> optProdMp = productoRepository.findById(item.getIdProducto());
                    if (optProdMp.isPresent() && optProdMp.get().isEsPesable()) {
                        subtotalItem = (int) ((item.getPrecio() / 1000.0) * item.getCantidad());
                    } else {
                        subtotalItem = (int) (item.getPrecio() * item.getCantidad());
                    }
                    itemsMp.add(PreferenceItemRequest.builder()
                            .title(item.getTitulo() + (optProdMp.isPresent() && optProdMp.get().isEsPesable() ? " (" + (int)item.getCantidad().doubleValue()  + "gr)" : " (x" + item.getCantidad() + ")"))
                            .quantity(1).unitPrice(new BigDecimal(subtotalItem)).currencyId("CLP").build());
                }
                PreferenceRequest prefRequest = PreferenceRequest.builder().items(itemsMp).build();
                Preference preference = new PreferenceClient().create(prefRequest);

                response.put("status", "mercadopago");
                response.put("init_point", preference.getInitPoint());
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                response.put("status", "error");
                response.put("mensaje", "Error MP: " + e.getMessage());
                return ResponseEntity.status(500).body(response);
            }
        }

        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/buscarCliente")
    public ResponseEntity<List<Cliente>> buscarCliente(@RequestParam String q) {
        String query = q.toLowerCase().trim();
        List<Cliente> todos = clienteRepository.findAll();
        List<Cliente> autorizados = todos.stream()
                .filter(c -> c.getEtiqueta() != null && c.getEtiqueta().toUpperCase().contains("VIP"))
                .filter(c -> (c.getNombre() != null && c.getNombre().toLowerCase().contains(query)) ||
                        (c.getDni() != null && c.getDni().toLowerCase().contains(query)) ||
                        (c.getTelefono() != null && c.getTelefono().toLowerCase().contains(query)))
                .toList();
        return ResponseEntity.ok(autorizados);
    }
}