package com.proyecto.neotec.models;

public class TransaccionesDigitales {
    public int numeroTransaccion;
    public float monto;
    public int entradaSalida;
    public String Observaciones;

    public TransaccionesDigitales() {
    }

    public TransaccionesDigitales(int numeroTransaccion, float monto, int entradaSalida, String observaciones) {
        this.numeroTransaccion = numeroTransaccion;
        this.monto = monto;
        this.entradaSalida = entradaSalida;
        Observaciones = observaciones;
    }

    public int getNumeroTransaccion() {
        return numeroTransaccion;
    }

    public void setNumeroTransaccion(int numeroTransaccion) {
        this.numeroTransaccion = numeroTransaccion;
    }

    public float getMonto() {
        return monto;
    }

    public void setMonto(float monto) {
        this.monto = monto;
    }

    public int getEntradaSalida() {
        return entradaSalida;
    }

    public void setEntradaSalida(int entradaSalida) {
        this.entradaSalida = entradaSalida;
    }

    public String getObservaciones() {
        return Observaciones;
    }

    public void setObservaciones(String observaciones) {
        Observaciones = observaciones;
    }
}
