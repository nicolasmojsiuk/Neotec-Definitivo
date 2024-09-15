package com.proyecto.neotec;

import com.proyecto.neotec.util.SesionUsuario;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class HomeController {

    @FXML
    private Label lblReloj;
    @FXML
    private Label lblBienvenida;

    @FXML
    public void initialize(){

        iniciarReloj();

        PauseTransition pausa = new PauseTransition(Duration.seconds(1));
        pausa.setOnFinished(e -> {
            iniciarReloj();
            pausa.play();
        });
        pausa.play();

        lblBienvenida.setText("Bienvenido, "+ SesionUsuario.getUsuarioLogueado().getNombre());
    }

    public void iniciarReloj(){
        LocalTime ahora = LocalTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("HH:mm:ss");
        String tiempoFormateado = ahora.format(formato);
        lblReloj.setText(tiempoFormateado);
    }

}
