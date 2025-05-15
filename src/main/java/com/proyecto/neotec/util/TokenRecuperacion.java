package com.proyecto.neotec.util;

import com.proyecto.neotec.models.Usuario;

public class TokenRecuperacion {
    private static String tokenR;

    // Método para establecer el usuario logueado
    public static void setToken(String token) {tokenR = token;}

    // Método para obtener el usuario logueado
    public static String getToken() {
        return tokenR;
    }

    // Método para cerrar sesión
    public static void eliminarToken() {tokenR = null;}
}
