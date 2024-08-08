package com.elevador;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Administrador {

    public static void mostrarUsuarios() {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        List<Usuario> usuarios = usuarioDAO.obtenerUsuarios();

        StringBuilder builder = new StringBuilder();
        for (Usuario usuario : usuarios) {
            builder.append("ID: ").append(usuario.getId()).append(" - Nombre: ").append(usuario.getNombre()).append("\n");
        }

        JTextArea textArea = new JTextArea(builder.toString());
        JScrollPane scrollPane = new JScrollPane(textArea);

        JFrame frame = new JFrame("Usuarios Registrados");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setSize(300, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
