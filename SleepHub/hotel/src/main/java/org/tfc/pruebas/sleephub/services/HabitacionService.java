package org.tfc.pruebas.sleephub.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tfc.pruebas.sleephub.entities.Habitacion;
import org.tfc.pruebas.sleephub.entities.Habitacion.Tipo;
import org.tfc.pruebas.sleephub.entities.ImagenHabitacion;
import org.tfc.pruebas.sleephub.entities.Reserva;
import org.tfc.pruebas.sleephub.exception.DangerException;
import org.tfc.pruebas.sleephub.repositories.HabitacionRepository;
import org.tfc.pruebas.sleephub.repositories.ImagenHabitacionRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HabitacionService {

    @Autowired
    private HabitacionRepository habitacionRepository;
    @Autowired
    private ImagenHabitacionRepository imagenRepo;

    // Guardar una habitación nueva
    public Habitacion guardarHabitacion(Habitacion habitacion) {
        return habitacionRepository.save(habitacion);
    }

    // Buscar habitación por ID
    public Optional<Habitacion> buscarPorId(Long id) {
        return habitacionRepository.findById(id);
    }

    // Listar todas las habitaciones
    public List<Habitacion> listarHabitaciones() {
        return habitacionRepository.findAll();
    }

    // Eliminar habitación por ID
    public void eliminarHabitacion(Long id) throws DangerException {
        Optional<Habitacion> opt = habitacionRepository.findByIdConReservas(id);
        if (opt.isPresent()) {
            Habitacion h = opt.get();

            // Verificación básica: si tiene reservas, no se elimina
            if (h.getReservas() != null && !h.getReservas().isEmpty()) {
                throw new DangerException("No se puede eliminar la habitación porque tiene reservas asociadas");
            }

            // Ruta de la carpeta de imágenes
            String carpeta = "src/main/resources/static/img/" + h.getNumero();
            Path carpetaRuta = Paths.get(carpeta);

            try {
                // Eliminar todas las imágenes de la carpeta si existe
                if (Files.exists(carpetaRuta)) {
                    Files.walk(carpetaRuta)
                            .sorted((a, b) -> b.compareTo(a)) // eliminar archivos antes que carpetas
                            .forEach(ruta -> {
                                try {
                                    Files.delete(ruta);
                                } catch (Exception e) {
                                    System.err.println("No se pudo borrar: " + ruta);
                                }
                            });
                }

                // Finalmente elimina la habitación
                habitacionRepository.deleteById(id);

            } catch (Exception e) {
                e.printStackTrace();
                throw new DangerException("Error al eliminar habitación y su carpeta de imágenes");
            }
        } else {
            throw new DangerException("Habitación no encontrada");
        }
    }

    public void eliminarImagenPorId(Long id) {
        Optional<ImagenHabitacion> opt = imagenRepo.findById(id);
        if (opt.isPresent()) {
            ImagenHabitacion img = opt.get();
            try {
                Path ruta = Paths.get("src/main/resources/static" + img.getUrl());
                Files.deleteIfExists(ruta);
            } catch (Exception e) {
                e.printStackTrace(); // error al borrar del disco
            }
            imagenRepo.deleteById(id);
        }
    }

    public List<Habitacion> buscarConFiltros(String tipo, String estado) {
        return habitacionRepository.findAll().stream()
                .filter(h -> (tipo == null || tipo.isEmpty() || h.getTipo().name().equalsIgnoreCase(tipo)))
                .filter(h -> (estado == null || estado.isEmpty() || h.getEstado().name().equalsIgnoreCase(estado)))
                .collect(Collectors.toList());
    }

    // HabitacionService.java
    public List<String> calcularFechasDisponibles(Habitacion habitacion, LocalDate desde, LocalDate hasta) {
        System.out.println("=== CALCULANDO FECHAS DISPONIBLES PARA HABITACIÓN ===");
        System.out.println("Habitación ID: " + habitacion.getId());
        System.out.println("Número de reservas asociadas: "
                + (habitacion.getReservas() != null ? habitacion.getReservas().size() : "null"));

        if (habitacion.getReservas() != null) {
            for (Reserva r : habitacion.getReservas()) {
                System.out.println(" - Reserva: " + r.getFechaEntrada() + " -> " + r.getFechaSalida());
            }
        }

        Set<String> ocupadas = new HashSet<>();
        for (Reserva reserva : habitacion.getReservas()) {
            LocalDate ini = reserva.getFechaEntrada();
            LocalDate fin = reserva.getFechaSalida().minusDays(1); // El día de salida NO está ocupado
            while (!ini.isAfter(fin)) {
                ocupadas.add(ini.toString());
                ini = ini.plusDays(1);
            }
        }

        List<String> disponibles = new ArrayList<>();
        LocalDate actual = desde;
        while (!actual.isAfter(hasta)) {
            if (!ocupadas.contains(actual.toString())) {
                disponibles.add(actual.toString());
            }
            actual = actual.plusDays(1);
        }

        System.out.println("Fechas ocupadas: " + ocupadas);
        System.out.println("Fechas disponibles: " + disponibles);
        return disponibles;
    }

    public List<Habitacion> filtrarHabitaciones(String tipo, Integer capacidad, LocalDate fechaInicio,
            LocalDate fechaFin) {
        List<Habitacion> todas = habitacionRepository.findAll();

        return todas.stream()
                .filter(h -> tipo == null || tipo.isBlank() || h.getTipo().name().equalsIgnoreCase(tipo))
                .filter(h -> capacidad == null || h.getCapacidad() >= capacidad)
                .filter(h -> {
                    if (fechaInicio == null || fechaFin == null)
                        return true;
                    return !hayConflictoFechas(h, fechaInicio, fechaFin);
                })
                .toList();
    }

    private boolean hayConflictoFechas(Habitacion h, LocalDate inicio, LocalDate fin) {
        return h.getReservas().stream()
                .anyMatch(r -> (r.getFechaEntrada().isBefore(fin) && r.getFechaSalida().isAfter(inicio)));
    }

    public List<Habitacion> filtrarPorTipoYDisponibilidad(String tipo, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Habitacion> todas = (tipo == null || tipo.isEmpty())
                ? habitacionRepository.findAllConReservas()
                : habitacionRepository.findByTipoConReservas(Tipo.valueOf(tipo));

        return todas.stream()
                .filter(h -> {
                    List<Reserva> reservas = h.getReservas();
                    return reservas.stream().noneMatch(
                            r -> !(r.getFechaSalida().isBefore(fechaInicio) || r.getFechaEntrada().isAfter(fechaFin)));
                })
                .toList();
    }

}
