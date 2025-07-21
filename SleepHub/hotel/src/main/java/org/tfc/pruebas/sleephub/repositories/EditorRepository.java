package org.tfc.pruebas.sleephub.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tfc.pruebas.sleephub.entities.Editor;

public interface EditorRepository extends JpaRepository<Editor, Long> {
}
