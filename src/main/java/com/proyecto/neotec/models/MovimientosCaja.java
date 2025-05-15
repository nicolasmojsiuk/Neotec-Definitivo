package com.proyecto.neotec.models;

public class MovimientosCaja {
    public int idcajaMovimientos;
    public String entradaSalida;
    public float monto;
    public String fechaHora;
    public int dniResponsable;
    public String nombreResponsable;
    public String tipoMovimiento;

    public MovimientosCaja() {
    }

    public MovimientosCaja(int idcajaMovimientos, String entradaSalida, float monto, String fechaHora, int dniResponsable, String nombreResponsable, String tipoMovimiento) {
        this.idcajaMovimientos = idcajaMovimientos;
        this.entradaSalida = entradaSalida;
        this.monto = monto;
        this.fechaHora = fechaHora;
        this.dniResponsable = dniResponsable;
        this.nombreResponsable = nombreResponsable;
        this.tipoMovimiento = tipoMovimiento;
    }

    public int getIdcajaMovimientos() {
        return idcajaMovimientos;
    }

    public void setIdcajaMovimientos(int idcajaMovimientos) {
        this.idcajaMovimientos = idcajaMovimientos;
    }

    public String getEntradaSalida() {
        return entradaSalida;
    }

    public void setEntradaSalida(String entradaSalida) {
        this.entradaSalida = entradaSalida;
    }

    public float getMonto() {
        return monto;
    }

    public void setMonto(float monto) {
        this.monto = monto;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getDniResponsable() {
        return dniResponsable;
    }

    public void setDniResponsable(int dniResponsable) {
        this.dniResponsable = dniResponsable;
    }

    public String getNombreResponsable() {
        return nombreResponsable;
    }

    public void setNombreResponsable(String nombreResponsable) {
        this.nombreResponsable = nombreResponsable;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }
}
