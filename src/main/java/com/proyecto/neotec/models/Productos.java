package com.proyecto.neotec.models;

public class Productos {
    private int idproductos;
    private String codigoProducto;
    private String marca;
    private int cantidad;
    private int precioCosto;
    private int precioUnitario;
    private String descripcion;
    private String nombreProducto;

    // Constructor completo
    public Productos(int idproductos, String codigoProducto, String marca, int cantidad, int precioCosto, int precioUnitario, String descripcion, String nombreProducto) {
        this.idproductos = idproductos;
        this.codigoProducto = codigoProducto;
        this.marca = marca;
        this.cantidad = cantidad;
        this.precioCosto = precioCosto;
        this.precioUnitario = precioUnitario;
        this.descripcion = descripcion;
        this.nombreProducto = nombreProducto;
    }

    // Constructor vacío
    public Productos() {
    }

    // Constructor para crear un nuevo producto
    public Productos(String nomP, String codigoP, String marca, int cant, int pC, int pU, String desc) {
        this.nombreProducto = nomP;
        this.codigoProducto = codigoP;
        this.marca = marca;
        this.cantidad = cant;
        this.precioCosto = pC;
        this.precioUnitario = pU;
        this.descripcion = desc;
    }

    // Getters y setters
    public int getIdProductos() {
        return idproductos;
    }

    public void setIdProductos(int idProductos) {
        this.idproductos = idProductos;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getPrecioCosto() {
        return precioCosto;
    }

    public void setPrecioCosto(int precioCosto) {
        this.precioCosto = precioCosto;
    }

    public int getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(int precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }
}
