package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.UsuarioDAO;
import com.proyecto.neotec.models.Usuario;
import com.proyecto.neotec.util.CargarPantallas;
import com.proyecto.neotec.util.MostrarAlerta;
import com.proyecto.neotec.util.SesionUsuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.mindrot.jbcrypt.BCrypt;

public class contraseniaRecuperadaController {
    @FXML
    public PasswordField txfContrasenia;
    @FXML
    public PasswordField txfContrasenia2;
    @FXML
    public Pane workspace;

    public void actualizar() {
        if(!(txfContrasenia.getText().equals(txfContrasenia2.getText()))){
            MostrarAlerta.mostrarAlerta("Actualizacion de contraseña","Las contraseñas no coinciden", Alert.AlertType.WARNING);
            return;
        }
        String contrasenna = txfContrasenia.getText();
        if (contrasenna.length() < 8) {
            MostrarAlerta.mostrarAlerta("Error", "La contraseña debe tener al menos 8 caracteres.", Alert.AlertType.WARNING);
            return;
        }
        //hashear la contraseña antes de pasrsela
        String contrasennaHash = BCrypt.hashpw(contrasenna, BCrypt.gensalt(12));
        UsuarioDAO ud = new UsuarioDAO();
        Usuario usLog = SesionUsuario.getUsuarioLogueado();
        String respuesta = ud.actualizarContrasenia(usLog.getIdusuarios(),contrasennaHash);
        MostrarAlerta.mostrarAlerta("Actualizacion de contraseña",respuesta, Alert.AlertType.INFORMATION);
        CargarPantallas.cargar(workspace,"/vistas/login.fxml");
    }
}
