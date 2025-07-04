package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.UsuarioDAO;
import com.proyecto.neotec.models.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class CrearUsuarioController {
    @FXML
    private TextField txfNombre;
    @FXML
    private TextField txfApellido;
    @FXML
    private TextField txfDni;
    @FXML
    private TextField txfEmail;
    @FXML
    private TextField pfContrasenna;
    @FXML
    private ComboBox<String> cbRol;
    @FXML
    private Button btnCrear;
    @FXML
    private Button btnCancelar;
    private static final Logger logger = Logger.getLogger(CrearUsuarioController.class);

    @FXML
    public void initialize(){
        restriccionesCampos(); // Aplicar restricciones a los campos
        cbRol.getItems().addAll("empleado", "administrador"); // Agregar opciones al ComboBox
    }

    // Método para aplicar restricciones a los campos
    private void restriccionesCampos() {
        // Permitir solo números en el campo de DNI
        Pattern pattern = Pattern.compile("\\d*");
        TextFormatter<String> formatter = new TextFormatter<>((UnaryOperator<TextFormatter.Change>) change ->
                pattern.matcher(change.getControlNewText()).matches() ? change : null);
        txfDni.setTextFormatter(formatter);

        txfDni.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (event.getCharacter().equals(" ")) {
                event.consume();  // Bloquea el espacio
            }
        });

    }

    // Método para validar los datos e intentar crear un usuario
    public void crearUsuario(){
        String nombre = txfNombre.getText();
        String apellido = txfApellido.getText();
        String dni = txfDni.getText();
        String email = txfEmail.getText();
        String contrasenna = pfContrasenna.getText();
        String rol = cbRol.getValue();

        logger.debug("Datos ingresados - Nombre: " + nombre + ", Apellido: " + apellido + ", DNI: " + dni + ", Email: " + email + ", Rol: " + rol);

        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || email.isEmpty() || contrasenna.isEmpty() || rol == null) {
            logger.warn("Faltan campos obligatorios - Nombre: " + nombre + ", Apellido: " + apellido + ", DNI: " + dni + ", Email: " + email + ", Contraseña vacía: " + contrasenna.isEmpty() + ", Rol: " + rol);
            mostrarAlerta("Error", "Por favor, complete todos los campos.", Alert.AlertType.WARNING);
            return;
        }

        // Validar la contraseña (mínimo 8 caracteres)
        if (contrasenna.length() < 8) {
            logger.warn("Contraseña demasiado corta (longitud: " + contrasenna.length() + ")");
            mostrarAlerta("Error", "La contraseña debe tener al menos 8 caracteres.", Alert.AlertType.WARNING);
            return;
        }

        // Validar el email (debe contener '@' y '.')
        if (!email.contains("@") || !email.contains(".") || email.contains(" ")) {
            logger.warn("Email inválido ingresado: " + email);
            mostrarAlerta("Error", "Por favor, ingrese un correo electrónico válido.", Alert.AlertType.WARNING);
            return;
        }

        logger.info("Todos los datos del usuario validados correctamente.");

        try {
            int ndni = Integer.parseInt(dni);

            // Hashear la contraseña con BCrypt
            String contrasennaHash = BCrypt.hashpw(contrasenna, BCrypt.gensalt(12));

            Usuario nuevoUsuario = new Usuario(nombre, apellido, email, ndni, contrasennaHash, rol);
            String mensaje = UsuarioDAO.crearUsuario(nuevoUsuario);
            logger.debug("Usuario Creado con éxito");
            mostrarAlerta("Creación de Usuario", mensaje, Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            logger.error("Error, Formato de DNI Invalido. Detalles " + e.getMessage());
            mostrarAlerta(
                    "Error de Formato",
                    "El valor ingresado para el DNI no es válido o es demasiado grande.",
                    Alert.AlertType.ERROR
            );
        } catch (Exception e) {
            logger.error("Error al crear el usuario. Detalle: " + e.getMessage());
            mostrarAlerta(
                    "Error al Crear Usuario",
                    "Ocurrió un error inesperado. Verifique los datos e intente nuevamente.",
                    Alert.AlertType.ERROR
            );
        }
        // Cerrar la ventana
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();


    }

    // Método para cerrar el pop-up
    public void cancelar(){
        logger.debug("Operación cancelada por el usuario.");
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    // Método para mostrar una alerta
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
