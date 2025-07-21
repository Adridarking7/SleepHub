package org.tfc.pruebas.sleephub.controller;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tfc.pruebas.sleephub.entities.Administrador;
import org.tfc.pruebas.sleephub.entities.Cliente;
import org.tfc.pruebas.sleephub.entities.Editor;
import org.tfc.pruebas.sleephub.entities.Reserva;
import org.tfc.pruebas.sleephub.entities.Usuario;
import org.tfc.pruebas.sleephub.exception.DangerException;
import org.tfc.pruebas.sleephub.helper.PRG;
import org.tfc.pruebas.sleephub.services.ReservaService;
import org.tfc.pruebas.sleephub.services.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ReservaService reservaService;

    // Listar usuarios
    @GetMapping("r")
    public String r(ModelMap m) {
        m.put("usuarios", usuarioService.listarUsuarios());
        m.put("view", "usuario/r");
        return "_t/frame";
    }

    // Mostrar formulario creación usuario
    @GetMapping("c")
    public String c(@RequestParam(value = "error", required = false) String error, ModelMap m) {
        if (error != null) {
            m.put("error", error);
        }
        m.put("view", "usuario/c");
        return "_t/frame";
    }

    // Procesar creación usuario (solo con sesión activa)
    @PostMapping("c")
    public String cPost(
            @RequestParam("nombre") String nombre,
            @RequestParam("email") String email,
            @RequestParam("contrasena") String contrasena,
            @RequestParam("tipo") String tipo,
            HttpSession session,
            ModelMap m) throws DangerException {

        if (session.getAttribute("usuario") == null) {
            return "redirect:/usuario/login";
        }

        // Validar contraseña con regex
        if (!contrasenaValida(contrasena)) {
            m.put("error", "La contraseña debe tener al menos 6 caracteres, una mayúscula, una minúscula y un número");
            m.put("view", "usuario/c");
            return "_t/frame";
        }

        try {
            Usuario u;

            switch (tipo) {
                case "EDITOR":
                    u = new Editor();
                    break;
                case "ADMIN":
                    u = new Administrador();
                    break;
                case "CLIENTE":
                default:
                    u = new Cliente();
                    break;
            }

            u.setNombre(nombre);
            u.setEmail(email);
            u.setContrasena(contrasena);
            u.setRol(Usuario.Rol.valueOf(tipo));

            usuarioService.guardarUsuario(u);

        } catch (Exception e) {
            m.put("error", "El correo ya existe en el sistema");
            m.put("view", "usuario/c");
            return "_t/frame";
        }

        return "redirect:/usuario/r";
    }

    // Mostrar formulario para editar usuario
    @GetMapping("u")
    public String u(@RequestParam("id") Long id, ModelMap m) {
        Usuario u = usuarioService.buscarPorId(id).orElse(null);
        m.put("usuario", u);
        m.put("view", "usuario/u");
        return "_t/frame";
    }

    // Cerrar sesión
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/usuario/login";
    }

    // Mostrar formulario login
    @GetMapping("/login")
    public String mostrarLogin(HttpSession session, ModelMap m) {
        if (session.getAttribute("usuario") != null) {
            return "redirect:/usuario/perfil"; // Redirige a perfil si ya está logueado
        }
        m.put("view", "usuario/login");
        return "_t/frame";
    }

    // Procesar login
    @PostMapping("/login")
    public String loginPost(
            @RequestParam("email") String email,
            @RequestParam("contrasena") String contrasena,
            HttpSession session,
            ModelMap m) throws DangerException {

        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(email);

        if (usuarioOpt.isEmpty() || !usuarioService.comprobarContrasena(contrasena, usuarioOpt.get().getContrasena())) {
            m.put("error", "Email o contraseña incorrectos");
            m.put("view", "usuario/login");
            return "_t/frame";
        }

        session.setAttribute("usuario", usuarioOpt.get());

        // Redirección después de login si viene de reserva u otra página protegida
        String redirigir = (String) session.getAttribute("redirigirDespuesLogin");
        if (redirigir != null) {
            session.removeAttribute("redirigirDespuesLogin");
            return "redirect:" + redirigir;
        }

        return "redirect:/"; // si no venía de reserva, lo lleva a perfil
    }

    // Mostrar formulario registro
    @GetMapping("/registro")
    public String mostrarRegistro(ModelMap m) {
        m.put("view", "usuario/registro");
        return "_t/frame";
    }

    // Procesar registro cliente
    @PostMapping("/registro")
    public String registrarCliente(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String contrasena,
            @RequestParam String contrasena2,
            ModelMap m) throws DangerException {

        if (!contrasena.equals(contrasena2)) {
            m.put("error", "Las contraseñas no coinciden.");
            m.put("view", "usuario/registro");
            return "_t/frame";
        }

        if (!contrasenaValida(contrasena)) {
            m.put("error", "La contraseña debe tener al menos 6 caracteres, una mayúscula, una minúscula y un número.");
            m.put("view", "usuario/registro");
            return "_t/frame";
        }

        try {
            Usuario cliente = new Cliente();
            cliente.setNombre(nombre);
            cliente.setEmail(email);
            cliente.setContrasena(contrasena);
            cliente.setRol(Usuario.Rol.CLIENTE);

            usuarioService.guardarUsuario(cliente);
        } catch (Exception e) {
            m.put("error", "Ese correo ya está registrado.");
            m.put("nombre", nombre);
            m.put("email", email);
            m.put("view", "usuario/registro");
            return "_t/frame";
        }

        return "redirect:/usuario/login";
    }

    private boolean contrasenaValida(String contrasena) {
        return contrasena.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$");
    }

    // Procesar actualización usuario
    @PostMapping("u")
    public String uPost(
            @RequestParam("id") Long id,
            @RequestParam("nombre") String nombre,
            @RequestParam("email") String email,
            @RequestParam("contrasena") String contrasena,
            @RequestParam("rol") String rolStr,
            HttpSession session,
            RedirectAttributes redirectAttrs) throws DangerException {

        Usuario sessionUsuario = (Usuario) session.getAttribute("usuario");

        if (sessionUsuario == null ||
                (!sessionUsuario.getId().equals(id) && sessionUsuario.getRol() != Usuario.Rol.ADMINISTRADOR)) {
            return "redirect:/";
        }

        try {
            Usuario u = usuarioService.buscarPorId(id).orElse(null);

            if (u != null) {
                Optional<Usuario> usuarioConEseEmail = usuarioService.buscarPorEmail(email);
                if (usuarioConEseEmail.isPresent() && !usuarioConEseEmail.get().getId().equals(u.getId())) {
                    redirectAttrs.addFlashAttribute("error", "Este email ya está en uso por otro usuario");
                    redirectAttrs.addFlashAttribute("nombre", nombre);
                    redirectAttrs.addFlashAttribute("email", email);
                    redirectAttrs.addFlashAttribute("rol", rolStr);
                    return "redirect:/usuario/u?id=" + id;
                }

                u.setNombre(nombre);
                u.setEmail(email);

                if (contrasena != null && !contrasena.trim().isEmpty()
                        && !contrasena.equals("")
                        && !usuarioService.comprobarContrasena(contrasena, u.getContrasena())) {

                    if (!contrasenaValida(contrasena)) {
                        redirectAttrs.addFlashAttribute("error",
                                "La contraseña debe tener al menos 6 caracteres, una mayúscula, una minúscula y un número.");
                        redirectAttrs.addFlashAttribute("nombre", nombre);
                        redirectAttrs.addFlashAttribute("email", email);
                        redirectAttrs.addFlashAttribute("rol", rolStr);
                        return "redirect:/usuario/u?id=" + id;
                    }

                    u.setContrasena(contrasena);
                }

                if (sessionUsuario.getRol() == Usuario.Rol.ADMINISTRADOR) {
                    u.setRol(Usuario.Rol.valueOf(rolStr));
                }

                usuarioService.guardarUsuario(u);

                if (sessionUsuario.getId().equals(u.getId())) {
                    session.setAttribute("usuario", u);
                }

                redirectAttrs.addFlashAttribute("mensajeExito", "Usuario actualizado correctamente");
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttrs.addFlashAttribute("error", "Error al actualizar usuario");
            redirectAttrs.addFlashAttribute("nombre", nombre);
            redirectAttrs.addFlashAttribute("email", email);
            redirectAttrs.addFlashAttribute("rol", rolStr);
            return "redirect:/usuario/perfil";
        }

        if (sessionUsuario.getRol() == Usuario.Rol.ADMINISTRADOR && !sessionUsuario.getId().equals(id)) {
            return "redirect:/usuario/r";
        } else {
            return "redirect:/usuario/perfil";
        }
    }

    // Eliminar usuario
    @PostMapping("d")
    public String d(@RequestParam("id") Long id, HttpSession session) throws DangerException {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/usuario/login";
        }

        try {
            usuarioService.eliminarUsuario(id);
        } catch (Exception e) {
            PRG.error("No se pudo eliminar el usuario", "/usuario/r");
        }
        return "redirect:/usuario/r";
    }

    // Ver perfil usuario
    @GetMapping("/perfil")
    public String verPerfilCliente(
            HttpSession session, ModelMap m,
            @ModelAttribute("error") String error,
            @ModelAttribute("nombre") String nombre,
            @ModelAttribute("email") String email,
            @ModelAttribute("rol") String rol,
            @ModelAttribute("mensajeExito") String mensajeExito) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/usuario/login";
        }

        m.put("nombre", (nombre == null || nombre.isEmpty()) ? usuario.getNombre() : nombre);
        m.put("email", (email == null || email.isEmpty()) ? usuario.getEmail() : email);
        m.put("rol", (rol == null || rol.isEmpty()) ? usuario.getRol() : rol);
        m.put("usuario", usuario);

        if (error != null && !error.isEmpty()) {
            m.put("error", error);
        }
        if (mensajeExito != null && !mensajeExito.isEmpty()) {
            m.put("mensajeExito", mensajeExito);
        }

        if (usuario.getRol() == Usuario.Rol.CLIENTE) {
            m.put("reservas", reservaService.obtenerReservasPorUsuario(usuario));
        } else {
            m.put("reservas", Collections.emptyList());
        }

        m.put("view", "usuario/perfil");
        return "_t/frame";
    }
    @PostMapping("/cancelar-reserva")
    public String cancelarReservaDesdePerfil(@RequestParam("idReserva") Long idReserva,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");

            Reserva reserva = reservaService.buscarPorId(idReserva)
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            // Verificamos que la reserva pertenece al usuario logueado
            if (!reserva.getUsuario().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tiene permiso para cancelar esta reserva.");
                return "redirect:/usuario/perfil";
            }

            reserva.setEstado(Reserva.Estado.CANCELADA);
            reservaService.guardarReserva(reserva);

            redirectAttributes.addFlashAttribute("mensajeExito", "Reserva cancelada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo cancelar la reserva.");
        }

        return "redirect:/usuario/perfil";
    }
}