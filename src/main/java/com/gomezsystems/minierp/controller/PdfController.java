package com.gomezsystems.minierp.controller;

import com.gomezsystems.minierp.model.Cliente;
import com.gomezsystems.minierp.model.CorteZ;
import com.gomezsystems.minierp.model.Gasto;
import com.gomezsystems.minierp.model.Venta;
import com.gomezsystems.minierp.repository.ClienteRepository;
import com.gomezsystems.minierp.repository.CorteZRepository;
import com.gomezsystems.minierp.repository.GastoRepository;
import com.gomezsystems.minierp.repository.VentaRepository;
import com.gomezsystems.minierp.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    @Autowired
    private PdfService pdfService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private GastoRepository gastoRepository;

    @Autowired
    private CorteZRepository corteZRepository;

    @GetMapping("/cliente/{id}")
    public ResponseEntity<byte[]> getPdfHistorialCliente(@PathVariable Long id) {
        Cliente c = clienteRepository.findById(id).orElse(null);
        if (c == null) return ResponseEntity.notFound().build();

        List<Venta> historial = ventaRepository.findAll().stream()
                .filter(v -> v.getCliente() != null && v.getCliente().getId().equals(id))
                .sorted((v1, v2) -> v2.getFechaHora().compareTo(v1.getFechaHora()))
                .collect(Collectors.toList());

        byte[] pdf = pdfService.generarPdfHistorialVip(c, historial);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "historial_cliente_" + id + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/tesoreria")
    public ResponseEntity<byte[]> getPdfTesoreria() {
        LocalDate hoy = LocalDate.now();

        List<Venta> ventasHoy = ventaRepository.findAll().stream()
                .filter(v -> v.getFechaHora() != null && v.getFechaHora().toLocalDate().equals(hoy))
                .collect(Collectors.toList());

        Double ventasTotales = ventasHoy.stream().mapToDouble(v -> v.getMontoTotal() != null ? v.getMontoTotal() : 0.0).sum();

        List<Gasto> gastosHoy = gastoRepository.findAll().stream()
                .filter(g -> g.getFechaHora() != null && g.getFechaHora().toLocalDate().equals(hoy))
                .collect(Collectors.toList());

        Double gastosPagados = gastosHoy.stream()
                .filter(g -> "PAGADO".equals(g.getEstado()))
                .mapToDouble(g -> g.getMonto() != null ? g.getMonto() : 0.0)
                .sum();

        Map<String, Double> metrics = new HashMap<>();
        metrics.put("ventas", ventasTotales);
        metrics.put("gastos", gastosPagados);
        metrics.put("flujoLibre", ventasTotales - gastosPagados);

        byte[] pdf = pdfService.generarPdfLibroContable(ventasHoy, gastosHoy, metrics);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "reporte_tesoreria_" + hoy.toString() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/cortez/{id}")
    public ResponseEntity<byte[]> getPdfCorteZ(@PathVariable Long id) {
        CorteZ corte = corteZRepository.findById(id).orElse(null);
        if (corte == null) return ResponseEntity.notFound().build();

        byte[] pdf = pdfService.generarPdfCorteZ(corte);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "corte_z_" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}
