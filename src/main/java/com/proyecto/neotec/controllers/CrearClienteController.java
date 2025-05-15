package com.proyecto.neotec.controllers;
import com.proyecto.neotec.DAO.ClienteDAO;
import com.proyecto.neotec.models.Cliente;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class CrearClienteController {
    @FXML
    private Button btnCancelar;
    @FXML
    private TextField txfNombre;
    @FXML
    private TextField txfApellido;
    @FXML
    private TextField txfDni;
    @FXML
    private TextField txfEmail;
    @FXML
    private TextField txfTelefono;

    @FXML
    public void initialize() {
        restriccionesCampos(); // Aplicar restricciones a los campos
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

    public void crearCliente() {
        String nombre = txfNombre.getText();
        String apellido = txfApellido.getText();
        String dni = (txfDni.getText());
        String email = txfEmail.getText();
        String telefono = txfTelefono.getText();


        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
            mostrarAlerta("Error", "Por favor, complete todos los campos.", Alert.AlertType.WARNING);
            return;
        }

        // Validar el email (debe contener '@' y '.')
        if (!email.contains("@") || !email.contains(".") || email.contains(" ")) {
            mostrarAlerta("Error", "Por favor, ingrese un correo electrónico válido.", Alert.AlertType.WARNING);
            return;
        }

        //convertir dni en un int
        int ndni;
        ndni = Integer.parseInt(dni);

        Cliente clientenuevo = new Cliente(nombre, apellido, email, telefono, ndni);

        String mensaje = ClienteDAO.crearCliente(clientenuevo);

        mostrarAlerta("Creacion De cliente", mensaje, Alert.AlertType.INFORMATION);

        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    public void cancelar() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
