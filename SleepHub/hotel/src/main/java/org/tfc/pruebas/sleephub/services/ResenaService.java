package org.tfc.pruebas.sleephub.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tfc.pruebas.sleephub.entities.Resena;
import org.tfc.pruebas.sleephub.repositories.ResenaRepository;

@Service
public class ResenaService {

    @Autowired
    private ResenaRepository resenaRepo;

    public List<Resena> listarPorHabitacion(Long habitacionId) {
        return resenaRepo.findByHabitacion_Id(habitacionId);
    }

    public void guardar(Resena resena) {
        resenaRepo.save(resena);
    }

    public void eliminarPorId(Long id) {
        resenaRepo.deleteById(id);
    }

}
