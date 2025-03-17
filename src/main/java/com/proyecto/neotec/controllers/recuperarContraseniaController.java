package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.UsuarioDAO;
import com.proyecto.neotec.models.Usuario;
import com.proyecto.neotec.util.MostrarAlerta;
import com.proyecto.neotec.util.TextFieldSoloNumeros;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.util.Objects;

public class recuperarContraseniaController {
    @FXML
    public TextField txfDni;
    @FXML
    public TextField txfEmail;

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

        //si toodo es correcto envia el email

    }

    /*
    import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

public class EmailController {

    @FXML
    private TextField recipientField;

    @FXML
    private TextField subjectField;

    @FXML
    private TextArea messageArea;

    public void handleSendEmail() {
        String recipient = recipientField.getText();
        String subject = subjectField.getText();
        String message = messageArea.getText();

        // Configuración del servidor SMTP
        String host = "smtp.gmail.com"; // Cambiar según el proveedor de correo
        String port = "587"; // Puerto para conexiones TLS
        String username = "tu_email@gmail.com"; // Tu correo
        String password = "tu_contraseña"; // Tu contraseña (usa tokens de aplicaciones si aplica)

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Crear el mensaje
            Message emailMessage = new MimeMessage(session);
            emailMessage.setFrom(new InternetAddress(username));
            emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            emailMessage.setSubject(subject);
            emailMessage.setText(message);

            // Enviar el correo
            Transport.send(emailMessage);

            // Mostrar confirmación
            showAlert(Alert.AlertType.INFORMATION, "Correo enviado", "El correo ha sido enviado exitosamente.");
        } catch (MessagingException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo enviar el correo: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

     */
}
