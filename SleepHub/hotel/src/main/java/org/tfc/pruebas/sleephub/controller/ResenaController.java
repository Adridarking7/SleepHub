package org.tfc.pruebas.sleephub.controller;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tfc.pruebas.sleephub.entities.*;
import org.tfc.pruebas.sleephub.entities.Usuario.Rol;
import org.tfc.pruebas.sleephub.repositories.*;
import org.tfc.pruebas.sleephub.services.ResenaService;
import org.springframework.ui.ModelMap;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/resena")
public class ResenaController {

    @Autowired
    private ResenaRepository resenaRepo;

    @Autowired
    private ResenaService resenaService;

    @Autowired
    private HabitacionRepository habitacionRepository;

    @PostMapping("/crear")
    public String crear(
            @RequestParam Long habitacionId,
            @RequestParam int puntuacion,
            @RequestParam String comentario,
            HttpSession session,
            ModelMap m) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null || (usuario.getRol() != Usuario.Rol.CLIENTE && usuario.getRol() != Usuario.Rol.EDITOR)) {
            return "redirect:/usuario/login";
        }

        Habitacion habitacion = habitacionRepository.findById(habitacionId).orElse(null);
        if (habitacion == null)
            return "redirect:/";

        Resena r = Resena.builder()
                .usuario(usuario)
                .habitacion(habitacion)
                .comentario(comentario)
                .puntuacion(puntuacion)
                .fecha(LocalDate.now())
                .build();

        resenaRepo.save(r);
        session.setAttribute("resenaEnviada", "ok");

        return "redirect:/reserva/nueva?habitacionId=" + habitacionId + "&resena=ok";
    }

    @PostMapping("/eliminar")
    public String eliminarResena(@RequestParam("resenaId") Long resenaId, HttpSession session,
            RedirectAttributes redirectAttrs) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null || usuario.getRol() != Rol.EDITOR) {
            redirectAttrs.addFlashAttribute("error", "No tiene permiso para realizar esta acción.");
            return "redirect:/";
        }

        resenaService.eliminarPorId(resenaId);
        redirectAttrs.addFlashAttribute("msg", "Reseña eliminada correctamente.");
        return "redirect:/resena/r";
    }

    @GetMapping("/r")
    public String listar(ModelMap m, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null || usuario.getRol() != Rol.EDITOR) {
            return "redirect:/";
        }

        try {
            m.put("resenas", resenaRepo.findAll());
            m.put("view", "resena/r"); // 👈 Esto es clave
            return "_t/frame"; // 👈 Esto es lo que carga su layout principal
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/?error=resenas";
        }
    }

}
