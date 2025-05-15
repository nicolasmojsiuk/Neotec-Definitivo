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
    private String ultimoAcceso;
    private String fechaCreacion;
    private String fechaModificacion;

    // Constructor vacío
    public Usuario() {}

    // Constructor con todos los campos
    public Usuario(int idusuarios, String nombre, String apellido, int dni, String email, String contrasenna, String rol, String activo,
                   String ultimoAcceso, String fechaCreacion, String fechaModificacion) {
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

    public Usuario(String nombre, String apellido, String email, int dni, String contrasenna, String rol, String activo, String ultimoAcceso, String fechaCreacion, String fechaModificacion) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.dni = dni;
        this.contrasenna = contrasenna;
        this.rol = rol;
        this.activo = activo;
        this.ultimoAcceso = ultimoAcceso;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaModificacion;
    }

    public Usuario(String nombre, String apellido, String email, int dni, String contrasenna, String rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.dni = dni;
        this.contrasenna = contrasenna;
        this.rol = rol;
    }

    public Usuario(int idusuarios, String nombre, String apellido, String email, int dni, String rol) {
        this.idusuarios = idusuarios;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.dni = dni;
        this.rol = rol;
    }

    public Usuario(int idusuarios, String nombre, String apellido, String email, int dni, String contrasenna, String rol) {
        this.idusuarios = idusuarios;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.dni = dni;
        this.contrasenna = contrasenna;
        this.rol = rol;
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

    public String getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(String ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(String fechaModificacion) {
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

