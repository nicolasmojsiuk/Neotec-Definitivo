package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.UsuarioDAO;
import com.proyecto.neotec.util.BloquearLogin;
import com.proyecto.neotec.util.SesionUsuario;
import com.proyecto.neotec.util.TextFieldSoloNumeros;
import com.proyecto.neotec.util.VolverPantallas;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.IOException;
import org.apache.log4j.Logger;

import static com.proyecto.neotec.util.MostrarAlerta.mostrarAlerta;

public class LoginController {
    @FXML
    private PasswordField pfContrasenna;
    @FXML
    private TextField txfUsuario;
    @FXML
    private Button btnIngresar;

    // Inicializo el contador de intentos en 5
    private int intentos = 5;
    private int minutosBloqueo = 0;
    private static final Logger logger = Logger.getLogger(LoginController.class);

    @FXML
    public void initialize() {
        TextFieldSoloNumeros.allowOnlyNumbers(txfUsuario);

        // Verifica si el login está bloqueado y si el tiempo de bloqueo ha expirado
        if (BloquearLogin.estaBloqueado()) {
            if (BloquearLogin.puedeDesbloquearse()) {
                txfUsuario.setDisable(false);
                pfContrasenna.setDisable(false);
                btnIngresar.setDisable(false);
                minutosBloqueo = 0; // Resetea el tiempo de bloqueo
                BloquearLogin.desbloquearLogin(); // Limpia el registro de bloqueo
            } else {
                txfUsuario.setDisable(true);
                pfContrasenna.setDisable(true);
                btnIngresar.setDisable(true);
            }
        } else {
            txfUsuario.setDisable(false);
            pfContrasenna.setDisable(false);
            btnIngresar.setDisable(false);
        }
    }

    @FXML
    public void iniciarSesion() throws IOException {
        // Toma los datos desde los campos
        int dni;
        String contrasenna = pfContrasenna.getText();

        try {
            dni = Integer.parseInt(txfUsuario.getText());
        } catch (NumberFormatException e) {

            logger.warn("Intento de login con DNI inválido: " + txfUsuario.getText());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Inicio de Sesión");
            alert.setHeaderText("Datos inválidos");
            alert.setContentText("El DNI ingresado no es válido.");
            alert.showAndWait();
            return;
        }

        // Llama a verificarCredenciales()
        if (UsuarioDAO.verificarCredenciales(dni, contrasenna)) {
            Stage stage = (Stage) pfContrasenna.getScene().getWindow();
            VolverPantallas.guardarEscenaAnterior(stage.getScene());

            FXMLLoader loader = new FXMLLoader();

            if (!UsuarioDAO.verificarActivo(dni)){
                logger.warn("Intento de login con usuario inactivo. DNI: " + dni);
                // Si devuelve false, informo que las credenciales son inválidas y resto intentos
                intentos--;
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Inicio de Sesión");
                alert.setHeaderText("No se pudo iniciar sesion");
                alert.setContentText("El usuario esta inactivo");
                alert.showAndWait();
                return;
            }

            // Verificar el rol del usuario para brindar acceso
            if (UsuarioDAO.verificarRol(dni) == 1) {
                loader.setLocation(getClass().getResource("/vistas/indexE.fxml"));
                SesionUsuario.setUsuarioLogueado(UsuarioDAO.obtenerUsuarioPorDni(dni));
                UsuarioDAO.actualizarUltimoAcceso(dni);
            } else {
                loader.setLocation(getClass().getResource("/vistas/indexA.fxml"));
                SesionUsuario.setUsuarioLogueado(UsuarioDAO.obtenerUsuarioPorDni(dni));
                UsuarioDAO.actualizarUltimoAcceso(dni);
            }

            // Cargar la vista correspondiente
            try {
                Parent root = loader.load();
                Scene escena = new Scene(root);
                Stage primaryStage = (Stage) txfUsuario.getScene().getWindow();
                primaryStage.setScene(escena);
                primaryStage.setTitle("Neotec Multiplayer");

                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                primaryStage.setX((screenBounds.getWidth() - primaryStage.getWidth()) / 2);
                primaryStage.setY((screenBounds.getHeight() - primaryStage.getHeight()) / 2);
                primaryStage.show();

                SesionUsuario.setUsuarioLogueado(UsuarioDAO.obtenerUsuarioPorDni(dni));
                UsuarioDAO.actualizarUltimoAcceso(dni);

                logger.info("Inicio de sesión exitoso para el usuario con DNI: " + dni);

            } catch (IOException e) {
                logger.error("Error al cargar la vista luego de login para DNI: " + dni, e);
                mostrarAlerta("Error", "No se pudo cargar la interfaz.", Alert.AlertType.ERROR);
            }

        } else {
            // Si devuelve false, informo que las credenciales son inválidas y resto intentos
            logger.warn("Intento fallido de login. Credenciales invalidas. DNI: " + dni + " | Intentos restantes: " + intentos);
            intentos--;
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Inicio de Sesión");
            alert.setHeaderText("Datos incorrectos");
            alert.setContentText("El DNI o la contraseña ingresados son incorrectos. Intentos restantes: " + intentos);
            alert.showAndWait();

            if (intentos == 0) {
                logger.error("Intento fallido de login. DNI: " + dni + " | Intentos restantes: " + intentos);
                minutosBloqueo = 10;
                alert.setTitle("Inicio de Sesión");
                alert.setHeaderText("Ya no quedan intentos");
                alert.setContentText("Debe esperar " + minutosBloqueo + " minutos para iniciar sesión.");
                BloquearLogin.bloquearLogin(minutosBloqueo);
                alert.showAndWait();
                txfUsuario.setDisable(true);
                pfContrasenna.setDisable(true);
                btnIngresar.setDisable(true);
            }
        }
    }

    @FXML
    public void recuperarContrasenna() throws IOException {
        Stage stage = (Stage) pfContrasenna.getScene().getWindow();
        VolverPantallas.guardarEscenaAnterior(stage.getScene());
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/vistas/recuperarContrasenia.fxml"));
        Parent root = loader.load();
        Scene escena = new Scene(root);
        Stage primaryStage = (Stage) txfUsuario.getScene().getWindow();
        primaryStage.setScene(escena);
        primaryStage.setTitle("Neotec Multiplayer");
        // Centrar la ventana en la pantalla
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX((screenBounds.getWidth() - primaryStage.getWidth()) / 2);
        primaryStage.setY((screenBounds.getHeight() - primaryStage.getHeight()) / 2);
        primaryStage.show();
    }
}
