package com.proyecto.neotec.models;

public class Presupuestos {
    private int costosVariables;
    private int idpresupuesto;
    private int idEquipo;
    private String propietario;
    private String equipo;
    private int estado;
    private int manoDeObra;
    private int precioTotal;
    private int diasEstimados;
    private String fechaHora;
    private String observaciones;
    private float totalProductos;

    public Presupuestos(int costosVariables, int idpresupuesto, int idEquipo, String propietario, String equipo, int estado, int manoDeObra, int precioTotal, int diasEstimados, String fechaHora, String observaciones, float totalProductos) {
        this.costosVariables = costosVariables;
        this.idpresupuesto = idpresupuesto;
        this.idEquipo = idEquipo;
        this.propietario = propietario;
        this.equipo = equipo;
        this.estado = estado;
        this.manoDeObra = manoDeObra;
        this.precioTotal = precioTotal;
        this.diasEstimados = diasEstimados;
        this.fechaHora = fechaHora;
        this.observaciones = observaciones;
        this.totalProductos = totalProductos;
    }

    public Presupuestos() {

    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public float getTotalProductos() {
        return totalProductos;
    }

    public void setTotalProductos(float totalProductos) {
        this.totalProductos = totalProductos;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

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

    public int getCostosVariables() {
        return costosVariables;
    }

    public void setCostosVariables(int costosVariables) {
        this.costosVariables = costosVariables;
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
