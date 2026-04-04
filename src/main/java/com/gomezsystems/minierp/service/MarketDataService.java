package com.gomezsystems.minierp.service;

import com.gomezsystems.minierp.model.Producto;
import com.gomezsystems.minierp.repository.ProductoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MarketDataService {

    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private com.gomezsystems.minierp.repository.ClienteRepository clienteRepository;

    @PostConstruct
    public void inicializarGomezMarket() {
        if (productoRepository.count() > 0) {
            System.out.println("✅ Base de datos GOMEZ MARKET ya inicializada. Omitiendo inyección mock.");
            return;
        }
        
        System.out.println("🧹 Limpiando base de datos para GOMEZ MARKET...");
        productoRepository.deleteAll();

        // Inyectar 2 clientes iniciales si la DB de CRM está vacía para pruebas
        if(clienteRepository.count() == 0) {
            com.gomezsystems.minierp.model.Cliente c1 = new com.gomezsystems.minierp.model.Cliente();
            c1.setNombre("Ejemplo: Juan Pérez (Regular)");
            c1.setTelefono("+56912345678");
            c1.setDireccion("Avenida Principal 123");
            c1.setEtiqueta("REGULAR");
            clienteRepository.save(c1);

            com.gomezsystems.minierp.model.Cliente c2 = new com.gomezsystems.minierp.model.Cliente();
            c2.setNombre("Ejemplo: Importadora ACME (VIP)");
            c2.setTelefono("+56998765432");
            c2.setDireccion("Bodega Central 45");
            c2.setEtiqueta("VIP");
            c2.setDeudaActiva(50000.0);
            c2.setFechaLimitePago(java.time.LocalDate.now().plusDays(15));
            clienteRepository.save(c2);
            System.out.println("👥 Clientes de prueba CRM integrados.");
        }

        List<Producto> p = new ArrayList<>();

        // Nombres Base
        String[] nombresAbarrotes = {"Arroz", "Fideos", "Aceite", "Lentejas", "Porotos", "Azúcar", "Sal", "Salsa Tomate", "Atún", "Jurel"};
        String[] adjsAbarrotes = {"Grado 1", "Grado 2", "1Kg", "Extra Fino", "Premium", "En lata"};

        String[] nombresPanaderia = {"Pan Marraqueta", "Pan Hallulla", "Pan Frica", "Pan Molde", "Tortilla", "Empanada", "Kuchen", "Torta", "Donas", "Medialunas"};
        String[] nombresBebestibles = {"Coca Cola", "Sprite", "Fanta", "Pepsi", "Jugo Néctar", "Agua Mineral", "Cerveza Lager", "Cerveza Ale", "Pisco", "Vino Tinto"};
        String[] nombresFeria = {"Tomate", "Palta", "Cebolla", "Limón", "Naranja", "Manzana", "Plátano", "Pera", "Durazno", "Uva", "Lechuga", "Ajo", "Zanahoria"};

        // Lógica Fotográfica Estricta (HashMap)
        java.util.Map<String, String> fotos = new java.util.HashMap<>();
        
        // Abarrotes
        fotos.put("Arroz", "https://images.unsplash.com/photo-1586201375761-83865001e31c?q=80&w=400");
        fotos.put("Fideos", "https://images.unsplash.com/photo-1612889815779-1e37bcdee5df?q=80&w=400");
        fotos.put("Aceite", "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?q=80&w=400");
        fotos.put("Lentejas", "https://images.unsplash.com/photo-1515543904379-3d757afe72e4?q=80&w=400");
        fotos.put("Porotos", "https://images.unsplash.com/photo-1515543904379-3d757afe72e4?q=80&w=400");
        fotos.put("Azúcar", "https://images.unsplash.com/photo-1581441363689-1f3c3c414655?q=80&w=400");
        fotos.put("Sal", "https://images.unsplash.com/photo-1581441363689-1f3c3c414655?q=80&w=400");
        fotos.put("Salsa Tomate", "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?q=80&w=400");
        fotos.put("Atún", "https://images.unsplash.com/photo-1604719312566-8fa2d6501ab6?q=80&w=400");
        fotos.put("Jurel", "https://images.unsplash.com/photo-1604719312566-8fa2d6501ab6?q=80&w=400");
        
        // Panadería
        fotos.put("Pan Marraqueta", "https://images.unsplash.com/photo-1509440159596-0249088772ff?q=80&w=400");
        fotos.put("Pan Hallulla", "https://images.unsplash.com/photo-1509440159596-0249088772ff?q=80&w=400");
        fotos.put("Pan Frica", "https://images.unsplash.com/photo-1509440159596-0249088772ff?q=80&w=400");
        fotos.put("Pan Molde", "https://images.unsplash.com/photo-1598373182133-52452f7691ef?q=80&w=400");
        fotos.put("Tortilla", "https://images.unsplash.com/photo-1512852939750-1305098529bf?q=80&w=400");
        fotos.put("Empanada", "https://images.unsplash.com/photo-1512852939750-1305098529bf?q=80&w=400");
        fotos.put("Kuchen", "https://images.unsplash.com/photo-1535141192574-5d4897c12636?q=80&w=400");
        fotos.put("Torta", "https://images.unsplash.com/photo-1535141192574-5d4897c12636?q=80&w=400");
        fotos.put("Donas", "https://images.unsplash.com/photo-1551106651-a5bccaacc856?q=80&w=400");
        fotos.put("Medialunas", "https://images.unsplash.com/photo-1551106651-a5bccaacc856?q=80&w=400");
        
        // Bebestibles
        fotos.put("Coca Cola", "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?q=80&w=400");
        fotos.put("Sprite", "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?q=80&w=400");
        fotos.put("Fanta", "https://images.unsplash.com/photo-1527004013197-933c4bb611b3?q=80&w=400");
        fotos.put("Pepsi", "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?q=80&w=400");
        fotos.put("Jugo Néctar", "https://images.unsplash.com/photo-1613478223719-2ab218ee8755?q=80&w=400");
        fotos.put("Agua Mineral", "https://images.unsplash.com/photo-1548839140-29a749e1bc4e?q=80&w=400");
        fotos.put("Cerveza Lager", "https://images.unsplash.com/photo-1605336648712-870020d2dca7?q=80&w=400");
        fotos.put("Cerveza Ale", "https://images.unsplash.com/photo-1605336648712-870020d2dca7?q=80&w=400");
        fotos.put("Pisco", "https://images.unsplash.com/photo-1506377247377-2a5b3b417ebb?q=80&w=400");
        fotos.put("Vino Tinto", "https://images.unsplash.com/photo-1506377247377-2a5b3b417ebb?q=80&w=400");
        
        // Feria
        fotos.put("Tomate", "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?q=80&w=400");
        fotos.put("Palta", "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?q=80&w=400");
        fotos.put("Cebolla", "https://images.unsplash.com/photo-1618512496248-a07ce83aa8cb?q=80&w=400");
        fotos.put("Limón", "https://images.unsplash.com/photo-1590505681916-2e862176a9f0?q=80&w=400");
        fotos.put("Naranja", "https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?q=80&w=400");
        fotos.put("Manzana", "https://images.unsplash.com/photo-1560806887-1e4cd0b6caa6?q=80&w=400");
        fotos.put("Plátano", "https://images.unsplash.com/photo-1571501679680-de32f1e7aad4?q=80&w=400");
        fotos.put("Pera", "https://images.unsplash.com/photo-1615485925600-97237c4fc1ec?q=80&w=400");
        fotos.put("Durazno", "https://images.unsplash.com/photo-1528821128474-27f963b062bf?q=80&w=400");
        fotos.put("Uva", "https://images.unsplash.com/photo-1596363505729-411781ebcd45?q=80&w=400");
        fotos.put("Lechuga", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=400");
        fotos.put("Ajo", "https://images.unsplash.com/photo-1540148426945-044730537ba3?q=80&w=400");
        fotos.put("Zanahoria", "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?q=80&w=400");

        String fotoPorDefecto = "https://images.unsplash.com/photo-1542838132-92c53300491e?q=80&w=400";

        // Generar exactamente 200 productos lógicos
        for(int i = 0; i < 200; i++) {
            int tipo = i % 4; // 0=Feria, 1=Abarrotes, 2=Panadería, 3=Bebestibles
            
            if (tipo == 0) { 
                String nomBase = nombresFeria[i % nombresFeria.length];
                String nombreCompleto = nomBase + " " + (i%3==0 ? "Extra" : "Primera");
                double precio = 800 + (Math.random() * 3000);
                double pOficial = Math.round(precio/10)*10;
                double costo = pOficial * (0.5 + Math.random() * 0.2); // 50% a 70% del precio (ganancia 30%-50%)
                p.add(crearP(nombreCompleto, 0, "Feria", fotos.getOrDefault(nomBase, fotoPorDefecto), "Cosecha fresca, código: " + (1000 + i), true, pOficial, Math.round(costo/10)*10));
            
            } else if (tipo == 1) { 
                String nomBase = nombresAbarrotes[i % nombresAbarrotes.length];
                String nombreCompleto = nomBase + " " + adjsAbarrotes[i % adjsAbarrotes.length];
                double precio = 900 + (Math.random() * 4000);
                double pOficial = Math.round(precio/10)*10;
                double costo = pOficial * (0.5 + Math.random() * 0.2);
                p.add(crearP(nombreCompleto, pOficial, "Abarrotes", fotos.getOrDefault(nomBase, fotoPorDefecto), "Despensa código: " + (1000 + i), false, 0, Math.round(costo/10)*10));
            
            } else if (tipo == 2) { 
                String nomBase = nombresPanaderia[i % nombresPanaderia.length];
                String nombreCompleto = nomBase + " " + (i%2==0 ? "Horneado" : "Clásico");
                double precio = 1200 + (Math.random() * 2000);
                double pOficial = Math.round(precio/10)*10;
                double costo = pOficial * (0.5 + Math.random() * 0.2);
                p.add(crearP(nombreCompleto, pOficial, "Panadería", fotos.getOrDefault(nomBase, fotoPorDefecto), "Panadería código: " + (1000 + i), false, 0, Math.round(costo/10)*10));
            
            } else if (tipo == 3) { 
                String nomBase = nombresBebestibles[i % nombresBebestibles.length];
                String nombreCompleto = nomBase + " " + ((i%2==0)?"1L":"2L");
                double precio = 1500 + (Math.random() * 5000);
                double pOficial = Math.round(precio/10)*10;
                double costo = pOficial * (0.5 + Math.random() * 0.2);
                p.add(crearP(nombreCompleto, pOficial, "Bebestibles", fotos.getOrDefault(nomBase, fotoPorDefecto), "Bebidas código: " + (1000 + i), false, 0, Math.round(costo/10)*10));
            }
        }

        // Guardar en la base de datos
        productoRepository.saveAll(p);
        System.out.println("🛒 GOMEZ MARKET: Catálogo fotográfico vinculado y generado con 200 productos.");
    }

    // Método ayudante
    private Producto crearP(String nom, double pre, String cat, String img, String desc, boolean pesable, double precioKilo, double precioCompra) {
        Producto prod = new Producto();
        prod.setNombre(nom);
        prod.setPrecio(pre);
        prod.setCategoria(cat);
        prod.setImagen(img);
        prod.setDescripcion(desc);
        prod.setEsPesable(pesable);
        prod.setPrecioPorKilo(precioKilo);
        prod.setPrecioCompra(precioCompra);
        prod.setStock(100.0);
        return prod;
    }
}