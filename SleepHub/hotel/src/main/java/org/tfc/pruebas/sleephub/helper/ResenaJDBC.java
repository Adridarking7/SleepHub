package org.tfc.pruebas.sleephub.helper;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class ResenaJDBC {

    public List<Map<String, Object>> obtenerResenasPorHabitacion(Long habitacionId) {
        List<Map<String, Object>> resenas = new ArrayList<>();

        try (Connection conn = ConexionBD.conectar()) {
            String sql = "SELECT r.comentario, r.puntuacion, r.fecha, u.nombre AS usuario " +
                    "FROM resena r JOIN usuarios u ON r.usuario_id = u.id " +
                    "WHERE r.habitacion_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, habitacionId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("comentario", rs.getString("comentario"));
                    fila.put("puntuacion", rs.getInt("puntuacion"));
                    fila.put("fecha", rs.getDate("fecha").toLocalDate());
                    fila.put("usuario", rs.getString("usuario"));
                    resenas.add(fila);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resenas;
    }
}
