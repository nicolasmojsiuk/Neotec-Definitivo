package com.proyecto.neotec.util;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class VolverPantallas {
    private static Scene escenaAnterior;

    // Método para almacenar la escena actual antes de cambiar
    public static void guardarEscenaAnterior(Scene escena) {
        escenaAnterior = escena;
    }

    // Método para volver a la escena anterior y centrar la ventana
    public static void volver(Stage stage) {
        if (escenaAnterior != null) {
            stage.setScene(escenaAnterior);

            // Obtener las dimensiones de la pantalla
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Calcular las coordenadas para centrar la ventana
            stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);

            // Mostrar la escena centrada
            stage.show();
        }
    }
}
