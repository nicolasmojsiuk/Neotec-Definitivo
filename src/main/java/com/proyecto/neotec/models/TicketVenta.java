package com.proyecto.neotec.models;

import java.time.LocalDateTime;

public class TicketVenta {
    private int idTicketVenta;
    private int idCliente;
    private int idUsuarioVendedor;
    private LocalDateTime fechayhora;
    private Float total;
    private String rutaTicket;
    private String vendedor;
    private String dniVendedor;
    private String cliente;
    private String dniCliente;


    public TicketVenta() {
    }

    public TicketVenta(int idTicketVenta, int idCliente, int idUsuarioVendedor, LocalDateTime fechayhora, Float total, String rutaTicket) {
        this.idTicketVenta = idTicketVenta;
        this.idCliente = idCliente;
        this.idUsuarioVendedor = idUsuarioVendedor;
        this.fechayhora = fechayhora;
        this.total = total;
        this.rutaTicket = rutaTicket;
    }

    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }

    public String getDniVendedor() {
        return dniVendedor;
    }

    public void setDniVendedor(String dniVendedor) {
        this.dniVendedor = dniVendedor;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getDniCliente() {
        return dniCliente;
    }

    public void setDniCliente(String dniCliente) {
        this.dniCliente = dniCliente;
    }

    public int getIdTicketVenta() {
        return idTicketVenta;
    }

    public void setIdTicketVenta(int idTicketVenta) {
        this.idTicketVenta = idTicketVenta;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdUsuarioVendedor() {
        return idUsuarioVendedor;
    }

    public void setIdUsuarioVendedor(int idUsuarioVendedor) {
        this.idUsuarioVendedor = idUsuarioVendedor;
    }

    public LocalDateTime getFechayhora() {
        return fechayhora;
    }

    public void setFechayhora(LocalDateTime fechayhora) {
        this.fechayhora = fechayhora;
    }

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
    }

    public String getRutaTicket() {
        return rutaTicket;
    }

    public void setRutaTicket(String rutaTicket) {
        this.rutaTicket = rutaTicket;
    }
}
