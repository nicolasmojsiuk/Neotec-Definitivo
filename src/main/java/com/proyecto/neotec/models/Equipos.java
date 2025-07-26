package com.proyecto.neotec.models;

public class Equipos {
    private int id;
    private int idcliente;
    private int estado;
    private String observaciones;
    private String fechaIngreso;
    private String fechaModificacion;
    private String fechaSalida;
    private String dispositivo;
    private int activo;
    private String img1;
    private String img2;
    private String img3;
    private String img4;

    public String getImg1() {
        return img1;
    }
    private String nombreCompletoCliente;

    public String getNombreCompletoCliente() {
        return nombreCompletoCliente;
    }

    public void setNombreCompletoCliente(String nombreCompletoCliente) {
        this.nombreCompletoCliente = nombreCompletoCliente;
    }

    public void setImg1(String img1) {
        this.img1 = img1;
    }

    public String getImg2() {
        return img2;
    }

    public void setImg2(String img2) {
        this.img2 = img2;
    }

    public String getImg3() {
        return img3;
    }

    public void setImg3(String img3) {
        this.img3 = img3;
    }

    public String getImg4() {
        return img4;
    }

    public void setImg4(String img4) {
        this.img4 = img4;
    }


    public int getActivo() {
        return activo;
    }

    public void setActivo(int activo) {
        this.activo = activo;
    }

    public Equipos(int idcliente, String dispositivo, int estado, String observaciones, int activo) {
        this.idcliente = idcliente;
        this.dispositivo= dispositivo;
        this.estado = estado;
        this.observaciones = observaciones;
        this.activo = activo;
    }
    public Equipos(int idcliente, String dispositivo, int estado, String observaciones, int id, int activo) {
        this.idcliente = idcliente;
        this.dispositivo= dispositivo;
        this.estado = estado;
        this.observaciones = observaciones;
        this.id = id;
        this.activo= activo;
    }



    public String getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }

    public Equipos() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdcliente() {
        return idcliente;
    }

    public void setIdcliente(int idcliente) {
        this.idcliente = idcliente;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(String fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(String fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public String getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(String fechaSalida) {
        this.fechaSalida = fechaSalida;
    }



}
