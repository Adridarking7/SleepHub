package org.tfc.pruebas.sleephub.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tfc.pruebas.sleephub.entities.Administrador;
import org.tfc.pruebas.sleephub.entities.Usuario;
import org.tfc.pruebas.sleephub.repositories.UsuarioRepository;

import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostConstruct
    public void crearAdminPorDefecto() {
        Optional<Usuario> adminExistente = usuarioRepository.findByEmail("admin@admin.com");
        if (adminExistente.isEmpty()) {
            Usuario admin = new Administrador();
            admin.setEmail("admin@admin.com");
            admin.setContrasena(encoder.encode("admin123"));
            admin.setRol(Usuario.Rol.ADMINISTRADOR);
            admin.setNombre("Administrador"); // si tu entidad tiene nombre
            usuarioRepository.save(admin);
            System.out.println("Usuario admin creado por defecto.");
        }
    }

    public Usuario guardarUsuario(Usuario usuario) {
        Optional<Usuario> existente = usuarioRepository.findByEmail(usuario.getEmail());

        if (existente.isPresent() && !existente.get().getId().equals(usuario.getId())) {
            throw new RuntimeException("El email ya está registrado. Por favor, use otro.");
        }

        if (!usuario.getContrasena().startsWith("$2a$")) {
            usuario.setContrasena(encoder.encode(usuario.getContrasena()));
        }

        return usuarioRepository.save(usuario);
    }

    public boolean comprobarContrasena(String rawPassword, String hash) {
        return encoder.matches(rawPassword, hash);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}