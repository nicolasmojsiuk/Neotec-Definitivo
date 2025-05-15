package com.proyecto.neotec.models;

public class Producto {
    private int idproductos;
    private String codigoProducto;
    private String marca;
    private int cantidad;
    private float precioCosto;
    private float precioUnitario;
    private String descripcion;
    private String nombreProducto;
    private String categoriaString;
    private int categoriaInt;
    private float totalLinea;
    private float ventas;

    public float getVentas() {
        return ventas;
    }

    public void setVentas(float ventas) {
        this.ventas = ventas;
    }

    // Constructor completo
    public Producto(int idproductos, String codigoProducto, String marca, int cantidad, float precioCosto, float precioUnitario, String descripcion, String nombreProducto) {
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
    public Producto() {
    }



    // Constructor para crear un nuevo producto
    public Producto(String nomP, String codigoP, String marca, int cant, float pC, float pU, String desc) {
        this.nombreProducto = nomP;
        this.codigoProducto = codigoP;
        this.marca = marca;
        this.cantidad = cant;
        this.precioCosto = pC;
        this.precioUnitario = pU;
        this.descripcion = desc;
    }

    // Getters y setters


    public float getTotalLinea() {
        return totalLinea;
    }

    public void setTotalLinea(float totalLinea) {
        this.totalLinea = totalLinea;
    }

    public int getIdproductos() {
        return idproductos;
    }

    public void setIdproductos(int idproductos) {
        this.idproductos = idproductos;
    }

    public String getCategoriaString() {
        return categoriaString;
    }

    public void setCategoriaString(String categoriaString) {
        this.categoriaString = categoriaString;
    }

    public int getCategoriaInt() {
        return categoriaInt;
    }

    public void setCategoriaInt(int categoriaInt) {
        this.categoriaInt = categoriaInt;
    }

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

    public float getPrecioCosto() {
        return precioCosto;
    }

    public void setPrecioCosto(float precioCosto) {
        this.precioCosto = precioCosto;
    }

    public float getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(float precioUnitario) {
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
