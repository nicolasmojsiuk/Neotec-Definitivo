package com.proyecto.neotec.models;

public class Usuario {

    // Atributos
    private int idusuarios;
    private String nombre;
    private String apellido;



    private String email;
    private int dni;
    private String contrasenna;
    private String rol;
    private String activo;
    private java.time.LocalDateTime ultimoAcceso;
    private java.time.LocalDateTime fechaCreacion;
    private java.time.LocalDateTime fechaModificacion;

    // Constructor vacío
    public Usuario() {}

    // Constructor con todos los campos
    public Usuario(int idusuarios, String nombre, String apellido, int dni, String email, String contrasenna, String rol, String activo,
                   java.time.LocalDateTime ultimoAcceso, java.time.LocalDateTime fechaCreacion, java.time.LocalDateTime fechaModificacion) {
        this.idusuarios = idusuarios;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.email = email;
        this.contrasenna = contrasenna;
        this.rol = rol;
        this.activo = activo;
        this.ultimoAcceso = ultimoAcceso;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaModificacion;
    }

    // Getters y Setters
    public int getIdusuarios() {
        return idusuarios;
    }

    public void setIdusuarios(int idusuarios) {
        this.idusuarios = idusuarios;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public int getDni() {
        return dni;
    }

    public void setDni(int dni) {
        this.dni = dni;
    }

    public String getContrasenna() {
        return contrasenna;
    }

    public void setContrasenna(String contrasenna) {
        this.contrasenna = contrasenna;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getActivo() {
        return activo;
    }

    public void setActivo(String activo) {
        this.activo = activo;
    }

    public java.time.LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(java.time.LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public java.time.LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(java.time.LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public java.time.LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(java.time.LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    // Método toString para facilitar la depuración
    @Override
    public String toString() {
        return "Usuario{" +
                "idusuarios=" + idusuarios +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", dni='" + dni + '\'' +
                ", contrasenna='" + contrasenna + '\'' +
                ", rol=" + rol +
                ", activo=" + activo +
                ", ultimoAcceso=" + ultimoAcceso +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaModificacion=" + fechaModificacion +
                '}';
    }
}

