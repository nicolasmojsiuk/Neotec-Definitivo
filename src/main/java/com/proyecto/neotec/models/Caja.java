package com.proyecto.neotec.models;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class Caja {
    public int idoperacion;
    public String fechayhora;
    public String estado;
    public float saldoInicial;
    public float saldoFinal;
    public Usuario reponsable;

    public Caja() {
    }

    public int getIdoperacion() {
        return idoperacion;
    }

    public void setIdoperacion(int idoperacion) {
        this.idoperacion = idoperacion;
    }

    public String getFechayhora() {
        return fechayhora;
    }

    public void setFechayhora(String fechayhoraApertura) {
        this.fechayhora = fechayhoraApertura;
    }


    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public float getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(float saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    public float getSaldoFinal() {
        return saldoFinal;
    }

    public void setSaldoFinal(float saldoFinal) {
        this.saldoFinal = saldoFinal;
    }

    public Usuario getReponsable() {
        return reponsable;
    }

    public void setReponsable(Usuario reponsable) {
        this.reponsable = reponsable;
    }
}
