package com.gomezsystems.minierp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cortes_z")
public class CorteZ {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaCorte;
    private Double totalEfectivo;
    private Double totalFiado;
    private Double totalTarjeta;

    @Column(length = 5000)
    private String detalleInforme; // Opcional, guarda un log HTML o JSON del resumen visual.

    public CorteZ() {
        this.fechaCorte = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechaCorte() { return fechaCorte; }
    public void setFechaCorte(LocalDateTime fechaCorte) { this.fechaCorte = fechaCorte; }

    public Double getTotalEfectivo() { return totalEfectivo; }
    public void setTotalEfectivo(Double totalEfectivo) { this.totalEfectivo = totalEfectivo; }

    public Double getTotalFiado() { return totalFiado; }
    public void setTotalFiado(Double totalFiado) { this.totalFiado = totalFiado; }

    public Double getTotalTarjeta() { return totalTarjeta; }
    public void setTotalTarjeta(Double totalTarjeta) { this.totalTarjeta = totalTarjeta; }

    public String getDetalleInforme() { return detalleInforme; }
    public void setDetalleInforme(String detalleInforme) { this.detalleInforme = detalleInforme; }
}
