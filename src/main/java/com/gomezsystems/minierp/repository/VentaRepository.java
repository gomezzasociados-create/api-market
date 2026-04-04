package com.gomezsystems.minierp.repository;

import com.gomezsystems.minierp.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByEstado(String estado);
    List<Venta> findByCierreAplicadoFalseOrderByFechaHoraDesc();
}
