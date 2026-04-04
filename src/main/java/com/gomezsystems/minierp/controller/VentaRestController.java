package com.gomezsystems.minierp.controller;

import com.gomezsystems.minierp.model.Cliente;
import com.gomezsystems.minierp.model.Venta;
import com.gomezsystems.minierp.model.Producto;
import com.gomezsystems.minierp.model.CorteZ;
import com.gomezsystems.minierp.repository.ClienteRepository;
import com.gomezsystems.minierp.repository.VentaRepository;
import com.gomezsystems.minierp.repository.CorteZRepository;
import com.gomezsystems.minierp.repository.ProductoRepository;
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
    private com.gomezsystems.minierp.service.EvolutionApiService evolutionApiService;

    @Autowired
    private com.gomezsystems.minierp.repository.AjusteRepository ajusteRepository;

    @Autowired
    private CorteZRepository corteZRepository;

    @PostMapping("/procesar")
    public String procesarVenta(@RequestBody Map<String, Object> payload) {
        try {
            Double total = Double.parseDouble(payload.get("total").toString());
            
            // SECURITY: Bloquear carritos falsos de $0 para no llenar la base de datos de basura.
            if(total <= 0) return "CARRITO_VACIO";

            String tipo = payload.get("tipo").toString(); // "CONTADO" o "FIADO"
            String detalle = payload.get("detalle").toString();
            
            // DEDUCCIÓN DE INVENTARIO FÍSICO
            if (payload.containsKey("items")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
                for (Map<String, Object> item : items) {
                    Long prodId = Long.parseLong(item.get("id").toString());
                    Double cantidad = Double.parseDouble(item.get("cantidad").toString());

                    Producto p = productoRepository.findById(prodId).orElse(null);
                    if (p != null) {
                        double nuevoStock = p.getStock() - cantidad;
                        // Prevenir stock lógicamente menor a cero (Opcionalmente, si prefieres permitir negativos para cuadrar despues, quita el Math.max)
                        // Para este engine blindado, permitiremos negativos por si hay error de conteo físico sigan vendiendo.
                        p.setStock(nuevoStock);
                        productoRepository.save(p);
                    }
                }
            }

            Venta v = new Venta();
            v.setMontoTotal(total);
            v.setDetalle(detalle);

            if ("FIADO".equals(tipo)) {
                Long clienteId = Long.parseLong(payload.get("clienteId").toString());
                Cliente c = clienteRepository.findById(clienteId).orElse(null);
                if (c != null) {
                    v.setTipoPago("FIADO");
                    v.setEstado("PENDIENTE");
                    v.setCliente(c);
                    
                    // Sumamos deuda al cliente garantizando integridad
                    Double deudaActual = c.getDeudaActiva() != null ? c.getDeudaActiva() : 0.0;
                    c.setDeudaActiva(deudaActual + total);
                    clienteRepository.save(c);

                    // 📲 Enviar Notificación WhatsApp al Fiado
                    if (c.getTelefono() != null && !c.getTelefono().trim().isEmpty()) {
                        String defaultMsg = "🛒 Hola *[NOMBRE]*, se cargó a tu cuenta una suma de *$[TOTAL]*.\n\nDetalle de los artículos:\n_[DETALLE]_\n\n📉 Tu deuda total pendiente es: *$[DEUDA]*.";
                        
                        String msgTemplate = c.getPlantillaMsgCompra() != null && !c.getPlantillaMsgCompra().trim().isEmpty() 
                                             ? c.getPlantillaMsgCompra() 
                                             : ajusteRepository.findById("ROBOT_MSG_COMPRA").map(a -> a.getValor()).orElse(defaultMsg);
                        
                        String msg = msgTemplate
                                .replace("[NOMBRE]", c.getNombre())
                                .replace("[TOTAL]", String.valueOf(Math.round(total)))
                                .replace("[DETALLE]", detalle)
                                .replace("[DEUDA]", String.valueOf(Math.round(c.getDeudaActiva())));
                                
                        evolutionApiService.enviarMensajeFiado(c.getTelefono(), msg);
                    }
                } else {
                    return "ERROR_CLIENTE";
                }
            } else {
                v.setTipoPago("EFECTIVO");
                v.setEstado("PAGADO");
            }

            ventaRepository.save(v);
            return "OK";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/caja-fuerte")
    public Map<String, Object> estadoCajaFuerte() {
        List<Venta> activas = ventaRepository.findByCierreAplicadoFalseOrderByFechaHoraDesc();
        Double totalEfectivo = 0.0;
        Double totalFiado = 0.0;
        Double totalTarjeta = 0.0;

        for (Venta v : activas) {
            if ("EFECTIVO".equals(v.getTipoPago())) {
                totalEfectivo += v.getMontoTotal();
            } else if ("FIADO".equals(v.getTipoPago())) {
                totalFiado += v.getMontoTotal();
            } else if ("TARJETA".equals(v.getTipoPago())) {
                totalTarjeta += v.getMontoTotal();
            }
        }

        return Map.of(
            "ventas", activas,
            "totalEfectivo", totalEfectivo,
            "totalFiado", totalFiado,
            "totalTarjeta", totalTarjeta
        );
    }

    @PostMapping("/ejecutar-corte")
    public String ejecutarCorteCaja() {
        try {
            List<Venta> activas = ventaRepository.findByCierreAplicadoFalseOrderByFechaHoraDesc();
            if(activas.isEmpty()) return "SIN_VENTAS";
            
            Double tEfectivo = 0.0;
            Double tFiado = 0.0;
            Double tTarjeta = 0.0;
            for(Venta v : activas) {
                if("EFECTIVO".equals(v.getTipoPago())) tEfectivo += v.getMontoTotal();
                else if("FIADO".equals(v.getTipoPago())) tFiado += v.getMontoTotal();
                else if("TARJETA".equals(v.getTipoPago())) tTarjeta += v.getMontoTotal();
                
                v.setCierreAplicado(true);
            }
            ventaRepository.saveAll(activas);

            CorteZ corte = new CorteZ();
            corte.setTotalEfectivo(tEfectivo);
            corte.setTotalFiado(tFiado);
            corte.setTotalTarjeta(tTarjeta);
            corte.setDetalleInforme("Cierre automático generado con " + activas.size() + " tickets aprobados.");
            corteZRepository.save(corte);

            return "OK";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
