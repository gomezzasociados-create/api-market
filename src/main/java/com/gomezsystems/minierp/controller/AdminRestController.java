package com.gomezsystems.minierp.controller;

import com.gomezsystems.minierp.model.Cliente;
import com.gomezsystems.minierp.model.Producto;
import com.gomezsystems.minierp.repository.ClienteRepository;
import com.gomezsystems.minierp.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.gomezsystems.minierp.model.Ajuste;
import com.gomezsystems.minierp.model.Proveedor;
import com.gomezsystems.minierp.model.Gasto;
import com.gomezsystems.minierp.repository.AjusteRepository;
import com.gomezsystems.minierp.repository.ProveedorRepository;
import com.gomezsystems.minierp.repository.GastoRepository;
import com.gomezsystems.minierp.repository.VentaRepository;
import com.gomezsystems.minierp.model.CorteZ;
import com.gomezsystems.minierp.repository.CorteZRepository;
import com.gomezsystems.minierp.service.CobranzaRobotService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AjusteRepository ajusteRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private GastoRepository gastoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private CorteZRepository corteZRepository;

    @Autowired
    private CobranzaRobotService cobranzaRobotService;

    @Value("${app.security.admin.pin}")
    private String systemAdminPin;

    // 1. Inventario: Ajustar Stock ultra-rápido
    @PostMapping("/inventario/{id}/stock")
    public String ajustarStock(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Producto p = productoRepository.findById(id).orElse(null);
        if (p != null && payload.containsKey("nuevoStock")) {
            Double nuevoStock = Double.parseDouble(payload.get("nuevoStock").toString());
            p.setStock(nuevoStock);
            productoRepository.save(p);
            return "OK";
        }
        return "ERROR";
    }

    // 1.5 Crear Producto Manualmente
    @PostMapping("/productos")
    public String crearProductoTotal(@RequestBody Producto payload) {
        if(payload.getPrecioCompra() == null) payload.setPrecioCompra(0.0);
        if(payload.getStock() == null) payload.setStock(0.0);
        productoRepository.save(payload);
        return "OK";
    }

    // 2. Editor Maestro (Casillas): Guardar toda la fila del producto interactivo
    @PutMapping("/productos/{id}")
    public String editarProductoTotal(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Producto p = productoRepository.findById(id).orElse(null);
        if (p != null) {
            if (payload.containsKey("nombre")) p.setNombre(payload.get("nombre").toString());
            if (payload.containsKey("categoria")) p.setCategoria(payload.get("categoria").toString());
            if (payload.containsKey("descripcion")) p.setDescripcion(payload.get("descripcion").toString());
            if (payload.containsKey("imagen") && payload.get("imagen") != null) p.setImagen(payload.get("imagen").toString());

            if (payload.containsKey("precioCompra") && payload.get("precioCompra") != null) {
                p.setPrecioCompra(Double.parseDouble(payload.get("precioCompra").toString()));
            }
            if (payload.containsKey("stock") && payload.get("stock") != null) {
                p.setStock(Double.parseDouble(payload.get("stock").toString()));
            }

            if (payload.containsKey("precio")) {
                Double nuevoPrecio = Double.parseDouble(payload.get("precio").toString());
                if (p.isEsPesable()) p.setPrecioPorKilo(nuevoPrecio);
                else p.setPrecio(nuevoPrecio);
            }

            if (payload.containsKey("proveedorId") && payload.get("proveedorId") != null && !payload.get("proveedorId").toString().isEmpty()) {
                Long pId = Long.parseLong(payload.get("proveedorId").toString());
                Proveedor prov = proveedorRepository.findById(pId).orElse(null);
                p.setProveedor(prov);
            } else if (payload.containsKey("proveedorId") && (payload.get("proveedorId") == null || payload.get("proveedorId").toString().isEmpty())) {
                p.setProveedor(null);
            }

            // Motor Promo
            if (payload.containsKey("promocionActiva")) {
                p.setPromocionActiva((Boolean) payload.get("promocionActiva"));
            }
            if (payload.containsKey("precioPromo") && payload.get("precioPromo") != null && !payload.get("precioPromo").toString().isEmpty()) {
                p.setPrecioPromo(Double.parseDouble(payload.get("precioPromo").toString()));
            } else {
                p.setPrecioPromo(null);
            }
            if (payload.containsKey("fechaInicioPromo") && payload.get("fechaInicioPromo") != null && !payload.get("fechaInicioPromo").toString().isEmpty()) {
                p.setFechaInicioPromo(java.time.LocalDate.parse(payload.get("fechaInicioPromo").toString()));
            } else {
                p.setFechaInicioPromo(null);
            }
            if (payload.containsKey("fechaFinPromo") && payload.get("fechaFinPromo") != null && !payload.get("fechaFinPromo").toString().isEmpty()) {
                p.setFechaFinPromo(java.time.LocalDate.parse(payload.get("fechaFinPromo").toString()));
            } else {
                p.setFechaFinPromo(null);
            }

            // Códigos de Barras Alias
            if (payload.containsKey("codigosBarras") && payload.get("codigosBarras") instanceof List) {
                List<?> arr = (List<?>) payload.get("codigosBarras");
                java.util.Set<String> setAlias = new java.util.HashSet<>();
                for (Object st : arr) {
                    if (st != null && !st.toString().trim().isEmpty()) {
                        setAlias.add(st.toString().trim());
                    }
                }
                p.setCodigosBarras(setAlias);
            }

            productoRepository.save(p);
            return "OK";
        }
        return "ERROR";
    }

    @DeleteMapping("/productos/{id}")
    public String eliminarProductoIndividual(@PathVariable Long id) {
        if(productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            return "OK";
        }
        return "ERROR";
    }

    // 3. Calculadora de Márgenes: Guardado masivo de precios y costos propuestos
    @PutMapping("/formulas/bulk-update")
    public String actualizacionMasivaPrecio(@RequestBody List<Map<String, Object>> payload) {
        try {
            for(Map<String, Object> item : payload) {
                Long id = Long.parseLong(item.get("id").toString());
                Producto p = productoRepository.findById(id).orElse(null);

                if(p != null) {
                    if (item.containsKey("nuevoPrecio") && item.get("nuevoPrecio") != null) {
                        Double nuevoPrecio = Double.parseDouble(item.get("nuevoPrecio").toString());
                        if(p.isEsPesable()) p.setPrecioPorKilo(nuevoPrecio);
                        else p.setPrecio(nuevoPrecio);
                    }

                    if (item.containsKey("nuevoCosto") && item.get("nuevoCosto") != null) {
                        Double nuevoCosto = Double.parseDouble(item.get("nuevoCosto").toString());
                        p.setPrecioCompra(nuevoCosto);
                    }

                    productoRepository.save(p);
                }
            }
            return "OK";
        } catch(Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    // 4. Obtener clientes para darlos a elegir al fiar en POS
    @GetMapping("/clientes/autorizados")
    public List<Cliente> obtenerClientesCredito() {
        return clienteRepository.findAll();
    }

    // 5. Cobrador de Deudas para Dashboard
    @GetMapping("/deudores")
    public List<Cliente> obtenerDeudores() {
        return clienteRepository.findAll().stream()
                .filter(c -> c.getDeudaActiva() != null && c.getDeudaActiva() > 0)
                .collect(Collectors.toList());
    }

    // 6. Abonar dinero a una cuenta fiada
    @PostMapping("/deudores/{id}/abonar")
    public String abonarDeuda(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Cliente c = clienteRepository.findById(id).orElse(null);
        if (c != null && payload.containsKey("abono")) {
            Double abono = Double.parseDouble(payload.get("abono").toString());
            String metodoPago = payload.containsKey("metodoPago") ? payload.get("metodoPago").toString() : "EFECTIVO";
            
            c.setDeudaActiva(Math.max(0, c.getDeudaActiva() - abono));
            clienteRepository.save(c);

            com.gomezsystems.minierp.model.Venta v = new com.gomezsystems.minierp.model.Venta();
            v.setCliente(c);
            v.setMontoTotal(abono);
            v.setTipoPago("ABONO_" + metodoPago);
            v.setEstado("PAGADO");
            v.setDetalle("Abono de deuda. Método: " + metodoPago);
            v.setFechaHora(java.time.LocalDateTime.now());
            ventaRepository.save(v);

            // 📲 Enviar Notificación WhatsApp de Abono
            if (c.getTelefono() != null && !c.getTelefono().trim().isEmpty()) {
                String defaultMsg = "💸 Hola *[NOMBRE]*, recibimos tu abono de *$[ABONO]* mediante [METODO].\n\n¡Muchas Gracias! \n📉 Tu saldo pendiente por pagar se actualizó a: *$[DEUDA]*.";
                
                // Si el cliente tiene plantilla personalizada, usarla. Sino usar la global. Sino usar la por defecto.
                String msgTemplate = c.getPlantillaMsgAbono() != null && !c.getPlantillaMsgAbono().trim().isEmpty() 
                                     ? c.getPlantillaMsgAbono() 
                                     : ajusteRepository.findById("ROBOT_MSG_ABONO").map(a -> a.getValor()).orElse(defaultMsg);
                
                String msg = msgTemplate
                        .replace("[NOMBRE]", c.getNombre())
                        .replace("[ABONO]", String.valueOf(Math.round(abono)))
                        .replace("[METODO]", metodoPago)
                        .replace("[DEUDA]", String.valueOf(Math.round(c.getDeudaActiva())));
                
                evolutionApiService.enviarMensajeFiado(c.getTelefono(), msg);
            }

            return "OK";
        }
        return "ERROR";
    }

    // 6.5 Alta de un Nuevo Cliente Manual desde el CRM
    @PostMapping("/clientes")
    public String crearClienteManual(@RequestBody Cliente payload) {
        clienteRepository.save(payload);
        return "OK";
    }

    // 7. Modificar Cliente Completo (+ Web Fields)
    @PutMapping("/clientes/{id}")
    public String actualizarInfoDeudor(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Cliente c = clienteRepository.findById(id).orElse(null);
        if (c != null) {
            if (payload.containsKey("nombre") && payload.get("nombre") != null) c.setNombre(payload.get("nombre").toString());
            if (payload.containsKey("telefono") && payload.get("telefono") != null) c.setTelefono(payload.get("telefono").toString());
            if (payload.containsKey("email") && payload.get("email") != null) c.setEmail(payload.get("email").toString());
            if (payload.containsKey("dni") && payload.get("dni") != null) c.setDni(payload.get("dni").toString());
            if (payload.containsKey("direccion") && payload.get("direccion") != null) c.setDireccion(payload.get("direccion").toString());
            if (payload.containsKey("etiqueta") && payload.get("etiqueta") != null) c.setEtiqueta(payload.get("etiqueta").toString().toUpperCase()); // Normalizar mayúsculas

            if (payload.containsKey("fechaLimitePago") && payload.get("fechaLimitePago") != null && !payload.get("fechaLimitePago").toString().isEmpty()) {
                c.setFechaLimitePago(java.time.LocalDate.parse(payload.get("fechaLimitePago").toString()));
            } else {
                c.setFechaLimitePago(null);
            }

            if (payload.containsKey("deudaActiva")) {
                try {
                    c.setDeudaActiva(Math.max(0, Double.parseDouble(payload.get("deudaActiva").toString())));
                } catch(Exception ignored) {}
            }

            if (payload.containsKey("cupoMaximo")) {
                try {
                    c.setCupoMaximo(Double.parseDouble(payload.get("cupoMaximo").toString()));
                } catch(Exception ignored) {}
            }
            
            if (payload.containsKey("plantillaMsgCompra")) {
                c.setPlantillaMsgCompra(payload.get("plantillaMsgCompra") != null ? payload.get("plantillaMsgCompra").toString() : null);
            }
            if (payload.containsKey("plantillaMsgAbono")) {
                c.setPlantillaMsgAbono(payload.get("plantillaMsgAbono") != null ? payload.get("plantillaMsgAbono").toString() : null);
            }

            clienteRepository.save(c);
            return "OK";
        }
        return "ERROR";
    }

    // 7.6 Reversión Inteligente de Transacciones VIP (Borrado de Historial)
    @DeleteMapping("/ventas/{id}")
    public String borrarVentaHistorial(@PathVariable Long id) {
        com.gomezsystems.minierp.model.Venta v = ventaRepository.findById(id).orElse(null);
        if(v != null) {
            if(v.getCliente() != null) {
                Cliente c = v.getCliente();
                Double monto = v.getMontoTotal() != null ? v.getMontoTotal() : 0.0;
                Double deuda = c.getDeudaActiva() != null ? c.getDeudaActiva() : 0.0;
                
                if (v.getTipoPago() != null && v.getTipoPago().startsWith("ABONO_")) {
                    c.setDeudaActiva(deuda + monto); // Se borró un pago, revierte: suma deuda
                } else if ("FIADO".equals(v.getTipoPago())) {
                    c.setDeudaActiva(Math.max(0, deuda - monto)); // Se borró una compra, revierte: resta deuda
                }
                clienteRepository.save(c);
            }
            ventaRepository.deleteById(id);
            return "OK";
        }
        return "ERROR";
    }

    // 7.5 Borrar Cliente del CRM
    @DeleteMapping("/clientes/{id}")
    public String borrarCliente(@PathVariable Long id) {
        if(clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            return "OK";
        }
        return "ERROR";
    }

    // 8. Configuraciones del Robot
    @GetMapping("/config")
    public Map<String, String> getConfig() {
        Map<String, String> configs = new HashMap<>();
        ajusteRepository.findAll().forEach(a -> configs.put(a.getClave(), a.getValor()));
        return configs;
    }

    @PostMapping("/config")
    public String saveConfig(@RequestBody Map<String, String> payload) {
        payload.forEach((k, v) -> {
            Ajuste a = ajusteRepository.findById(k).orElse(new Ajuste(k, ""));
            a.setValor(v);
            ajusteRepository.save(a);
        });
        return "OK";
    }

    @PostMapping("/robot/test")
    public String triggerRobot() {
        cobranzaRobotService.ejecutarRondaMensajes();
        return "Disparo Manual Completado. Verifica la consola.";
    }

    // 9. Despacho Masivo de WhatsApp por Etiqueta
    @Autowired
    private com.gomezsystems.minierp.service.EvolutionApiService evolutionApiService;

    @PostMapping("/campana/etiqueta")
    public String enviarCampanaMasiva(@RequestBody Map<String, String> payload) {
        String etiquetaTarget = payload.get("etiqueta");
        String mensaje = payload.get("mensaje");

        if(etiquetaTarget == null || etiquetaTarget.trim().isEmpty() || mensaje == null || mensaje.trim().isEmpty()) {
            return "Faltan datos de etiqueta o mensaje.";
        }

        String targetUpperCase = etiquetaTarget.toUpperCase().trim();
        List<Cliente> prospectos = clienteRepository.findAll().stream()
                .filter(c -> c.getEtiqueta() != null && c.getEtiqueta().toUpperCase().trim().equals(targetUpperCase))
                .filter(c -> c.getTelefono() != null && !c.getTelefono().trim().isEmpty())
                .collect(Collectors.toList());

        if (prospectos.isEmpty()) return "No hay clientes con teléfono válido bajo esa etiqueta.";

        for (Cliente c : prospectos) {
            String msjFinal = mensaje.replace("[NOMBRE]", c.getNombre() != null ? c.getNombre() : "Cliente");
            if (c.getDeudaActiva() != null) msjFinal = msjFinal.replace("[MONTO]", String.valueOf(Math.round(c.getDeudaActiva())));
            else msjFinal = msjFinal.replace("[MONTO]", "0");

            evolutionApiService.enviarMensajeFiado(c.getTelefono(), msjFinal);
        }

        return "Campaña enviada a " + prospectos.size() + " clientes exitosamente.";
    }

    // ==========================================
    // MODULO: PROVEEDORES Y GASTOS (COMPRAS)
    // ==========================================

    @GetMapping("/ventas")
    public List<com.gomezsystems.minierp.model.Venta> getVentas() {
        return ventaRepository.findAll();
    }

    @GetMapping("/proveedores")
    public List<Proveedor> getProveedores() {
        return proveedorRepository.findAll();
    }

    @PostMapping("/proveedores")
    public String crearProveedor(@RequestBody Proveedor p) {
        proveedorRepository.save(p);
        return "OK";
    }

    @PostMapping("/gastos")
    public String registrarGasto(@RequestBody Map<String, Object> payload) {
        Gasto g = new Gasto();
        g.setCategoria(payload.get("categoria").toString());
        g.setDescripcion(payload.get("descripcion").toString());
        g.setMonto(Double.parseDouble(payload.get("monto").toString()));
        g.setEstado(payload.get("estado").toString());

        if (payload.containsKey("proveedorId") && payload.get("proveedorId") != null) {
            Long provId = Long.parseLong(payload.get("proveedorId").toString());
            Proveedor p = proveedorRepository.findById(provId).orElse(null);
            g.setProveedor(p);

            // Si el estado es DEUDA, sumar a deuda activa del proveedor
            if ("DEUDA".equals(g.getEstado()) && p != null) {
                p.setDeudaActiva(p.getDeudaActiva() + g.getMonto());
                proveedorRepository.save(p);
            }
        }
        gastoRepository.save(g);
        return "OK";
    }

    @GetMapping("/gastos")
    public List<Gasto> getUltimosGastos() {
        return gastoRepository.findAll().stream()
                .sorted((g1, g2) -> g2.getFechaHora().compareTo(g1.getFechaHora()))
                .limit(50)
                .collect(Collectors.toList());
    }

    @GetMapping("/caja/hoy")
    public Map<String, Object> reporteCajaHoy() {
        java.time.LocalDate hoy = java.time.LocalDate.now();

        Double ventasTotales = ventaRepository.findAll().stream()
                .filter(v -> v.getFechaHora() != null && v.getFechaHora().toLocalDate().equals(hoy))
                .mapToDouble(v -> v.getMontoTotal() != null ? v.getMontoTotal() : 0.0)
                .sum();

        Double gastosTotales = gastoRepository.findAll().stream()
                .filter(g -> "PAGADO".equals(g.getEstado()))
                .filter(g -> g.getFechaHora() != null && g.getFechaHora().toLocalDate().equals(hoy))
                .mapToDouble(g -> g.getMonto() != null ? g.getMonto() : 0.0)
                .sum();

        Double debeGeneral = clienteRepository.findAll().stream()
                .mapToDouble(c -> c.getDeudaActiva() != null ? c.getDeudaActiva() : 0.0)
                .sum();

        Double haberGeneral = proveedorRepository.findAll().stream()
                .mapToDouble(p -> p.getDeudaActiva() != null ? p.getDeudaActiva() : 0.0)
                .sum();

        Map<String, Object> rs = new HashMap<>();
        rs.put("ventas", ventasTotales);
        rs.put("gastos", gastosTotales);
        rs.put("flujoLibre", ventasTotales - gastosTotales);
        rs.put("debeGlobal", debeGeneral);
        rs.put("haberGlobal", haberGeneral);
        return rs;
    }

    // 10. Listado Histórico de Cortes Z
    @GetMapping("/cortes-z")
    public List<CorteZ> obtenerCortesZ() {
        return corteZRepository.findAllByOrderByFechaCorteDesc();
    }

    // 11. BOTÓN NUCLEAR - FACTORY RESET PARA LA NUBE
    @DeleteMapping("/system/factory-reset")
    public String factoryReset(@RequestBody Map<String, String> payload) {
        String pin = payload.get("pin");
        if (pin == null || !pin.equals(systemAdminPin)) {
            return "ERROR: PIN INCORRECTO. Abortando reseteo de fábrica.";
        }

        try {
            // Borramos todo de la bas de datos
            corteZRepository.deleteAll();
            ventaRepository.deleteAll();
            gastoRepository.deleteAll();
            ajusteRepository.deleteAll();
            productoRepository.deleteAll();
            proveedorRepository.deleteAll();
            clienteRepository.deleteAll();
            return "OK: SISTEMA RESETEADO A ESTADO DE FÁBRICA CORRECTAMENTE.";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }
}