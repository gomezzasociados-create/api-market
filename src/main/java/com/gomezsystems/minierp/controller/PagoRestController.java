package com.gomezsystems.minierp.controller;

import com.gomezsystems.minierp.model.Cita;
import com.gomezsystems.minierp.model.Cliente;
import com.gomezsystems.minierp.repository.CitaRepository;
import com.gomezsystems.minierp.repository.ClienteRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.exceptions.MPApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/pagos")
public class PagoRestController {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @PostMapping("/crear-preferencia")
    public String crearPreferencia(@RequestBody Map<String, Object> datos) {
        try {
            // 1. CONFIGURACIÓN TOKEN (Tu token actual)
            MercadoPagoConfig.setAccessToken("APP_USR-5751871946510432-080417-64053e5e178846a2dff9e962057f1032-735099817");

            // 2. EXTRACCIÓN DE DATOS
            String titulo = (String) datos.get("titulo");
            String precioStr = String.valueOf(datos.get("precio"));
            BigDecimal precio = new BigDecimal(precioStr);
            String fechaAgendamientoStr = (String) datos.get("fechaAgendamiento");

            // Datos del cliente
            String nombreCli = (String) datos.get("nombre");
            String telefonoCli = (String) datos.get("telefono");
            String emailCli = (String) datos.get("email");
            String metodoPago = (String) datos.get("metodoPago");

            // 3. REGISTRO DE CLIENTA EN CRM
            Cliente clienteVenta = new Cliente();
            clienteVenta.setNombre(nombreCli);
            clienteVenta.setTelefono(telefonoCli);
            if (emailCli != null && !emailCli.isEmpty()) {
                clienteVenta.setCorreo(emailCli);
            }
            clienteVenta = clienteRepository.save(clienteVenta);

            // 4. CREACIÓN DE LA CITA
            Cita nuevaCita = new Cita();
            nuevaCita.setCliente(clienteVenta);
            nuevaCita.setNombreTratamiento(titulo);
            nuevaCita.setTotalPagado(precio.intValue());
            nuevaCita.setEstado("RESERVADO"); // Queda pendiente de pago/confirmación
            nuevaCita.setFechaCreacion(LocalDateTime.now());

            if (fechaAgendamientoStr != null && !fechaAgendamientoStr.isEmpty()) {
                String fechaLimpia = fechaAgendamientoStr.substring(0, 19);
                nuevaCita.setFechaHora(LocalDateTime.parse(fechaLimpia));
            }

            Cita citaGuardada = citaRepository.save(nuevaCita);

            // 🔥 5. LÓGICA DE RUTEO DE PAGO 🔥
            if ("transferencia".equals(metodoPago)) {
                // Si es transferencia, devolvemos una bandera al frontend para que muestre el modal del banco
                return "TRANSFERENCIA_OK|" + citaGuardada.getId();
            }

            // 6. SI ES MERCADO PAGO, CREAMOS LA PREFERENCIA
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(titulo)
                    .quantity(1)
                    .unitPrice(precio)
                    .currencyId("CLP")
                    .build();

            // Pasarle el email a MP hace que se salte la pantalla de inicio de sesión inicial
            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .email(emailCli != null && !emailCli.isEmpty() ? emailCli : "cliente@lindamama.cl")
                    .name(nombreCli)
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("http://localhost:8080/api/pagos/exito")
                    .pending("http://localhost:8080/catalogo")
                    .failure("http://localhost:8080/agenda")
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(Collections.singletonList(itemRequest))
                    .payer(payer)
                    .backUrls(backUrls)
                    .externalReference(String.valueOf(citaGuardada.getId()))
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            return preference.getInitPoint();

        } catch (MPApiException apiException) {
            System.err.println("🚨 Error MP: " + apiException.getApiResponse().getContent());
            return "Error MP: " + apiException.getApiResponse().getContent();
        } catch (Exception e) {
            System.err.println("Error interno: " + e.getMessage());
            return "Error al crear pago: " + e.getMessage();
        }
    }

    @GetMapping("/exito")
    public RedirectView pagoExitoso(
            @RequestParam(value = "collection_id", required = false) String collectionId,
            @RequestParam(value = "collection_status", required = false) String collectionStatus,
            @RequestParam(value = "external_reference", required = false) String externalReference) {

        if ("approved".equals(collectionStatus) && externalReference != null) {
            try {
                Long idCita = Long.parseLong(externalReference);
                Optional<Cita> citaOpt = citaRepository.findById(idCita);

                if (citaOpt.isPresent()) {
                    Cita cita = citaOpt.get();
                    cita.setEstado("CONFIRMADO");
                    citaRepository.save(cita);
                }
            } catch (Exception e) {
                System.err.println("Error confirmando pago: " + e.getMessage());
            }
        }
        return new RedirectView("http://localhost:8080/catalogo?pago=exito");
    }
}