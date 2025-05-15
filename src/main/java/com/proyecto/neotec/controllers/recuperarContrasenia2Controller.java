package com.proyecto.neotec.controllers;

import com.proyecto.neotec.util.CargarPantallas;
import com.proyecto.neotec.util.MostrarAlerta;
import com.proyecto.neotec.util.TokenRecuperacion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

public class recuperarContrasenia2Controller {
    @FXML
    public TextField txfCodigo;
    @FXML
    public Pane workspace;
    public void verificarToken() {
        String codigoIngresado = txfCodigo.getText();
        if (codigoIngresado==""){
            MostrarAlerta.mostrarAlerta("Recuperacion de contraseña","El campo esta en blanco. Debe ingresar el codigo.", Alert.AlertType.WARNING);
            return;
        }
        System.out.println(TokenRecuperacion.getToken());
        if (codigoIngresado.equals( TokenRecuperacion.getToken())){
            MostrarAlerta.mostrarAlerta("CODIGO VERIFICADO","El codigo ingresado se verifico con exito. A continuacion debera actualizar su contraseña", Alert.AlertType.INFORMATION);
            CargarPantallas.cargar(workspace,"/vistas/contraseniaRecuperada.fxml");
        }
        if (!(codigoIngresado.equals(TokenRecuperacion.getToken()))){
            MostrarAlerta.mostrarAlerta("CODIGO INCORRECTO","El codigo ingresado no es correcto. Intente Nuevamente", Alert.AlertType.INFORMATION);
            CargarPantallas.cargar(workspace,"/vistas/recuperarContrasenia.fxml");
        }

    }
}
