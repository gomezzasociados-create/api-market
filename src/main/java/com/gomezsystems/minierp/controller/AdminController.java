package com.gomezsystems.minierp.controller;

import com.gomezsystems.minierp.model.Producto;
import com.gomezsystems.minierp.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
public class AdminController {

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping("/admin")
    public String renderAdminDashboard() {
        return "admin"; // Devuelve admin.html
    }

    // =======================================================
    // 1. BOTÓN ROJO: PURGA MASIVA DE CATÁLOGO
    // =======================================================
    @DeleteMapping("/api/admin/inventario/purgar")
    @Transactional
    @ResponseBody
    public ResponseEntity<String> purgarInventario() {
        try {
            productoRepository.deleteAll();
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en purga: " + e.getMessage());
        }
    }

    // =======================================================
    // 2. DEDUPLICADOR INTELIGENTE DE CATÁLOGO
    // =======================================================
    @PostMapping("/api/admin/inventario/deduplicar")
    @Transactional
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deduplicarCatalogo() {
        try {
            List<Producto> todos = productoRepository.findAll();
            int totalAntes = todos.size();

            Map<String, List<Producto>> agrupados = new LinkedHashMap<>();
            for (Producto p : todos) {
                String key = normalizarTexto(p.getNombre());
                if (key.isEmpty()) {
                    key = "sin_nombre_" + p.getId();
                }
                agrupados.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
            }

            List<Producto> aEliminar = new ArrayList<>();
            List<Producto> aGuardar = new ArrayList<>();

            for (List<Producto> copias : agrupados.values()) {
                if (copias.size() > 1) {
                    copias.sort(Comparator.comparing(Producto::getId));
                    Producto principal = copias.get(0);

                    for (int i = 1; i < copias.size(); i++) {
                        Producto copia = copias.get(i);
                        if ((principal.getImagen() == null || principal.getImagen().isEmpty()) && copia.getImagen() != null) {
                            principal.setImagen(copia.getImagen());
                        }
                        if ((principal.getDescripcion() == null || principal.getDescripcion().isEmpty()) && copia.getDescripcion() != null) {
                            principal.setDescripcion(copia.getDescripcion());
                        }
                        if (copia.getCodigosBarras() != null && !copia.getCodigosBarras().isEmpty()) {
                            principal.getCodigosBarras().addAll(copia.getCodigosBarras());
                        }
                        if (principal.getProveedor() == null && copia.getProveedor() != null) {
                            principal.setProveedor(copia.getProveedor());
                        }
                        aEliminar.add(copia);
                    }
                    aGuardar.add(principal);
                }
            }

            if (!aGuardar.isEmpty()) {
                productoRepository.saveAll(aGuardar);
            }
            if (!aEliminar.isEmpty()) {
                productoRepository.deleteAll(aEliminar);
            }

            int totalDespues = totalAntes - aEliminar.size();

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("totalAntes", totalAntes);
            resp.put("totalDespues", totalDespues);
            resp.put("duplicadosEliminados", aEliminar.size());
            resp.put("productosUnicos", agrupados.size());
            resp.put("mensaje", "Catálogo saneado: Se eliminaron " + aEliminar.size() + " duplicados. Quedan " + totalDespues + " productos únicos.");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("mensaje", "Error deduplicando catálogo: " + e.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }

    // =======================================================
    // 3. LECTOR CSV INTELIGENTE (UPSERT SIN DUPLICADOS)
    // =======================================================
    @PostMapping("/api/admin/inventario/csv")
    @Transactional
    @ResponseBody
    public ResponseEntity<String> subirCsv(@RequestParam("file") MultipartFile file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            List<String> lineas = new ArrayList<>();
            String l;
            while ((l = br.readLine()) != null) {
                if (!l.trim().isEmpty()) {
                    lineas.add(l.trim());
                }
            }

            if (lineas.isEmpty()) {
                return ResponseEntity.badRequest().body("El archivo CSV está vacío.");
            }

            // Detectar delimitador (, o ;)
            char delimiter = ',';
            String primeraLinea = lineas.get(0);
            long countSemicolon = primeraLinea.chars().filter(ch -> ch == ';').count();
            long countComma = primeraLinea.chars().filter(ch -> ch == ',').count();
            if (countSemicolon > countComma) {
                delimiter = ';';
            }

            // Pre-limpiar duplicados existentes en BD y armar mapas de búsqueda
            deduplicarCatalogoInterno();

            List<Producto> todosExistentes = productoRepository.findAll();
            Map<String, Producto> porNombre = new HashMap<>();
            Map<String, Producto> porCodigo = new HashMap<>();

            for (Producto p : todosExistentes) {
                if (p.getNombre() != null) {
                    porNombre.put(normalizarTexto(p.getNombre()), p);
                }
                if (p.getCodigosBarras() != null) {
                    for (String c : p.getCodigosBarras()) {
                        if (c != null && !c.trim().isEmpty()) {
                            porCodigo.put(c.trim().toLowerCase(), p);
                        }
                    }
                }
            }

            // Detectar si la primera fila es encabezado
            List<String> tokensFila0 = parseCsvLine(lineas.get(0), delimiter);
            boolean esEncabezado = false;
            int colBarcode = -1, colNombre = 1, colCategoria = 2, colCosto = 3, colVenta = 4, colStock = 5, colDesc = 6, colImg = 7;

            for (int i = 0; i < tokensFila0.size(); i++) {
                String t = tokensFila0.get(i).toLowerCase();
                if (t.contains("nombre") || t.contains("producto") || t.contains("categor") || t.contains("precio") || t.contains("costo") || t.contains("stock")) {
                    esEncabezado = true;
                    break;
                }
            }

            int startIndex = 0;
            if (esEncabezado) {
                startIndex = 1;
                // Mapear columnas dinámicamente según títulos
                for (int i = 0; i < tokensFila0.size(); i++) {
                    String t = tokensFila0.get(i).toLowerCase();
                    if (t.contains("bar") || t.contains("cód") || t.contains("cod") || t.contains("sku")) colBarcode = i;
                    else if (t.contains("nom") || t.contains("producto") || t.contains("articulo") || t.contains("artículo")) colNombre = i;
                    else if (t.contains("cat")) colCategoria = i;
                    else if (t.contains("cost") || t.contains("compra")) colCosto = i;
                    else if (t.contains("vent") || t.contains("precio")) colVenta = i;
                    else if (t.contains("stock") || t.contains("cant")) colStock = i;
                    else if (t.contains("desc") || t.contains("deta")) colDesc = i;
                    else if (t.contains("img") || t.contains("foto") || t.contains("url") || t.contains("imagen")) colImg = i;
                }
            } else {
                // Si la primera fila no tiene encabezado, y la columna 0 no es número sino nombre
                if (tokensFila0.size() >= 1 && !tokensFila0.get(0).matches("^[0-9]+$")) {
                    colBarcode = -1;
                    colNombre = 0;
                    colCategoria = 1;
                    colCosto = 2;
                    colVenta = 3;
                    colStock = 4;
                    colDesc = 5;
                    colImg = 6;
                }
            }

            int actualizados = 0;
            int nuevos = 0;
            List<Producto> aGuardar = new ArrayList<>();

            for (int lineIdx = startIndex; lineIdx < lineas.size(); lineIdx++) {
                String linea = lineas.get(lineIdx);
                List<String> p = parseCsvLine(linea, delimiter);
                if (p.isEmpty()) continue;

                try {
                    String nombre = (colNombre >= 0 && colNombre < p.size()) ? p.get(colNombre).trim() : "";
                    if (nombre.isEmpty() || nombre.equalsIgnoreCase("nombre") || nombre.equalsIgnoreCase("producto")) {
                        continue;
                    }

                    String barcode = (colBarcode >= 0 && colBarcode < p.size()) ? p.get(colBarcode).trim() : "";
                    String categoria = (colCategoria >= 0 && colCategoria < p.size()) ? p.get(colCategoria).trim() : "General";
                    double costo = (colCosto >= 0 && colCosto < p.size()) ? parseNumero(p.get(colCosto)) : 0.0;
                    double venta = (colVenta >= 0 && colVenta < p.size()) ? parseNumero(p.get(colVenta)) : 0.0;
                    double stock = (colStock >= 0 && colStock < p.size()) ? parseNumero(p.get(colStock)) : 0.0;
                    String descripcion = (colDesc >= 0 && colDesc < p.size()) ? p.get(colDesc).trim() : "";
                    String imagen = (colImg >= 0 && colImg < p.size()) ? p.get(colImg).trim() : "";

                    // Determinar si es pesable (por categoría o mención de peso)
                    boolean esPesable = false;
                    String catLower = categoria.toLowerCase();
                    String nomLower = nombre.toLowerCase();
                    if (catLower.contains("feria") || catLower.contains("panader") || nomLower.contains("(kg)") || nomLower.contains("/kg") || nomLower.contains("granel")) {
                        esPesable = true;
                    }

                    String keyNom = normalizarTexto(nombre);

                    // Buscar si ya existe por código o por nombre
                    Producto prod = null;
                    if (!barcode.isEmpty() && porCodigo.containsKey(barcode.toLowerCase())) {
                        prod = porCodigo.get(barcode.toLowerCase());
                    }
                    if (prod == null && porNombre.containsKey(keyNom)) {
                        prod = porNombre.get(keyNom);
                    }

                    if (prod != null) {
                        // ACTUALIZAR PRODUCTO EXISTENTE (EVITA DUPLICAR)
                        prod.setNombre(nombre);
                        prod.setCategoria(categoria);
                        prod.setPrecioCompra(costo);
                        if (esPesable) {
                            prod.setEsPesable(true);
                            prod.setPrecioPorKilo(venta);
                            prod.setPrecio(0.0);
                        } else {
                            prod.setEsPesable(false);
                            prod.setPrecio(venta);
                            prod.setPrecioPorKilo(0.0);
                        }
                        prod.setStock(stock);
                        if (!descripcion.isEmpty()) prod.setDescripcion(descripcion);
                        if (!imagen.isEmpty()) prod.setImagen(imagen);

                        if (!barcode.isEmpty()) {
                            prod.getCodigosBarras().add(barcode);
                            porCodigo.put(barcode.toLowerCase(), prod);
                        }
                        actualizados++;
                    } else {
                        // CREAR NUEVO PRODUCTO
                        prod = new Producto();
                        prod.setNombre(nombre);
                        prod.setCategoria(categoria);
                        prod.setPrecioCompra(costo);
                        if (esPesable) {
                            prod.setEsPesable(true);
                            prod.setPrecioPorKilo(venta);
                            prod.setPrecio(0.0);
                        } else {
                            prod.setEsPesable(false);
                            prod.setPrecio(venta);
                            prod.setPrecioPorKilo(0.0);
                        }
                        prod.setStock(stock);
                        prod.setDescripcion(descripcion);
                        if (!imagen.isEmpty()) prod.setImagen(imagen);

                        if (!barcode.isEmpty()) {
                            prod.getCodigosBarras().add(barcode);
                            porCodigo.put(barcode.toLowerCase(), prod);
                        }

                        porNombre.put(keyNom, prod);
                        nuevos++;
                    }

                    aGuardar.add(prod);

                } catch (Exception rowEx) {
                    System.err.println("Error procesando fila CSV: " + linea + " -> " + rowEx.getMessage());
                }
            }

            if (!aGuardar.isEmpty()) {
                productoRepository.saveAll(aGuardar);
            }

            long totalFinal = productoRepository.count();
            return ResponseEntity.ok("Procesado exitoso sin duplicados: " + actualizados + " productos actualizados, " + nuevos + " productos nuevos. Catálogo total: " + totalFinal + " productos.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error estructural leyendo CSV: " + e.getMessage());
        }
    }

    private void deduplicarCatalogoInterno() {
        try {
            List<Producto> todos = productoRepository.findAll();
            Map<String, List<Producto>> agrupados = new LinkedHashMap<>();
            for (Producto p : todos) {
                String key = normalizarTexto(p.getNombre());
                if (!key.isEmpty()) {
                    agrupados.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
                }
            }
            List<Producto> aEliminar = new ArrayList<>();
            for (List<Producto> copias : agrupados.values()) {
                if (copias.size() > 1) {
                    copias.sort(Comparator.comparing(Producto::getId));
                    for (int i = 1; i < copias.size(); i++) {
                        aEliminar.add(copias.get(i));
                    }
                }
            }
            if (!aEliminar.isEmpty()) {
                productoRepository.deleteAll(aEliminar);
            }
        } catch (Exception e) {
            System.err.println("Error en deduplicación interna: " + e.getMessage());
        }
    }

    private String normalizarTexto(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private double parseNumero(String val) {
        if (val == null || val.trim().isEmpty()) return 0.0;
        String clean = val.trim().replace("$", "").replace(" ", "");
        if (clean.contains(",") && !clean.contains(".")) {
            clean = clean.replace(",", ".");
        } else {
            clean = clean.replace(",", "");
        }
        clean = clean.replaceAll("[^0-9.]", "");
        try {
            return clean.isEmpty() ? 0.0 : Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private List<String> parseCsvLine(String line, char delimiter) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == delimiter && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }
}