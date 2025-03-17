package com.proyecto.neotec.util;

import javafx.scene.control.Alert;

public class MostrarAlerta {
    public static void mostrarAlerta(String titulo, String contentText, Alert.AlertType tipoAlerta) {
        Alert alert = new Alert(tipoAlerta);
        alert.setContentText(contentText);
        alert.setTitle(titulo);
        alert.setHeaderText(null);

        // Corregir la ruta al CSS (directamente desde recursos)
        String cssPath = MostrarAlerta.class.getResource("/css/estilos.css").toExternalForm();

        if (cssPath != null) {
            alert.getDialogPane().getStylesheets().add(cssPath);
        } else {
            System.err.println("No se pudo encontrar la ruta CSS: /css/estilos.css");
        }

        alert.showAndWait();
    }
}

