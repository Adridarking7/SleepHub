package org.tfc.pruebas.sleephub.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "habitaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Transient
    private String fechasDisponiblesString;

    @Transient
    private String estadoActual;

    @Column(nullable = false, unique = true)
    private Integer numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    @OneToMany(mappedBy = "habitacion", fetch = FetchType.LAZY)
    private List<Reserva> reservas = new ArrayList<>();

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private Integer capacidad;

    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ImagenHabitacion> imagenes = new ArrayList<>();

    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Resena> resenas = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Estado estado = Estado.DISPONIBLE;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    public enum Tipo {
        INDIVIDUAL, DOBLE, SUITE
    }

    public enum Estado {
        DISPONIBLE, OCUPADA, MANTENIMIENTO
    }

    public String getFechasDisponiblesString() {
        return fechasDisponiblesString;
    }

    public void setFechasDisponiblesString(String fechasDisponiblesString) {
        this.fechasDisponiblesString = fechasDisponiblesString;
    }
}