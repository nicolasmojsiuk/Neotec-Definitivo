package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.UsuarioDAO;
import com.proyecto.neotec.models.Usuario;
import com.proyecto.neotec.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.util.Objects;
import java.util.Random;

public class recuperarContraseniaController {
    @FXML
    public TextField txfDni;
    @FXML
    public TextField txfEmail;
    @FXML
    private Pane workspace;
    @FXML
    public void initialize(){
        TextFieldSoloNumeros.allowOnlyNumbers(txfDni);
    }
    public void mostrarAyuda() {
        MostrarAlerta.mostrarAlerta("Recuperar contraseña","Para recuperar su contraseña, debera ingresar los datos solicitados a continuacion.\n" +
                "Luego, le enviaremos un correo con la informacion necesaria para continuar.", Alert.AlertType.INFORMATION);
    }

    public void enviarCorreo() {
        String dni = txfDni.getText();
        String email = txfEmail.getText();
        if (dni.isEmpty() || email.isEmpty()){
            MostrarAlerta.mostrarAlerta("Recuperar contraseña","Debe completar todos los campos", Alert.AlertType.WARNING);
            return;
        }

        int dniInt = Integer.parseInt(dni);

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        Usuario usuario = usuarioDAO.obtenerUsuarioPorDni(dniInt);

        if (usuario == null){
            MostrarAlerta.mostrarAlerta("Recuperar contraseña","El DNI ingresado no corresponde a un usuario registrado.", Alert.AlertType.WARNING);
            return;
        }

        if(!Objects.equals(usuario.getEmail(), email)){
            MostrarAlerta.mostrarAlerta("Recuperar contraseña","El correo ingresado es incorrecto.", Alert.AlertType.WARNING);
            return;
        }
        // Crear token
        Random random = new Random();
        int numero = 100000 + random.nextInt(900000); // de 100000 a 999999
        TokenRecuperacion.setToken(String.valueOf(numero));

        //TODO: si todo es correcto envia el email
        int respuesta = CorreoUtil.enviarCorreo(email,"Recuperacion de contraseña NEOTEC", "HOlA; su codigo temporal de ingreso es: "+TokenRecuperacion.getToken());
        if (respuesta==1){
            CargarPantallas.cargar(workspace,"/vistas/recuperarContrasenia2.fxml");
            SesionUsuario.setUsuarioLogueado(usuario);
        } else if (respuesta==0) {
            MostrarAlerta.mostrarAlerta("Recuperacion de contraseña", "Ocurrio un error al enviar el correo. Verifique su conexion a internet", Alert.AlertType.WARNING);
        }
    }



}
