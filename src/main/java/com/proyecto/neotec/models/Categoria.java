package com.proyecto.neotec.models;

public class Categoria {
    public int idcategoria;
    public String nombreCategoria;
    public float total;

    public Categoria() {
    }

    public Categoria(int idcategoria, String nombreCategoria, float total) {
        this.idcategoria = idcategoria;
        this.nombreCategoria = nombreCategoria;
        this.total = total;
    }

    public int getIdcategoria() {
        return idcategoria;
    }

    public void setIdCategoria(int idcategoria) {
        this.idcategoria = idcategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }
}
