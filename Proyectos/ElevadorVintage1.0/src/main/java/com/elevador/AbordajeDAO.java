package com.elevador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AbordajeDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public List<Abordaje> obtenerAbordajes() {
        List<Abordaje> abordajes = new ArrayList<>();
        String sql = "SELECT id, nombre_usuario, piso_subida, piso_bajada FROM abordajes";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombreUsuario = rs.getString("nombre_usuario");
                int pisoSubida = rs.getInt("piso_subida");
                int pisoBajada = rs.getInt("piso_bajada");
                abordajes.add(new Abordaje(id, nombreUsuario, pisoSubida, pisoBajada));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return abordajes;
    }
}

