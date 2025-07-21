package org.tfc.pruebas.sleephub.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tfc.pruebas.sleephub.entities.Reserva;
import org.tfc.pruebas.sleephub.entities.Usuario;
import org.tfc.pruebas.sleephub.repositories.ReservaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    /**
     * Guarda una nueva reserva en la base de datos.
     * Valida que no haya conflictos de fechas para la misma habitación.
     */
    public Reserva guardarReserva(Reserva reserva) {
        List<Reserva> reservasExistentes = reservaRepository.findAll();

        for (Reserva existente : reservasExistentes) {
            // Ignora la misma reserva (en caso de edición)
            if (!existente.getId().equals(reserva.getId()) &&
                    existente.getHabitacion().getId().equals(reserva.getHabitacion().getId())) {

                boolean fechasSolapadas = reserva.getFechaEntrada().isBefore(existente.getFechaSalida()) &&
                        reserva.getFechaSalida().isAfter(existente.getFechaEntrada());

                if (fechasSolapadas) {
                    throw new RuntimeException(
                            "La habitación ya está reservada en esas fechas. Elija otra fecha o habitación.");
                }
            }
        }

        return reservaRepository.save(reserva);
    }

    /**
     * Busca una reserva por su ID.
     */
    public Optional<Reserva> buscarPorId(Long id) {
        return reservaRepository.findById(id);
    }

    /**
     * Lista todas las reservas.
     */
    public List<Reserva> listarReservas() {
        return reservaRepository.findAll();
    }

    /**
     * Elimina una reserva por su ID.
     */
    public void eliminarReserva(Long id) {
        reservaRepository.deleteById(id);
    }

    public List<Reserva> obtenerReservasPorUsuario(Usuario usuario) {
        return reservaRepository.findByUsuario(usuario);
    }

    public void cancelarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id).orElse(null);
        if (reserva != null) {
            reserva.setEstado(Reserva.Estado.CANCELADA);
            reservaRepository.save(reserva);
        }
    }

    public boolean hayConflictoReserva(Long habitacionId, LocalDate entrada, LocalDate salida) {
        List<Reserva> reservasExistentes = reservaRepository.findByHabitacionId(habitacionId);

        for (Reserva r : reservasExistentes) {
            // Si las fechas se solapan, hay conflicto
            if (!(salida.isBefore(r.getFechaEntrada()) || entrada.isAfter(r.getFechaSalida()))) {
                return true;
            }
        }
        return false;
    }

    public String obtenerEstadoReserva(Reserva reserva) {
        if (reserva.getEstado() == Reserva.Estado.CANCELADA) {
            return "cancelada";
        } else if (reserva.getEstado() == Reserva.Estado.CONFIRMADA) {
            return "ocupada";
        } else if (reserva.getEstado() == Reserva.Estado.COMPLETADA) {
            return "completada";
        } else {
            // Si está pendiente, comprobamos por fecha
            LocalDate hoy = LocalDate.now();
            if (hoy.isBefore(reserva.getFechaEntrada())) {
                return "pendiente";
            } else if (!hoy.isAfter(reserva.getFechaSalida())) {
                return "ocupada";
            } else {
                return "completada";
            }
        }
    }

    public List<Reserva> buscarPorFechas(LocalDate inicio, LocalDate fin) {
        if (inicio != null && fin != null) {
            return reservaRepository.findByFechaEntradaBetween(inicio, fin);
        } else if (inicio != null) {
            return reservaRepository.findByFechaEntradaAfter(inicio.minusDays(1));
        } else if (fin != null) {
            return reservaRepository.findByFechaEntradaBefore(fin.plusDays(1));
        } else {
            return reservaRepository.findAll();
        }
    }

    public boolean hayConflictoReservaExcepto(Long idActual, Long habitacionId, LocalDate entrada, LocalDate salida) {
        List<Reserva> reservas = reservaRepository.findByHabitacionId(habitacionId);
        return reservas.stream()
                .anyMatch(r -> !r.getId().equals(idActual) &&
                        (entrada.isBefore(r.getFechaSalida()) && salida.isAfter(r.getFechaEntrada())));
    }

}
