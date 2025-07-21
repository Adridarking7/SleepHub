package org.tfc.pruebas.sleephub.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tfc.pruebas.sleephub.entities.Habitacion;
import org.tfc.pruebas.sleephub.entities.Habitacion.Tipo;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    @Query("SELECT h FROM Habitacion h LEFT JOIN FETCH h.reservas WHERE h.id = :id")
    Optional<Habitacion> findByIdConReservas(@Param("id") Long id);

    @Query("SELECT h FROM Habitacion h LEFT JOIN FETCH h.reservas")
    List<Habitacion> findAllConReservas();

    @Query("SELECT h FROM Habitacion h LEFT JOIN FETCH h.reservas WHERE h.tipo = :tipo")
    List<Habitacion> findByTipoConReservas(@Param("tipo") Tipo tipo);

    // ✅ Reemplazo de la antigua que causaba error
    @EntityGraph(attributePaths = { "reservas", "resenas" })
    @Query("SELECT h FROM Habitacion h WHERE h.id = :id")
    Optional<Habitacion> findByIdConReservasYResenas(@Param("id") Long id);
}
