package org.tfc.pruebas.sleephub.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tfc.pruebas.sleephub.entities.Resena;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByHabitacion_Id(Long habitacionId);

    @Query("SELECT r FROM Resena r JOIN FETCH r.usuario WHERE r.habitacion.id = :habitacionId")
    List<Resena> findByHabitacionIdConUsuario(@Param("habitacionId") Long habitacionId);

    void deleteById(Long id);

}
