package org.tfc.pruebas.sleephub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tfc.pruebas.sleephub.entities.Habitacion;
import org.tfc.pruebas.sleephub.entities.Habitacion.Tipo;
import org.tfc.pruebas.sleephub.exception.DangerException;
import org.tfc.pruebas.sleephub.services.HabitacionService;
import org.tfc.pruebas.sleephub.services.MailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private HabitacionService habitacionService;
    @Autowired
    private MailService emailService;

    @GetMapping("/")
    public String verInicio(ModelMap m) {
        // Obtener lista de habitaciones como antes
        List<Habitacion> habitaciones = habitacionService.listarHabitaciones();

        Habitacion habIndividual = habitaciones.stream()
                .filter(h -> h.getTipo() == Tipo.INDIVIDUAL)
                .findFirst()
                .orElse(null);

        Habitacion habDoble = habitaciones.stream()
                .filter(h -> h.getTipo() == Tipo.DOBLE)
                .findFirst()
                .orElse(null);

        Habitacion habSuite = habitaciones.stream()
                .filter(h -> h.getTipo() == Tipo.SUITE)
                .findFirst()
                .orElse(null);

        m.put("habIndividual", habIndividual);
        m.put("habDoble", habDoble);
        m.put("habSuite", habSuite);

        // Obtener el objeto Authentication de Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Comprobar si el usuario está autenticado y no es anonymous
        boolean loggedIn = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");
        m.put("loggedIn", loggedIn);

        m.put("view", "home/home");

        return "_t/frame";
    }

    @GetMapping("/test")
    public void test() throws Exception {
        throw new DangerException("¡¡¡¡ PUM !!!!!");
    }

    @GetMapping("/contador")
    public String contador(HttpSession s, ModelMap m) {
        if (s.getAttribute("nVisitas") == null) {
            s.setAttribute("nVisitas", 1);
        }
        m.put("visitas", s.getAttribute("nVisitas"));

        s.setAttribute("nVisitas", ((Integer) s.getAttribute("nVisitas")) + 1);
        return "home/contador";
    }

    @GetMapping("/servicios")
    public String mostrarServicios(ModelMap m) {
        m.put("view", "home/servicios"); // ← correcto porque está en /templates/home/
        return "_t/frame"; // ← usa layout general
    }
}