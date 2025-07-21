package org.tfc.pruebas.sleephub.entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity
@Table(name = "administradores")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Administrador extends Usuario {
    @Column(nullable = true) // Add this annotation
    private String departamento = "Oficinas";

    @Column(nullable = true) // Add this annotation
    private Integer nivelAcceso = 1;
}