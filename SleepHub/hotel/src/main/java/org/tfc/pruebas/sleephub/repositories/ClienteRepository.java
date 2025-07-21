package org.tfc.pruebas.sleephub.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tfc.pruebas.sleephub.entities.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
