package com.proyecto.neotec.models;

public class HistorialAperturaCierre {
    private String operacion;
    private String fechaHora;
    private String saldo;
    private Usuario responsable;

    public HistorialAperturaCierre(String operacion, String fechaHora, String saldo, Usuario responsable) {
        this.operacion = operacion;
        this.fechaHora = fechaHora;
        this.saldo = saldo;
        this.responsable = responsable;
    }

    // Getters
    public String getOperacion() {
        return operacion;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public String getSaldo() {
        return saldo;
    }

    public Usuario getResponsable() {
        return responsable;
    }
    public String getDniResponsable() {
        return responsable != null ? String.valueOf(responsable.getDni()) : "";
    }

    public String getNombreYApellidoResponsable() {
        return responsable != null ? responsable.getNombre() + " " + responsable.getApellido() : "";
    }
}

