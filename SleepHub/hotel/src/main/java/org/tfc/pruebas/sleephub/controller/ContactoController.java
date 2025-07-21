package org.tfc.pruebas.sleephub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tfc.pruebas.sleephub.services.MailService;

@Controller
public class ContactoController {

    @Autowired
    private MailService mailService;

    @PostMapping("/contacto/enviar")
    public String enviarMensaje(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String telefono,
            @RequestParam String mensaje) {
        mailService.enviarMensaje(nombre, email, telefono, mensaje);
        return "redirect:/?enviado=true";
    }
}