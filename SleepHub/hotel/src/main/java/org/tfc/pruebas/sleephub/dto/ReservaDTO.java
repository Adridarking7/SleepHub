package org.tfc.pruebas.sleephub.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservaDTO {
    private Long habitacion;
    private String fechaEntrada;
    private String fechaSalida;
}
