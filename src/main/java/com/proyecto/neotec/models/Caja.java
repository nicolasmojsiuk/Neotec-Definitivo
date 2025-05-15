package com.proyecto.neotec.models;

import java.time.LocalDateTime;

public class Caja {
    public int idcaja;
    public String estado;
    public float saldoActual;
    public String ultimoCambio;

    public Caja(int idcaja, String estado, float saldoActual, String ultimoCambio) {
        this.idcaja = idcaja;
        this.estado = estado;
        this.saldoActual = saldoActual;
        this.ultimoCambio = ultimoCambio;
    }

    public Caja() {
    }

    public Caja(int idcaja) {
        this.idcaja = idcaja;
    }

    public String getUltimoCambio() {
        return ultimoCambio;
    }

    public void setUltimoCambio(String ultimoCambio) {
        this.ultimoCambio = ultimoCambio;
    }

    public int getIdcaja() {
        return idcaja;
    }

    public void setIdcaja(int idcaja) {
        this.idcaja = idcaja;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public float getSaldoActual() {
        return this.saldoActual;
    }

    public void setSaldoActual(float saldoActual) {
        this.saldoActual = saldoActual;
    }
}
