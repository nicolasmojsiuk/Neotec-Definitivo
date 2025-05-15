package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.UsuarioDAO;
import com.proyecto.neotec.models.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class ModUsuarioController {
    private Usuario usuarioModificacion;

    @FXML
    private TextField txfNombre;
    @FXML
    private TextField txfApellido;
    @FXML
    private TextField txfDni;
    @FXML
    private TextField txfEmail;
    @FXML
    private PasswordField pfContrasenna;
    @FXML
    private ComboBox<String> cbRol;
    @FXML
    private Button btnMod;
    @FXML
    private Button btnCancelar;

    @FXML
    public void initialize() {
        restriccionesCampos();
    }

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


    public void setUsuario(Usuario usuario) {
        this.usuarioModificacion = usuario;
        cargarDatosActuales();
    }

    private void cargarDatosActuales() {
        if (usuarioModificacion == null) {
            mostrarAlerta("Error", "No se ha seleccionado ningún usuario.", Alert.AlertType.WARNING);
            return;
        }
        txfNombre.setText(usuarioModificacion.getNombre());
        txfApellido.setText(usuarioModificacion.getApellido());
        txfDni.setText(String.valueOf(usuarioModificacion.getDni()));
        txfEmail.setText(usuarioModificacion.getEmail());
        // Es poco común mostrar la contraseña, pero si es necesario
        pfContrasenna.setText(usuarioModificacion.getContrasenna());
        // Establecer el valor del ComboBox
        cbRol.setValue(usuarioModificacion.getRol());
    }

    @FXML
    public void guardarUsuario() {
        // Obtener los datos de los campos
        String nombre = txfNombre.getText();
        String apellido = txfApellido.getText();
        String dni = txfDni.getText();
        String email = txfEmail.getText();
        String contrasenna = pfContrasenna.getText();
        String rol = cbRol.getValue();

        // Validar que todos los campos estén completos
        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || email.isEmpty() || rol == null) {
            mostrarAlerta("Error", "Por favor, complete todos los campos.", Alert.AlertType.ERROR);
            return;
        }

        // Validar el email (debe contener '@' y '.')
        if (!email.contains("@") || !email.contains(".") || email.contains(" ")) {
            mostrarAlerta("Error", "Por favor, ingrese un correo electrónico válido.", Alert.AlertType.ERROR);
            return;
        }

        // Obtener el id del usuario que se va a modificar
        int id = usuarioModificacion.getIdusuarios();
        String mensaje = "";

        // Convertir DNI a int
        int ndni;
        try {
            ndni = Integer.parseInt(dni);
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Por favor, ingrese un DNI válido (solo números).", Alert.AlertType.ERROR);
            return;
        }

        // Verificar si la contraseña fue proporcionada o está vacía
        if (contrasenna == null || contrasenna.isEmpty()) {
            // Actualizar sin cambiar la contraseña
            Usuario usuarioNuevo = new Usuario(id, nombre, apellido, email, ndni, rol);
            mensaje = UsuarioDAO.modificarUsuarioSinContrasenna(usuarioNuevo);
        } else {
            // Validar la contraseña (mínimo 8 caracteres)
            if (contrasenna.length() < 8) {
                mostrarAlerta("Error", "La contraseña debe tener al menos 8 caracteres.", Alert.AlertType.ERROR);
                return;
            }

            // Hashear la contraseña nueva
            String contrasennaHash = BCrypt.hashpw(contrasenna, BCrypt.gensalt(12));

            // Actualizar con la nueva contraseña hasheada
            Usuario usuarioNuevo = new Usuario(id, nombre, apellido, email, ndni, contrasennaHash, rol);
            mensaje = UsuarioDAO.modificarUsuarioConContrasenna(usuarioNuevo);
        }

        // Mostrar mensaje de confirmación
        mostrarAlerta("Modificación de usuario", mensaje, Alert.AlertType.INFORMATION);

        // Cerrar la ventana después de la modificación
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void cancelar() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipodealerta) {
        Alert alert = new Alert(tipodealerta);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}
