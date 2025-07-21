package org.tfc.pruebas.sleephub.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tfc.pruebas.sleephub.entities.Habitacion;
import org.tfc.pruebas.sleephub.entities.ImagenHabitacion;
import org.tfc.pruebas.sleephub.entities.Reserva;
import org.tfc.pruebas.sleephub.entities.Habitacion.Tipo;
import org.tfc.pruebas.sleephub.exception.DangerException;
import org.tfc.pruebas.sleephub.helper.PRG;
import org.tfc.pruebas.sleephub.services.HabitacionService;

@Controller
@RequestMapping("/habitacion")
public class HabitacionController {

    @Autowired
    private HabitacionService habitacionService;

    @GetMapping("r")
    public String r(ModelMap m) {
        m.put("habitaciones", habitacionService.listarHabitaciones());
        m.put("view", "habitacion/r");
        return "_t/frame";
    }

    @GetMapping("c")
    public String c(ModelMap m) {
        m.put("tipos", Habitacion.Tipo.values());
        m.put("view", "habitacion/c");
        return "_t/frame";
    }

    @PostMapping("c")
    public String cPost(
            @RequestParam("numero") Integer numero,
            @RequestParam("tipo") Tipo tipo,
            @RequestParam("precio") Double precio,
            @RequestParam("capacidad") Integer capacidad,
            @RequestParam(name = "descripcion", required = false) String descripcion,
            @RequestParam("imagenes") MultipartFile[] imagenes,
            RedirectAttributes redirectAttrs) {
        try {
            Habitacion h = Habitacion.builder()
                    .numero(numero)
                    .tipo(tipo)
                    .precio(precio)
                    .capacidad(capacidad)
                    .descripcion(descripcion)
                    .build();

            // Crear carpeta para imágenes
            String carpetaNombre = "SleepHub/hotel/src/main/resources/static/img/" + numero;
            Path carpetaRuta = Paths.get(carpetaNombre);
            if (!Files.exists(carpetaRuta)) {
                Files.createDirectories(carpetaRuta);
            }

            List<ImagenHabitacion> listaImagenes = new ArrayList<>();

            for (MultipartFile imagen : imagenes) {
                if (!imagen.isEmpty()) {
                    String nombreArchivo = UUID.randomUUID() + "_" + imagen.getOriginalFilename();
                    Path ruta = carpetaRuta.resolve(nombreArchivo);
                    Files.write(ruta, imagen.getBytes());

                    ImagenHabitacion imgHab = ImagenHabitacion.builder()
                            .url("/img/" + numero + "/" + nombreArchivo)
                            .habitacion(h)
                            .build();

                    listaImagenes.add(imgHab);
                }
            }

            h.setImagenes(listaImagenes);

            habitacionService.guardarHabitacion(h);

            redirectAttrs.addFlashAttribute("success", "Habitación creada correctamente.");
            return "redirect:/habitacion/r";

        } catch (DuplicateKeyException ex) {
            redirectAttrs.addFlashAttribute("error", "Ya existe una habitación con ese número.");
            return "redirect:/habitacion/c";

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Ya existe una habitación con ese número.");
            return "redirect:/habitacion/c";
        }
    }

    @GetMapping("u")
    public String u(@RequestParam("id") Long id, ModelMap m, RedirectAttributes redirectAttrs) {
        Habitacion h = habitacionService.buscarPorId(id).orElse(null);
        if (h == null) {
            redirectAttrs.addFlashAttribute("danger", "Habitación no encontrada.");
            return "redirect:/habitacion/r";
        }
        m.put("habitacion", h);
        m.put("tipos", Habitacion.Tipo.values());
        m.put("view", "habitacion/u");
        return "_t/frame";
    }

    @PostMapping("u")
    public String uPost(
            @RequestParam("id") Long id,
            @RequestParam("numero") Integer numero,
            @RequestParam("tipo") Tipo tipo,
            @RequestParam("precio") Double precio,
            @RequestParam("capacidad") Integer capacidad,
            @RequestParam(name = "descripcion", required = false) String descripcion,
            @RequestParam(name = "nuevasImagenes", required = false) MultipartFile[] nuevasImagenes,
            RedirectAttributes redirectAttrs) throws DangerException {
        try {
            Habitacion h = habitacionService.buscarPorId(id).orElse(null);
            if (h == null) {
                redirectAttrs.addFlashAttribute("danger", "Habitación no encontrada.");
                return "redirect:/habitacion/r";
            }

            h.setNumero(numero);
            h.setTipo(tipo);
            h.setPrecio(precio);
            h.setCapacidad(capacidad);
            h.setDescripcion(descripcion);

            if (nuevasImagenes != null) {
                String carpetaNombre = "SleepHub/hotel/src/main/resources/static/img/" + numero;
                Path carpetaRuta = Paths.get(carpetaNombre);
                if (!Files.exists(carpetaRuta)) {
                    Files.createDirectories(carpetaRuta);
                }
                for (MultipartFile imagen : nuevasImagenes) {
                    if (!imagen.isEmpty()) {
                        String nombreArchivo = UUID.randomUUID() + "_" + imagen.getOriginalFilename();
                        Path ruta = carpetaRuta.resolve(nombreArchivo);
                        Files.write(ruta, imagen.getBytes());

                        h.getImagenes().add(ImagenHabitacion.builder()
                                .url("/img/" + numero + "/" + nombreArchivo)
                                .habitacion(h)
                                .build());
                    }
                }
            }

            habitacionService.guardarHabitacion(h);

            redirectAttrs.addFlashAttribute("success", "Habitación actualizada correctamente.");
            return "redirect:/habitacion/r";

        } catch (Exception e) {
            PRG.error("Error al actualizar habitación", "/habitacion/r");
            return null;
        }
    }

    @PostMapping("d")
    public String d(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            habitacionService.eliminarHabitacion(id);
            redirectAttributes.addFlashAttribute("success", "Habitación eliminada correctamente.");
        } catch (DangerException e) {
            redirectAttributes.addFlashAttribute("danger", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("danger", "No se pudo eliminar la habitación.");
        }
        return "redirect:/habitacion/r";
    }

    @PostMapping("/imagen/d")
    public String eliminarImagen(
            @RequestParam("imagenId") Long imagenId,
            @RequestParam("habitacionId") Long habitacionId,
            RedirectAttributes redirectAttrs) {
        try {
            habitacionService.eliminarImagenPorId(imagenId);
            redirectAttrs.addFlashAttribute("success", "Imagen eliminada correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("danger", "Error al eliminar la imagen.");
        }
        return "redirect:/habitacion/u?id=" + habitacionId;
    }

    @GetMapping("/todas")
    public String verTodasLasHabitaciones(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String estado,
            ModelMap model) {
        try {
            List<Habitacion> habitacionesFiltradas = habitacionService.buscarConFiltros(tipo, estado);

            LocalDate hoy = LocalDate.now().withDayOfMonth(1);
            LocalDate fin = hoy.plusMonths(3).withDayOfMonth(1).minusDays(1);

            for (Habitacion h : habitacionesFiltradas) {
                List<String> fechasDisponibles = habitacionService.calcularFechasDisponibles(h, hoy, fin);
                h.setFechasDisponiblesString(String.join(",", fechasDisponibles));
                h.setEstadoActual(obtenerEstadoDeHabitacion(h));

            }

            model.put("habitaciones", habitacionesFiltradas);
            model.put("view", "habitacion/habitacionesBuscador");
            return "_t/frame";

        } catch (Exception e) {
            e.printStackTrace();
            return "error/paginaError";
        }
    }

    private String obtenerEstadoDeHabitacion(Habitacion hab) {
        LocalDate hoy = LocalDate.now();

        for (Reserva r : hab.getReservas()) {
            if (!r.getFechaEntrada().isAfter(hoy) && !r.getFechaSalida().isBefore(hoy)) {
                switch (r.getEstado().name().toLowerCase()) {
                    case "pendiente":
                        return "ocupada";
                    case "ocupada":
                        return "ocupada";
                    case "finalizada":
                        return "finalizada";
                    case "cancelada":
                        continue; // ignorar canceladas
                }
            }
        }

        return "libre";
    }

    @GetMapping("/habitacion/habitacionesBuscador")
    public String verHabitacionesFiltradas(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer capacidad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            ModelMap m) {

        List<Habitacion> habitaciones = habitacionService.filtrarHabitaciones(tipo, capacidad, fechaInicio, fechaFin);
        m.put("habitaciones", habitaciones);
        m.put("view", "habitacion/habitacionesBuscador");
        return "_t/frame";
    }

    @GetMapping("/filtro")
    public String filtrarHabitaciones(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            ModelMap m) {

        List<Habitacion> habitaciones = habitacionService.filtrarPorTipoYDisponibilidad(tipo, fechaInicio, fechaFin);

        for (Habitacion h : habitaciones) {
            LocalDate hasta = (fechaFin != null) ? fechaFin : LocalDate.now().plusMonths(3);
            List<String> disponibles = habitacionService.calcularFechasDisponibles(h,
                    fechaInicio != null ? fechaInicio : LocalDate.now(), hasta);
            h.setFechasDisponiblesString(String.join(",", disponibles));
        }

        m.put("habitaciones", habitaciones);
        m.put("view", "habitacion/habitacionesBuscador");
        return "_t/frame";
    }

}