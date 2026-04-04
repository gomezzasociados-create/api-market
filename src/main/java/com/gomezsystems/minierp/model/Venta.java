package com.gomezsystems.minierp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double montoTotal;
    
    private String tipoPago; // "EFECTIVO", "TARJETA", "FIADO"
    
    private String estado;   // "PAGADO", "PENDIENTE"

    private LocalDateTime fechaHora;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente; // Cliente asociado (si es fiado)

    @Column(length = 1000)
    private String detalle; // Detalle del ticket

    @Column(columnDefinition = "boolean default false")
    private boolean cierreAplicado = false; // Indica si ya se "cortó" caja con esta venta

    public Venta() {
        this.fechaHora = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(Double montoTotal) { this.montoTotal = montoTotal; }

    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public boolean isCierreAplicado() { return cierreAplicado; }
    public void setCierreAplicado(boolean cierreAplicado) { this.cierreAplicado = cierreAplicado; }
}
