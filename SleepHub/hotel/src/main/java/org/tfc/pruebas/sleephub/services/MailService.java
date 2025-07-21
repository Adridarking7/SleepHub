package org.tfc.pruebas.sleephub.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarMensaje(String nombre, String email, String telefono, String mensaje) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo("sleephub3@gmail.com"); // Tu correo para recibir mensajes
        mail.setSubject("Nuevo mensaje desde la web del hotel");
        mail.setText(
                "Nombre: " + nombre + "\nEmail: " + email + "\nTeléfono: " + telefono + "\n\nMensaje:\n" + mensaje);
        mailSender.send(mail);
    }
}
