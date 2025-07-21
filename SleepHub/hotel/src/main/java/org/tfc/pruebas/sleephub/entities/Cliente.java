package org.tfc.pruebas.sleephub.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Cliente extends Usuario {
    @Column(name = "fecha_registro", updatable = false)
    private LocalDate fechaRegistro = LocalDate.now();

}