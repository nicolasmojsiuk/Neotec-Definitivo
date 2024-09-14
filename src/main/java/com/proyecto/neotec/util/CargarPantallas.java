package com.proyecto.neotec.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class CargarPantallas {
    public static void cargar(Pane workspace, String rutaFxml){
        try {
            FXMLLoader loader = new FXMLLoader(CargarPantallas.class.getResource(rutaFxml));
            Pane nuevoContenido = loader.load();
            workspace.getChildren().setAll(nuevoContenido);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
