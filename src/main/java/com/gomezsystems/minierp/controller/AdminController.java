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
    @Transactional
    @ResponseBody
    public ResponseEntity<String> subirCsv(@RequestParam("file") MultipartFile file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 7) { // Mínimo 7 columnas requeridas
                    Producto prod = new Producto();
                    prod.setNombre(p[1].trim());
                    prod.setCategoria(p[2].trim());
                    prod.setPrecioCompra(Double.parseDouble(p[3].trim()));

                    // Lógica inteligente: Si es Feria o Panadería, asume que se vende por Kilo
                    if(p[2].trim().equalsIgnoreCase("Feria") || p[2].trim().equalsIgnoreCase("Panadería")) {
                        prod.setEsPesable(true);
                        prod.setPrecioPorKilo(Double.parseDouble(p[4].trim()));
                        prod.setPrecio(0.0);
                    } else {
                        prod.setEsPesable(false);
                        prod.setPrecio(Double.parseDouble(p[4].trim()));
                        prod.setPrecioPorKilo(0.0);
                    }

                    prod.setStock(Double.parseDouble(p[5].trim()));
                    prod.setDescripcion(p[6].trim());

                    // Magia: Si el CSV trae una 8va columna, la guarda como URL de la foto
                    if(p.length >= 8) {
                        prod.setImagen(p[7].trim());
                    }

                    productoRepository.save(prod);
                }
            }
            return ResponseEntity.ok("Catálogo CSV inyectado exitosamente con imágenes.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error leyendo CSV: " + e.getMessage());
        }
    }
}