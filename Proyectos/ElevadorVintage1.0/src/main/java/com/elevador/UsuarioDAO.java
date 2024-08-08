package com.elevador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // Get a database connection
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    // Retrieve all users from the database
    public List<Usuario> obtenerUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id, nombre FROM usuarios"; // Ensure this query matches your database schema

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                usuarios.add(new Usuario(id, nombre));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener usuarios: " + e.getMessage());
            e.printStackTrace();
        }

        return usuarios;
    }

    // Authenticate user based on username and password
    public boolean autenticar(String nombre, String contraseña) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE nombre = ? AND contraseña = ?"; // Ensure this query matches your database schema

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            pstmt.setString(2, contraseña); // Consider hashing passwords

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // If count is greater than 0, authentication successful
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al autenticar usuario: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
