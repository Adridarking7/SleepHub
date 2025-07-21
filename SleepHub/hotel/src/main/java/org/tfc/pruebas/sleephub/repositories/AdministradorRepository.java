package org.tfc.pruebas.sleephub.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tfc.pruebas.sleephub.entities.Administrador;

import java.util.List;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    List<Administrador> findByDepartamento(String departamento);
}
