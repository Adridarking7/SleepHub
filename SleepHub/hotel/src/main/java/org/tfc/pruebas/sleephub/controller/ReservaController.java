package org.tfc.pruebas.sleephub.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.tfc.pruebas.sleephub.dto.ReservaDTO;
import org.tfc.pruebas.sleephub.entities.Habitacion;
import org.tfc.pruebas.sleephub.entities.Resena;
import org.tfc.pruebas.sleephub.entities.Reserva;
import org.tfc.pruebas.sleephub.entities.Usuario;
import org.tfc.pruebas.sleephub.exception.DangerException;
import org.tfc.pruebas.sleephub.helper.PRG;
import org.tfc.pruebas.sleephub.repositories.HabitacionRepository;
import org.tfc.pruebas.sleephub.repositories.ResenaRepository;
import org.tfc.pruebas.sleephub.repositories.UsuarioRepository;
import org.tfc.pruebas.sleephub.services.HabitacionService;
import org.tfc.pruebas.sleephub.services.ResenaDAO;
import org.tfc.pruebas.sleephub.services.ReservaService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/reserva")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

    @GetMapping("r")
    public String r(ModelMap m) {
        List<Reserva> reservas = reservaService.listarReservas();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        reservas.forEach(r -> {
            r.setFechaEntradaFormateada(r.getFechaEntrada().format(formatter));
            r.setFechaSalidaFormateada(r.getFechaSalida().format(formatter));
        });

        // Aquí generamos el estado dinámico
        m.put("reservas", reservas);
        m.put("estados", reservas.stream()
                .map(reservaService::obtenerEstadoReserva)
                .map(String::toLowerCase)
                .toList());

        m.put("view", "reserva/r");
        return "_t/frame";
    }

    @GetMapping("c")
    public String c(ModelMap m) {
        m.put("usuarios", usuarioRepository.findAll()); // 🔹 Añadir esta línea
        m.put("habitaciones", habitacionRepository.findAll()); // 🔹 Añadir esta línea
        m.put("view", "reserva/c");
        return "_t/frame";
    }

    @PostMapping("c")
    public String cPost(
            @RequestParam("usuario") Long usuarioId,
            @RequestParam("habitacion") Long habitacionId,
            @RequestParam("fechaEntrada") String fechaEntrada,
            @RequestParam("fechaSalida") String fechaSalida,
            ModelMap m) {
        try {
            LocalDate entrada = LocalDate.parse(fechaEntrada);
            LocalDate salida = LocalDate.parse(fechaSalida);

            if (!salida.isAfter(entrada)) {
                m.put("error", "La fecha de salida debe ser posterior a la de entrada");
            } else if (reservaService.hayConflictoReserva(habitacionId, entrada, salida)) {
                m.put("error", "Ya existe una reserva en ese rango de fechas para esta habitación");
            }

            if (m.containsKey("error")) {
                m.put("usuarios", usuarioRepository.findAll());
                m.put("habitaciones", habitacionRepository.findAll());
                m.put("view", "reserva/c");
                return "_t/frame";
            }

            Reserva r = new Reserva();
            r.setUsuario(usuarioRepository.findById(usuarioId).orElseThrow());
            r.setHabitacion(habitacionRepository.findByIdConReservas(habitacionId).orElseThrow());
            r.setFechaEntrada(entrada);
            r.setFechaSalida(salida);

            reservaService.guardarReserva(r);

        } catch (Exception e) {
            m.put("error", "Error al crear la reserva");
            m.put("usuarios", usuarioRepository.findAll());
            m.put("habitaciones", habitacionRepository.findAll());
            m.put("view", "reserva/c");
            return "_t/frame";
        }

        return "redirect:/reserva/r";
    }

    @GetMapping("u")
    public String u(@RequestParam("id") Long id, ModelMap m) {
        Reserva r = reservaService.buscarPorId(id).orElse(null);
        m.put("reserva", r);
        m.put("usuarios", usuarioRepository.findAll()); // 🔹 Añadir esta línea
        m.put("habitaciones", habitacionRepository.findAll()); // 🔹 Añadir esta línea
        m.put("view", "reserva/u");
        return "_t/frame";
    }

    @PostMapping("u")
    public String uPost(
            @RequestParam("id") Long id,
            @RequestParam("usuario") Long usuarioId,
            @RequestParam("habitacion") Long habitacionId,
            @RequestParam("fechaEntrada") String fechaEntrada,
            @RequestParam("fechaSalida") String fechaSalida,
            ModelMap m) {

        try {
            Reserva r = reservaService.buscarPorId(id).orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            Habitacion habitacion = habitacionRepository.findById(habitacionId)
                    .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));
            LocalDate entrada = LocalDate.parse(fechaEntrada);
            LocalDate salida = LocalDate.parse(fechaSalida);

            // Validación de conflicto (excluyendo la propia reserva actual)
            if (reservaService.hayConflictoReservaExcepto(r.getId().longValue(), habitacionId, entrada, salida)) {
                m.put("error", "Ya existe otra reserva en ese rango de fechas para esta habitación");
                m.put("reserva", r);
                m.put("usuarios", usuarioRepository.findAll());
                m.put("habitaciones", habitacionRepository.findAll());
                m.put("view", "reserva/u");
                return "_t/frame";
            }

            // Si no hay conflicto, guardar
            r.setUsuario(usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado")));
            r.setHabitacion(habitacion);
            r.setFechaEntrada(entrada);
            r.setFechaSalida(salida);
            r.setFechaReserva(LocalDate.now()); // 🔹 Añadir esta línea para forzar actualización

            reservaService.guardarReserva(r);

            return "redirect:/reserva/r";

        } catch (Exception e) {
            e.printStackTrace(); // Esto imprime el error real en la consola
            m.put("error", "Error al actualizar la reserva: " + e.getMessage());
            m.put("reserva", reservaService.buscarPorId(id).orElse(null));
            m.put("usuarios", usuarioRepository.findAll());
            m.put("habitaciones", habitacionRepository.findAll());
            m.put("view", "reserva/u");
            return "_t/frame";
        }
    }

    @PostMapping("d")
    public String d(@RequestParam("id") Long id) throws DangerException {
        try {
            reservaService.eliminarReserva(id);
        } catch (Exception e) {
            PRG.error("No se pudo eliminar la reserva", "/reserva/r");
        }
        return "redirect:/reserva/r";
    }

    @GetMapping("/nueva")
    public String nuevaReservaConHabitacion(
            @RequestParam("habitacionId") Long habitacionId,
            ModelMap m,
            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !(usuario.getRol() == Usuario.Rol.CLIENTE || usuario.getRol() == Usuario.Rol.EDITOR)) {
            session.setAttribute("redirigirDespuesLogin", "/reserva/nueva?habitacionId=" + habitacionId);
            return "redirect:/usuario/login";
        }

        Habitacion habitacion = habitacionRepository.findById(habitacionId).orElse(null);

        if (habitacion != null) {
            LocalDate hoy = LocalDate.now();
            LocalDate hasta = hoy.plusMonths(3);
            List<String> fechasDisponibles = habitacionService.calcularFechasDisponibles(habitacion, hoy, hasta);
            habitacion.setFechasDisponiblesString(String.join(",", fechasDisponibles));

            // ✅ OBTENER RESEÑAS MANUALMENTE CON JDBC
            ResenaDAO dao = new ResenaDAO();
            List<Map<String, Object>> resenas = dao.obtenerResenasDeHabitacion(habitacionId);
            m.put("resenas", resenas);
        }

        m.put("habitacionSeleccionada", habitacion);
        m.put("view", "reserva/crearCliente");
        return "_t/frame";
    }

    @PostMapping("/crear")
    public String crearReservaClientePost(
            @RequestParam("habitacion") Long habitacionId,
            @RequestParam("fechaEntrada") String fechaEntrada,
            @RequestParam("fechaSalida") String fechaSalida,
            HttpSession session,
            ModelMap m) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null || !(usuario.getRol() == Usuario.Rol.CLIENTE || usuario.getRol() == Usuario.Rol.EDITOR)) {
            return "redirect:/usuario/login";
        }

        try {
            LocalDate entrada = LocalDate.parse(fechaEntrada);
            LocalDate salida = LocalDate.parse(fechaSalida);
            LocalDate hoy = LocalDate.now();

            if (entrada.isBefore(hoy) || salida.isBefore(hoy)) {
                m.put("error", "Las fechas no pueden estar en el pasado");
                return redirigirAVistaConHabitacion(habitacionId, m);
            }

            if (!salida.isAfter(entrada)) {
                m.put("error", "La fecha de salida debe ser posterior a la de entrada");
                return redirigirAVistaConHabitacion(habitacionId, m);
            }

            Habitacion habitacion = habitacionRepository.findByIdConReservasYResenas(habitacionId).orElseThrow();

            if (reservaService.hayConflictoReserva(habitacionId, entrada, salida)) {
                m.put("error", "Ya existe una reserva en ese rango de fechas para esta habitación");
                return redirigirAVistaConHabitacion(habitacionId, m);
            }

            Reserva r = new Reserva();
            r.setUsuario(usuario);
            r.setHabitacion(habitacion);
            r.setFechaEntrada(entrada);
            r.setFechaSalida(salida);

            reservaService.guardarReserva(r);

        } catch (Exception e) {
            m.put("error", "Error al realizar la reserva");
            return redirigirAVistaConHabitacion(habitacionId, m);
        }

        return "redirect:/usuario/perfil";
    }

    @GetMapping("/confirmacion")
    public String confirmacionReserva(ModelMap m) {
        m.put("view", "reserva/confirmacion");
        return "_t/frame";
    }

    private String redirigirAVistaConHabitacion(Long habitacionId, ModelMap m) {
        Habitacion habitacion = habitacionRepository.findById(habitacionId).orElse(null);

        if (habitacion != null) {
            // ✅ Cargamos las reseñas manualmente con JOIN FETCH de usuarios
            List<Resena> resenas = resenaRepository.findByHabitacionIdConUsuario(habitacionId);
            habitacion.setResenas(new HashSet<>(resenas)); // Para que no reviente si la entidad usa Set

            // ⏺️ Fechas disponibles
            LocalDate hoy = LocalDate.now();
            LocalDate hasta = hoy.plusMonths(3);
            List<String> fechasDisponibles = habitacionService.calcularFechasDisponibles(habitacion, hoy, hasta);
            habitacion.setFechasDisponiblesString(String.join(",", fechasDisponibles));

            // 👇 Añadimos las reseñas por separado al modelo, por si prefiere usarlas así
            m.put("resenas", resenas);
        }

        m.put("habitacionSeleccionada", habitacion);

        if (habitacion != null) {
            habitacion.getResenas().size(); // 🔧 fuerza la carga aquí también
            System.out.println("RESEÑAS:");
            habitacion.getResenas().forEach(r -> {
                System.out.println(r.getComentario() + " por " + r.getUsuario().getNombre());
            });

            LocalDate hoy = LocalDate.now();
            LocalDate hasta = hoy.plusMonths(3);

            List<String> fechasDisponibles = habitacionService.calcularFechasDisponibles(habitacion, hoy, hasta);
            habitacion.setFechasDisponiblesString(String.join(",", fechasDisponibles));
        }
        m.put("habitacionSeleccionada", habitacion);
        m.put("view", "reserva/crearCliente");
        return "_t/frame";
    }

    @PostMapping(value = "/crear/api", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<?> crearReservaDesdeFetch(
            @RequestBody ReservaDTO datos,
            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null || !(usuario.getRol() == Usuario.Rol.CLIENTE || usuario.getRol() == Usuario.Rol.EDITOR)) {
            return ResponseEntity.status(401).body("Debe iniciar sesión como cliente o editor autorizado");
        }

        try {
            LocalDate entrada = LocalDate.parse(datos.getFechaEntrada());
            LocalDate salida = LocalDate.parse(datos.getFechaSalida());

            if (!salida.isAfter(entrada)) {
                return ResponseEntity.status(400).body("La fecha de salida debe ser posterior a la de entrada");
            }

            if (reservaService.hayConflictoReserva(datos.getHabitacion(), entrada, salida)) {
                return ResponseEntity.status(409).body("La habitación ya está reservada en esas fechas");
            }

            Habitacion habitacion = habitacionRepository.findByIdConReservas(datos.getHabitacion()).orElseThrow();

            Reserva r = new Reserva();
            r.setUsuario(usuario);
            r.setHabitacion(habitacion);
            r.setFechaEntrada(entrada);
            r.setFechaSalida(salida);

            reservaService.guardarReserva(r);

            return ResponseEntity.ok("Reserva realizada correctamente");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al procesar la reserva");
        }
    }

    @GetMapping("/filtro")
    public String filtrarReservas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            ModelMap m) {

        List<Reserva> reservasFiltradas = reservaService.buscarPorFechas(fechaInicio, fechaFin);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        reservasFiltradas.forEach(r -> {
            r.setFechaEntradaFormateada(r.getFechaEntrada().format(formatter));
            r.setFechaSalidaFormateada(r.getFechaSalida().format(formatter));
        });

        List<String> estados = reservasFiltradas.stream()
                .map(reservaService::obtenerEstadoReserva)
                .toList();

        m.put("reservas", reservasFiltradas);
        m.put("estados", estados);
        m.put("view", "reserva/r");
        return "_t/frame";
    }

}