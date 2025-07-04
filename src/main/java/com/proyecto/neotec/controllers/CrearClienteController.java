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
import org.apache.log4j.Logger;

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
    private static final Logger logger = Logger.getLogger(CrearClienteController.class);

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
        try {
            int ndni = Integer.parseInt(dni);

            Cliente nuevoCliente = new Cliente(nombre, apellido, email, telefono, ndni);
            String mensaje = ClienteDAO.crearCliente(nuevoCliente);
            logger.debug("Cliente creado con éxito - Nombre: " + nombre + ", Apellido: " + apellido + ", DNI: " + ndni);

            mostrarAlerta("Creación de Cliente", mensaje, Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            logger.error("Error de formato, Cliente no Creado. Detalle: " + e.getMessage());

            mostrarAlerta(
                    "Error de Formato",
                    "El valor ingresado para el DNI no es válido o es demasiado grande.",
                    Alert.AlertType.ERROR
            );
            return;
        } catch (Exception e) {
            logger.error("Error al Crear al Cliente Detalle: " + e.getMessage());
            mostrarAlerta(
                    "Error al Crear Cliente",
                    "No se pudo crear el cliente. Inténtelo de nuevo.",
                    Alert.AlertType.ERROR
            );
        }
        // Cerrar ventana
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();

    }

    public void cancelar() {
        logger.debug("Operación cancelada por el usuario.");
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
