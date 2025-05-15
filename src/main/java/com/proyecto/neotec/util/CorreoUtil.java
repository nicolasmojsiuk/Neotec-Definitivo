package com.proyecto.neotec.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class CorreoUtil {

    // Cambiá estos valores por los tuyos o cargalos desde config/env
    private static final String CORREO_REMITENTE = "neotec69@gmail.com";
    private static final String CONTRASENA = "lznr ehys hurf paxr";

    public static int enviarCorreo(String destinatario, String asunto, String cuerpo) {
        // Configuración del servidor SMTP (Gmail)
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Crear sesión autenticada
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(CORREO_REMITENTE, CONTRASENA);
            }
        });

        try {
            // Construir el mensaje
            Message mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(CORREO_REMITENTE));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);

            // Enviar mensaje
            Transport.send(mensaje);
            return 1;
        } catch (MessagingException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
