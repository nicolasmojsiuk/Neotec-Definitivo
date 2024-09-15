package com.proyecto.neotec;


import atlantafx.base.theme.PrimerDark;
import com.proyecto.neotec.util.BloquearLogin;
import com.proyecto.neotec.util.VolverPantallas;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class NeotecMultiplayerAPP extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        FXMLLoader fxmlLoader = new FXMLLoader(NeotecMultiplayerAPP.class.getResource("/vistas/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Neotec Multiplayer");
        stage.setScene(scene);
        //este va a servir para cerrar sesion
        VolverPantallas.guardarEscenaAnterior(stage.getScene());
        stage.show();
    }



}