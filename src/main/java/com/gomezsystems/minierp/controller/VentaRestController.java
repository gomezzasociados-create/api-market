package com.gomezsystems.minierp.controller;

import com.gomezsystems.minierp.model.Cliente;
import com.gomezsystems.minierp.model.Venta;
import com.gomezsystems.minierp.model.Producto;
import com.gomezsystems.minierp.model.CorteZ;
import com.gomezsystems.minierp.repository.ClienteRepository;
import com.gomezsystems.minierp.repository.VentaRepository;
import com.gomezsystems.minierp.repository.CorteZRepository;
import com.gomezsystems.minierp.repository.ProductoRepository;
import com.gomezsystems.minierp.repository.AjusteRepository;
import com.gomezsystems.minierp.service.WhatsappService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
public class VentaRestController {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private WhatsappService whatsappService;

    @Autowired
    private AjusteRepository ajusteRepository;

    @Autowired
    private CorteZRepository corteZRepository;

    @PostMapping("/procesar")
    public String procesarVenta(@RequestBody Map<String, Object> payload) {
        try {
            System.out.println("🔍 [DEBUG-CAJA] Recibiendo nueva venta desde el POS/Web...");

            Double total = Double.parseDouble(payload.get("total").toString());
            if(total <= 0) return "CARRITO_VACIO";

            String tipo = payload.get("tipo") != null ? payload.get("tipo").toString().toUpperCase() : "CONTADO";
            String detalle = payload.get("detalle") != null ? payload.get("detalle").toString() : "Venta POS";

            System.out.println("🔍 [DEBUG-CAJA] Tipo de pago recibido: " + tipo);

            // DEDUCCIÓN DE INVENTARIO FÍSICO
            if (payload.containsKey("items")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
                for (Map<String, Object> item : items) {
                    Long prodId = Long.parseLong(item.get("id").toString());
                    Double cantidad = Double.parseDouble(item.get("cantidad").toString());
                    Producto p = productoRepository.findById(prodId).orElse(null);
                    if (p != null) {
                        p.setStock(p.getStock() - cantidad);
                        productoRepository.save(p);
                    }
                }
            }

            Venta v = new Venta();
            v.setMontoTotal(total);
            v.setDetalle(detalle);

            // Aceptamos tanto "FIADO" como "CREDITO"
            if ("FIADO".equals(tipo) || "CREDITO".equals(tipo)) {

                if (!payload.containsKey("clienteId") || payload.get("clienteId") == null) {
                    System.out.println("❌ [DEBUG-CAJA] Abortado: No se envió el ID del cliente.");
                    return "ERROR_CLIENTE_NO_ENVIADO";
                }

                Long clienteId = Long.parseLong(payload.get("clienteId").toString());
                Cliente c = clienteRepository.findById(clienteId).orElse(null);

                if (c != null) {
                    System.out.println("🔍 [DEBUG-CAJA] Cliente detectado: " + c.getNombre());

                    v.setTipoPago("FIADO");
                    v.setEstado("PENDIENTE");
                    v.setCliente(c);

                    // ACTUALIZAR LA DEUDA EN BD
                    Double deudaAnterior = c.getDeudaActiva() != null ? c.getDeudaActiva() : 0.0;
                    c.setDeudaActiva(deudaAnterior + total);
                    clienteRepository.save(c);

                    // ========================================================
                    // 🛡️ BLINDAJE DEL MOTOR G.O.M.E.Z. (TRY-CATCH SEPARADO)
                    // ========================================================
                    try {
                        if (c.getTelefono() != null && !c.getTelefono().trim().isEmpty()) {
                            System.out.println("✅ [DEBUG-CAJA] Preparando WhatsApp para " + c.getTelefono());

                            String defaultMsg = "🛒 Hola [NOMBRE], registramos una nueva compra.\nDetalle:\n[DETALLE]\nTotal compra: $[TOTAL]\nDeuda anterior: $[DEUDA_ANTERIOR]\nDeuda total actualizada: $[DEUDA]";

                            String msgTemplate = (c.getPlantillaMsgCompra() != null && !c.getPlantillaMsgCompra().trim().isEmpty())
                                    ? c.getPlantillaMsgCompra()
                                    : ajusteRepository.findById("ROBOT_MSG_COMPRA").map(a -> a.getValor()).orElse(defaultMsg);

                            String msg = msgTemplate
                                    .replace("[NOMBRE]", c.getNombre() != null ? c.getNombre() : "Cliente")
                                    .replace("[TOTAL]", String.valueOf(Math.round(total)))
                                    .replace("[DETALLE]", detalle)
                                    .replace("[DEUDA_ANTERIOR]", String.valueOf(Math.round(deudaAnterior)))
                                    .replace("[DEUDA]", String.valueOf(Math.round(c.getDeudaActiva())));

                            whatsappService.enviarMensajeTexto(c.getTelefono(), msg);
                            System.out.println("✅ [DEBUG-CAJA] Orden despachada a Evolution.");
                        } else {
                            System.out.println("⚠️ [DEBUG-CAJA] El cliente no tiene teléfono. Se omite el WhatsApp.");
                        }
                    } catch (Exception wsEx) {
                        // Si falla la plantilla o el envío, LA VENTA Y EL ABONO SE SALVAN
                        System.err.println("⚠️ [DEBUG-CAJA] Error construyendo/enviando WhatsApp: " + wsEx.getMessage());
                    }
                    // ========================================================

                } else {
                    System.out.println("❌ [DEBUG-CAJA] Abortado: El ID (" + clienteId + ") no existe.");
                    return "ERROR_CLIENTE";
                }
            } else {
                System.out.println("🔍 [DEBUG-CAJA] Venta procesada como: " + tipo);
                // CORRECCIÓN BUG: Ahora respeta si es TRANSFERENCIA, MERCADOPAGO o CONTADO
                v.setTipoPago(tipo);
                v.setEstado("PAGADO");
            }

            // AHORA SÍ: Guardamos la venta en el registro general de la caja
            ventaRepository.save(v);
            return "OK";

        } catch (Exception e) {
            System.err.println("❌ [DEBUG-CAJA] Excepción crítica general: " + e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/caja-fuerte")
    public Map<String, Object> estadoCajaFuerte() {
        List<Venta> activas = ventaRepository.findByCierreAplicadoFalseOrderByFechaHoraDesc();
        Double totalEfectivo = 0.0; Double totalFiado = 0.0; Double totalTarjeta = 0.0;
        for (Venta v : activas) {
            if ("CONTADO".equals(v.getTipoPago()) || "EFECTIVO".equals(v.getTipoPago())) totalEfectivo += v.getMontoTotal();
            else if ("FIADO".equals(v.getTipoPago())) totalFiado += v.getMontoTotal();
            else if ("TARJETA".equals(v.getTipoPago()) || "TRANSFERENCIA".equals(v.getTipoPago()) || "MERCADOPAGO".equals(v.getTipoPago())) totalTarjeta += v.getMontoTotal();
        }
        return Map.of("ventas", activas, "totalEfectivo", totalEfectivo, "totalFiado", totalFiado, "totalTarjeta", totalTarjeta);
    }

    @PostMapping("/ejecutar-corte")
    public String ejecutarCorteCaja() {
        try {
            List<Venta> activas = ventaRepository.findByCierreAplicadoFalseOrderByFechaHoraDesc();
            if(activas.isEmpty()) return "SIN_VENTAS";
            Double tEfectivo = 0.0; Double tFiado = 0.0; Double tTarjeta = 0.0;
            for(Venta v : activas) {
                if("CONTADO".equals(v.getTipoPago()) || "EFECTIVO".equals(v.getTipoPago())) tEfectivo += v.getMontoTotal();
                else if("FIADO".equals(v.getTipoPago())) tFiado += v.getMontoTotal();
                else if("TARJETA".equals(v.getTipoPago()) || "TRANSFERENCIA".equals(v.getTipoPago()) || "MERCADOPAGO".equals(v.getTipoPago())) tTarjeta += v.getMontoTotal();
                v.setCierreAplicado(true);
            }
            ventaRepository.saveAll(activas);
            CorteZ corte = new CorteZ();
            corte.setTotalEfectivo(tEfectivo); corte.setTotalFiado(tFiado); corte.setTotalTarjeta(tTarjeta);
            corte.setDetalleInforme("Cierre automático generado con " + activas.size() + " tickets aprobados.");
            corteZRepository.save(corte);
            return "OK";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}