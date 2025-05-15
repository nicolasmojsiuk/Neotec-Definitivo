package com.proyecto.neotec.models;

public class Cliente {
    // Atributos
    private int idclientes;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private int dni;
    private String activo;
    private Float compras;

    public Cliente(int idclientes, String nombre, String apellido, String email, String telefono, int dni, String activo) {
        this.idclientes = idclientes;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.dni = dni;
        this.activo = activo;
    }

    public Cliente() {
    }

    public Cliente(int idclientes, String nombre, String apellido, String email, String telefono, int dni) {
        this.idclientes = idclientes;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.dni = dni;
    }

    public Cliente(String nombre, String apellido, String email, String telefono, int dni) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.dni = dni;
    }

    public Float getCompras() {
        return compras;
    }

    public void setCompras(Float compras) {
        this.compras = compras;
    }

    public int getIdclientes() {
        return idclientes;
    }

    public void setIdclientes(int idclientes) {
        this.idclientes = idclientes;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getDni() {
        return dni;
    }

    public void setDni(int dni) {
        this.dni = dni;
    }

    public String getActivo() {
        return activo;
    }

    public void setActivo(String activo) {
        this.activo = activo;
    }
}
