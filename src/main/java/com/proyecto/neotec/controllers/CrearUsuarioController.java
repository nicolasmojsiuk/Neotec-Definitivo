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

        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || email.isEmpty() || contrasenna.isEmpty() || rol == null) {
            mostrarAlerta("Error", "Por favor, complete todos los campos.");
            return;
        }

        // Validar la contraseña (mínimo 8 caracteres)
        if (contrasenna.length() < 8) {
            mostrarAlerta("Error", "La contraseña debe tener al menos 8 caracteres.");
            return;
        }

        // Validar el email (debe contener '@' y '.')
        if (!email.contains("@") || !email.contains(".")) {
            mostrarAlerta("Error", "Por favor, ingrese un correo electrónico válido.");
            return;
        }

        //convertir dni en un int
        int ndni;
        ndni = Integer.parseInt(dni);

        //hashear la contraseña antes de pasrsela
        String contrasennaHash = BCrypt.hashpw(contrasenna, BCrypt.gensalt(12));

        Usuario usuarioNuevo = new Usuario(nombre,apellido,email,ndni,contrasennaHash,rol);
        String mensaje = UsuarioDAO.crearUsuario(usuarioNuevo);


        // Aquí puedes continuar con el proceso de creación del usuario (guardar en la base de datos, etc.)
        mostrarAlerta("Creación de usuario", mensaje);
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    // Método para cerrar el pop-up
    public void cancelar(){
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    // Método para mostrar una alerta
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
