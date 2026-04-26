package com.gomezsystems.minierp.service;

import com.gomezsystems.minierp.model.Cliente;
import com.gomezsystems.minierp.model.CorteZ;
import com.gomezsystems.minierp.model.Gasto;
import com.gomezsystems.minierp.model.Venta;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.awt.Color;

@Service
public class PdfService {

    // 80mm width in points (~226)
    private static final Rectangle TICKET_80MM = new Rectangle(226, 842); // Standard A4 height, but narrow width
    
    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    private static final Font FONT_SMALL = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.DARK_GRAY);

    private String formatMoney(Double amount) {
        if (amount == null) return "$0";
        return "$" + String.format("%,.0f", amount).replace(",", ".");
    }

    private void agregarLineaBlanco(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
    }
    
    private void addDivider(Document document) throws DocumentException {
        Paragraph p = new Paragraph("----------------------------------", FONT_NORMAL);
        p.setAlignment(Element.ALIGN_CENTER);
        document.add(p);
    }

    // --- 1. HISTORIAL VIP ---
    public byte[] generarPdfHistorialVip(Cliente cliente, List<Venta> historial) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Document document = new Document(TICKET_80MM, 10, 10, 10, 10);
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("ESTADO DE CUENTA VIP", FONT_TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            Paragraph sub = new Paragraph("MARKET GOMEZ SYSTEMS", FONT_BOLD);
            sub.setAlignment(Element.ALIGN_CENTER);
            document.add(sub);
            
            addDivider(document);

            document.add(new Paragraph("Cliente: " + cliente.getNombre(), FONT_BOLD));
            document.add(new Paragraph("Rut: " + (cliente.getDni() != null ? cliente.getDni() : "S/N"), FONT_NORMAL));
            document.add(new Paragraph("Tel: " + (cliente.getTelefono() != null ? cliente.getTelefono() : "S/N"), FONT_NORMAL));
            document.add(new Paragraph("Emitido: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), FONT_SMALL));
            
            addDivider(document);
            
            document.add(new Paragraph("DEUDA ACTUAL: " + formatMoney(cliente.getDeudaActiva()), FONT_TITLE));
            if (cliente.getFechaLimitePago() != null) {
                document.add(new Paragraph("Límite de Pago: " + cliente.getFechaLimitePago().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), FONT_BOLD));
            }
            
            addDivider(document);
            document.add(new Paragraph("MOVIMIENTOS:", FONT_BOLD));
            
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 1f});
            
            if (historial.isEmpty()) {
                document.add(new Paragraph("Sin registros.", FONT_NORMAL));
            } else {
                for (Venta v : historial) {
                    boolean esAbono = v.getTipoPago() != null && v.getTipoPago().startsWith("ABONO");
                    String fecha = v.getFechaHora() != null ? v.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM yy")) : "";
                    
                    String det = esAbono ? "[PAGO] " + v.getDetalle() : "[COMPRA] " + fecha;
                    PdfPCell cellDetalle = new PdfPCell(new Phrase(det, FONT_SMALL));
                    cellDetalle.setBorder(Rectangle.NO_BORDER);
                    
                    PdfPCell cellMonto = new PdfPCell(new Phrase(formatMoney(v.getMontoTotal()), FONT_BOLD));
                    cellMonto.setBorder(Rectangle.NO_BORDER);
                    cellMonto.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    
                    table.addCell(cellDetalle);
                    table.addCell(cellMonto);
                }
                document.add(table);
            }
            
            addDivider(document);
            Paragraph footer = new Paragraph("Reporte generado por Inteligencia Artificial", FONT_SMALL);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    // --- 2. LIBRO CONTABLE / TESORERÍA ---
    public byte[] generarPdfLibroContable(List<Venta> ventas, List<Gasto> gastos, Map<String, Double> metrics) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Document document = new Document(TICKET_80MM, 10, 10, 10, 10);
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("REPORTE TESORERIA", FONT_TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            Paragraph sub = new Paragraph("MARKET GOMEZ SYSTEMS", FONT_BOLD);
            sub.setAlignment(Element.ALIGN_CENTER);
            document.add(sub);
            
            addDivider(document);
            
            document.add(new Paragraph("Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), FONT_NORMAL));
            
            addDivider(document);
            document.add(new Paragraph("INGRESOS HOY: " + formatMoney(metrics.get("ventas")), FONT_BOLD));
            document.add(new Paragraph("GASTOS HOY: -" + formatMoney(metrics.get("gastos")), FONT_BOLD));
            document.add(new Paragraph("FLUJO LIBRE: " + formatMoney(metrics.get("flujoLibre")), FONT_TITLE));
            
            addDivider(document);
            document.add(new Paragraph("GASTOS DETALLADOS:", FONT_BOLD));
            
            if (gastos.isEmpty()) {
                document.add(new Paragraph("Sin egresos hoy.", FONT_NORMAL));
            } else {
                for (Gasto g : gastos) {
                    document.add(new Paragraph(g.getDescripcion() + " (" + g.getEstado() + ")", FONT_SMALL));
                    Paragraph pM = new Paragraph(formatMoney(g.getMonto()), FONT_BOLD);
                    pM.setAlignment(Element.ALIGN_RIGHT);
                    document.add(pM);
                }
            }
            
            addDivider(document);
            Paragraph footer = new Paragraph("Reporte generado por Inteligencia Artificial", FONT_SMALL);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    // --- 3. CORTE Z ---
    public byte[] generarPdfCorteZ(CorteZ corte) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Document document = new Document(TICKET_80MM, 10, 10, 10, 10);
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("CIERRE DE CAJA", FONT_TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            Paragraph sub = new Paragraph("MARKET GOMEZ SYSTEMS", FONT_BOLD);
            sub.setAlignment(Element.ALIGN_CENTER);
            document.add(sub);
            
            addDivider(document);
            
            document.add(new Paragraph("Ticket ID: #" + corte.getId(), FONT_NORMAL));
            if (corte.getFechaCorte() != null) {
                document.add(new Paragraph("Fecha Cierre: " + corte.getFechaCorte().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")), FONT_SMALL));
            }
            
            addDivider(document);
            Double totalCalc = (corte.getTotalEfectivo() != null ? corte.getTotalEfectivo() : 0.0) +
                               (corte.getTotalTarjeta() != null ? corte.getTotalTarjeta() : 0.0) +
                               (corte.getTotalFiado() != null ? corte.getTotalFiado() : 0.0);
                               
            document.add(new Paragraph("TOTAL TRANSACCIONES: " + formatMoney(totalCalc), FONT_TITLE));
            document.add(new Paragraph("Efectivo: " + formatMoney(corte.getTotalEfectivo()), FONT_NORMAL));
            document.add(new Paragraph("Tarjetas: " + formatMoney(corte.getTotalTarjeta()), FONT_NORMAL));
            document.add(new Paragraph("Fiados (Crédito): " + formatMoney(corte.getTotalFiado()), FONT_NORMAL));
            
            addDivider(document);
            Paragraph footer = new Paragraph("Reporte validado y cerrado.\nNo admite modificaciones.", FONT_SMALL);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
}
