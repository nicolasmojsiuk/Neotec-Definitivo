package com.proyecto.neotec.util;

import com.proyecto.neotec.models.Caja;

public class CajaEstablecida {
    private static Caja cajaEstablecida;

    // Método para establecer el usuario logueado
    public static void setCajaEstablecida(Caja caja) {
        cajaEstablecida = caja;
    }

    // Método para obtener el usuario logueado
    public static Caja getCajaSeleccionada() {
        return cajaEstablecida;
    }

    // Método para cerrar sesión
    public static void deseleccionarCaja() {
        cajaEstablecida = null;
    }
}
