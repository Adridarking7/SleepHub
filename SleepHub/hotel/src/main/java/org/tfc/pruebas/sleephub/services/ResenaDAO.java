package org.tfc.pruebas.sleephub.services;

import org.tfc.pruebas.sleephub.entities.Resena;
import org.tfc.pruebas.sleephub.helper.ConexionBD;

import java.sql.*;
import java.util.*;
import java.sql.Date; // ✅

public class ResenaDAO {

    public List<Map<String, Object>> obtenerResenasDeHabitacion(Long habitacionId) {
        List<Map<String, Object>> resenas = new ArrayList<>();

        String sql = "SELECT r.comentario, r.puntuacion, r.fecha, u.nombre " +
                "FROM resena r " +
                "JOIN usuarios u ON r.usuario_id = u.id " +
                "WHERE r.habitacion_id = ?";

        try (Connection conn = ConexionBD.conectar();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, habitacionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> resena = new HashMap<>();
                resena.put("comentario", rs.getString("comentario"));
                resena.put("puntuacion", rs.getInt("puntuacion"));
                resena.put("fecha", rs.getDate("fecha"));
                resena.put("nombre", rs.getString("nombre"));
                resenas.add(resena);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resenas;
    }

    // ✅ NUEVO MÉTODO PARA INSERTAR UNA RESEÑA
    public void insertarResena(Resena resena) {
        String sql = "INSERT INTO resena (comentario, puntuacion, fecha, usuario_id, habitacion_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.conectar();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, resena.getComentario());
            stmt.setInt(2, resena.getPuntuacion());
            stmt.setDate(3, Date.valueOf(resena.getFecha()));
            stmt.setLong(4, resena.getUsuario().getId());
            stmt.setLong(5, resena.getHabitacionId()); // viene del @Transient

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
