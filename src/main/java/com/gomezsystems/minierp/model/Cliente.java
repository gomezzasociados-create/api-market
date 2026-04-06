package com.gomezsystems.minierp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String telefono;
    private String email;
    private String dni;
    private String direccion;
    
    // Para control CRM o créditos en el Market
    private String etiqueta; // Ej: "NUEVO", "CREDITO", "DEUDOR"
    private LocalDate fechaRegistro;
    
    private Double deudaActiva;
    
    // Para Cobranza Asistida
    private LocalDate fechaLimitePago;
    
    // Cupo dinámico del VIP
    private Double cupoMaximo;
    
    // Plantillas WhatsApp Personalizadas para el cliente
    @Column(columnDefinition = "TEXT")
    private String plantillaMsgCompra;
    
    @Column(columnDefinition = "TEXT")
    private String plantillaMsgAbono;

    @Column(columnDefinition = "TEXT")
    private String plantillaMsgOferta;

    public Cliente() {
        this.fechaRegistro = LocalDate.now();
        this.deudaActiva = 0.0;
        this.cupoMaximo = 100000.0;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Double getDeudaActiva() { return deudaActiva; }
    public void setDeudaActiva(Double deudaActiva) { this.deudaActiva = deudaActiva; }

    public LocalDate getFechaLimitePago() { return fechaLimitePago; }
    public void setFechaLimitePago(LocalDate fechaLimitePago) { this.fechaLimitePago = fechaLimitePago; }

    public Double getCupoMaximo() { return cupoMaximo; }
    public void setCupoMaximo(Double cupoMaximo) { this.cupoMaximo = cupoMaximo; }

    public String getPlantillaMsgCompra() { return plantillaMsgCompra; }
    public void setPlantillaMsgCompra(String plantillaMsgCompra) { this.plantillaMsgCompra = plantillaMsgCompra; }

    public String getPlantillaMsgAbono() { return plantillaMsgAbono; }
    public void setPlantillaMsgAbono(String plantillaMsgAbono) { this.plantillaMsgAbono = plantillaMsgAbono; }

    public String getPlantillaMsgOferta() { return plantillaMsgOferta; }
    public void setPlantillaMsgOferta(String plantillaMsgOferta) { this.plantillaMsgOferta = plantillaMsgOferta; }
}
