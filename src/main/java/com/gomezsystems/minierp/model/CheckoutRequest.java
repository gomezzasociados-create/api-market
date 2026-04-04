package com.gomezsystems.minierp.model;

import java.util.List;

public class CheckoutRequest {
    private List<CartItemDTO> items;
    private String metodoPago;
    private Long clienteId;

    public List<CartItemDTO> getItems() { return items; }
    public void setItems(List<CartItemDTO> items) { this.items = items; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
}
