package com.proyecto.neotec.util;

import com.proyecto.neotec.models.Usuario;

public class SesionUsuario {

    private static Usuario usuarioLogueado;

    // Método para establecer el usuario logueado
    public static void setUsuarioLogueado(Usuario usuario) {
        usuarioLogueado = usuario;
    }

    // Método para obtener el usuario logueado
    public static Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }

    // Método para cerrar sesión
    public static void cerrarSesion() {
        usuarioLogueado = null;
    }
}

