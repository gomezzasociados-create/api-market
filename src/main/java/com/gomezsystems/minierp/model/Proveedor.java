package com.gomezsystems.minierp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String empresa;
    private String vendedorContacto;
    private String telefono;
    
    private String direccion;
    private String rubro; // Producto o Mercancía que provee
    
    // Cuánto le debemos al proveedor actualmente
    private Double deudaActiva;

    private LocalDate fechaRegistro;

    public Proveedor() {
        this.fechaRegistro = LocalDate.now();
        this.deudaActiva = 0.0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getVendedorContacto() { return vendedorContacto; }
    public void setVendedorContacto(String vendedorContacto) { this.vendedorContacto = vendedorContacto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Double getDeudaActiva() { return deudaActiva; }
    public void setDeudaActiva(Double deudaActiva) { this.deudaActiva = deudaActiva; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getRubro() { return rubro; }
    public void setRubro(String rubro) { this.rubro = rubro; }
}
