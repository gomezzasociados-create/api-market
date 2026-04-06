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

@Controller
public class AdminController {

    // Conectamos el controlador a la base de datos de productos
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
            // Elimina todos los productos de la tabla
            productoRepository.deleteAll();
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en purga: " + e.getMessage());
        }
    }

    // =======================================================
    // 2. LECTOR CSV MEJORADO (LEE FOTOS Y PESABLES AUTOMÁTICAMENTE)
    // =======================================================
    @PostMapping("/api/admin/inventario/csv")
    @ResponseBody
    public ResponseEntity<String> subirCsv(@RequestParam("file") MultipartFile file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 7) { // Mínimo 7 columnas requeridas
                    try {
                    // Ignorar fila de títulos
                    if (p[1].toLowerCase().contains("nombre") || p[2].toLowerCase().contains("categor") || p[3].toLowerCase().contains("precio")) {
                        continue;
                    }

                    Producto prod = new Producto();
                    prod.setNombre(p[1].trim());
                    prod.setCategoria(p[2].trim());
                    
                    String costoStr = p[3].replaceAll("[^0-9.]", "");
                    prod.setPrecioCompra(costoStr.isEmpty() ? 0.0 : Double.parseDouble(costoStr));

                    String ventaStr = p[4].replaceAll("[^0-9.]", "");
                    Double valVenta = ventaStr.isEmpty() ? 0.0 : Double.parseDouble(ventaStr);

                    // Lógica inteligente: Si es Feria o Panadería, asume que se vende por Kilo
                    if(p[2].trim().equalsIgnoreCase("Feria") || p[2].trim().equalsIgnoreCase("Panadería")) {
                        prod.setEsPesable(true);
                        prod.setPrecioPorKilo(valVenta);
                        prod.setPrecio(0.0);
                    } else {
                        prod.setEsPesable(false);
                        prod.setPrecio(valVenta);
                        prod.setPrecioPorKilo(0.0);
                    }

                    String stockStr = p[5].replaceAll("[^0-9.-]", "");
                    prod.setStock(stockStr.isEmpty() ? 0.0 : Double.parseDouble(stockStr));
                    
                    prod.setDescripcion(p[6].trim());

                    // Magia: Si el CSV trae una 8va columna, la guarda como URL de la foto
                    if(p.length >= 8) {
                        prod.setImagen(p[7].trim());
                    }

                    productoRepository.save(prod);
                    } catch (Exception rowEx) {
                        // Si esta fila falla por un precio inválido o constraint, pasa a la siguiente silenciosamente.
                        System.err.println("Error procesando fila CSV: " + line + " -> " + rowEx.getMessage());
                    }
                }
            }
            return ResponseEntity.ok("Catálogo procesado. Las filas válidas se cargaron exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error estructural leyendo CSV: " + e.getMessage());
        }
    }
}