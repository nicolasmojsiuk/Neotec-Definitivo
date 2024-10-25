package com.proyecto.neotec.models;

public class Presupuestos {
    private int costoReparacion;
    private int idpresupuesto;
    private int idEquipo;
    private String propietario;
    private String equipo;
    private int estado;
    private int manoDeObra;
    private int precioTotal;
    private int diasEstimados;

    public String getPropietario() {
        return propietario;
    }

    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public int getDiasEstimados() {
        return diasEstimados;
    }

    public void setDiasEstimados(int diasEstimados) {
        this.diasEstimados = diasEstimados;
    }

    public int getCostoReparacion() {
        return costoReparacion;
    }

    public void setCostoReparacion(int costoReparacion) {
        this.costoReparacion = costoReparacion;
    }

    public int getIdpresupuesto() {
        return idpresupuesto;
    }

    public void setIdpresupuesto(int idpresupuesto) {
        this.idpresupuesto = idpresupuesto;
    }

    public int getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(int idEquipo) {
        this.idEquipo = idEquipo;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public int getManoDeObra() {
        return manoDeObra;
    }

    public void setManoDeObra(int manoDeObra) {
        this.manoDeObra = manoDeObra;
    }

    public int getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(int precioTotal) {
        this.precioTotal = precioTotal;
    }
}
