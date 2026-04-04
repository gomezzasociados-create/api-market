package com.gomezsystems.minierp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gastos")
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categoria; // "MERCADERIA", "ARRIENDO", "SERVICIOS"
    private String descripcion; 
    private Double monto;
    
    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor; // Puede ser nulo si no es compra guiada a proveedor
    
    private String estado; // "PAGADO", "DEUDA"

    private LocalDateTime fechaHora;

    public Gasto() {
        this.fechaHora = LocalDateTime.now();
        this.estado = "PAGADO"; // Default
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}
