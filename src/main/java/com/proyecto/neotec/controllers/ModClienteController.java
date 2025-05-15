package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.ClienteDAO;
import com.proyecto.neotec.DAO.UsuarioDAO;
import com.proyecto.neotec.models.Cliente;
import com.proyecto.neotec.models.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class ModClienteController {

    private Cliente clientemodificacion;

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


    public void setCliente(Cliente cliente) {
        this.clientemodificacion = cliente;
        cargarDatosActuales();
    }

    private void cargarDatosActuales() {
        if (clientemodificacion == null) {
            mostrarAlerta("Error", "No se ha seleccionado ningún usuario.", Alert.AlertType.WARNING);
            return;
        }
        txfNombre.setText(clientemodificacion.getNombre());
        txfApellido.setText(clientemodificacion.getApellido());
        txfDni.setText(String.valueOf(clientemodificacion.getDni()));
        txfEmail.setText(clientemodificacion.getEmail());
        txfTelefono.setText(clientemodificacion.getTelefono());
    }


    @FXML
    public void guardarCliente() {
        // Obtener los datos de los campos
        String nombre = txfNombre.getText();
        String apellido = txfApellido.getText();
        String dni = txfDni.getText();
        String email = txfEmail.getText();
        String telefono = txfTelefono.getText();

        // Validar que todos los campos estén completos
        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
            mostrarAlerta("Error", "Por favor, complete todos los campos.", Alert.AlertType.ERROR);
            return;
        }

        // Validar el email (debe contener '@' y '.')
        if (!email.contains("@") || !email.contains(".") || email.contains(" ")) {
            mostrarAlerta("Error", "Por favor, ingrese un correo electrónico válido.", Alert.AlertType.ERROR);
            return;
        }

        // Obtener el id del usuario que se va a modificar
        int id = clientemodificacion.getIdclientes();

        // Convertir DNI a int
        int ndni;
        try {
            ndni = Integer.parseInt(dni);
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Por favor, ingrese un DNI válido (solo números).", Alert.AlertType.ERROR);
            return;
        }


            // Actualizar cliente
        Cliente clienteModificar = new Cliente(id, nombre, apellido,email,telefono,ndni);
        String mensaje = ClienteDAO.modificarCliente(clienteModificar);

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
