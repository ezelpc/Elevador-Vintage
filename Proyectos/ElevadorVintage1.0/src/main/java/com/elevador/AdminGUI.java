package com.elevador;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class AdminGUI extends JFrame {
    private JTable table;
    private AbordajeDAO abordajeDAO;

    public AdminGUI() {
        setTitle("Panel de Administración");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Configuración de la apariencia vintage
        Font vintageFont = new Font("Serif", Font.BOLD, 16);

        // Configuración de UIManager para uniformar los estilos vintage
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Table.font", vintageFont);
            UIManager.put("Button.font", vintageFont);
            UIManager.put("Label.font", vintageFont);
            UIManager.put("Table.background", new Color(245, 245, 220)); // Beige
            UIManager.put("Table.gridColor", new Color(139, 69, 19)); // Marrón
            UIManager.put("Table.headerBackground", new Color(160, 82, 45)); // Marrón oscuro
            UIManager.put("Table.headerForeground", Color.WHITE);
            UIManager.put("Button.background", new Color(139, 69, 19)); // Marrón
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Panel.background", new Color(245, 245, 220)); // Beige
        } catch (Exception e) {
            e.printStackTrace();
        }

        abordajeDAO = new AbordajeDAO();
        table = new JTable();
        table.setBorder(BorderFactory.createLineBorder(new Color(139, 69, 19), 2)); // Borde vintage
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Botón de cerrar sesión con estilo vintage
        JButton logoutButton = new JButton("Cerrar Sesión");
        logoutButton.setFont(new Font("Serif", Font.BOLD, 16));
        logoutButton.setBackground(new Color(139, 69, 19)); // Marrón
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createRaisedBevelBorder()); // Efecto de botón vintage
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Regresar a la pantalla de login
                dispose(); // Cierra la ventana actual
                new LoginGUI().setVisible(true); // Abre la ventana de login
            }
        });

        JPanel panel = new JPanel();
        panel.setBackground(new Color(245, 245, 220)); // Beige
        panel.add(logoutButton);
        add(panel, BorderLayout.SOUTH);

        // Configura un Timer para actualizar la tabla cada segundo
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarTabla();
            }
        });
        timer.start();

        // Inicializa la tabla con datos
        actualizarTabla();
    }

    private void actualizarTabla() {
        List<Abordaje> abordajes = abordajeDAO.obtenerAbordajes();

        String[] columnNames = {"Nombre Usuario", "Piso Subida", "Piso Bajada"};
        String[][] data = new String[abordajes.size()][3]; // Cambiado de 4 a 3 columnas

        for (int i = 0; i < abordajes.size(); i++) {
            Abordaje abordaje = abordajes.get(i);
            data[i][0] = abordaje.getNombreUsuario(); // Nombre Usuario
            data[i][1] = String.valueOf(abordaje.getPisoSubida()); // Piso Subida
            data[i][2] = String.valueOf(abordaje.getPisoBajada()); // Piso Bajada
        }

        table.setModel(new javax.swing.table.DefaultTableModel(data, columnNames));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminGUI().setVisible(true));
    }
}
