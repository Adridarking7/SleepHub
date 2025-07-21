package org.tfc.pruebas.sleephub.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tfc.pruebas.sleephub.entities.Reserva;
import org.tfc.pruebas.sleephub.entities.Usuario;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByUsuario(Usuario usuario); // ya lo tienes

    List<Reserva> findByFechaEntradaBetween(LocalDate desde, LocalDate hasta);

    List<Reserva> findByFechaEntradaAfter(LocalDate desde);

    List<Reserva> findByFechaEntradaBefore(LocalDate hasta);

    List<Reserva> findByHabitacionId(Long habitacionId); // 🔥 lo que faltaba para validar solapamientos
}