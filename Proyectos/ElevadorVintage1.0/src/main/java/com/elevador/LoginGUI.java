package com.elevador;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginGUI extends JFrame {
    private JTextField nombreField;
    private JPasswordField contraseñaField;
    private JButton loginButton;

    public LoginGUI() {
        setTitle("Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        // Fuente vintage
        Font vintageFont = new Font("Serif", Font.BOLD, 16);
        UIManager.put("Button.font", new FontUIResource(vintageFont));
        UIManager.put("Label.font", new FontUIResource(vintageFont));
        UIManager.put("TextField.font", new FontUIResource(vintageFont));
        UIManager.put("PasswordField.font", new FontUIResource(vintageFont));

        // Creación de componentes
        nombreField = new JTextField(20);
        contraseñaField = new JPasswordField(20);
        loginButton = new JButton("Login");

        // Estilo vintage para campos de texto y botones
        Border border = BorderFactory.createLineBorder(new Color(100, 100, 100), 2);
        nombreField.setBorder(border);
        contraseñaField.setBorder(border);
        loginButton.setBackground(new Color(80, 120, 80)); // Ajuste de color vintage para el botón
        loginButton.setForeground(Color.WHITE); // Texto blanco
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createRaisedBevelBorder()); // Efecto de botón vintage

        // Panel con fondo y bordes vintage
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240)); // Fondo gris claro vintage
        panel.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2)); // Borde del panel

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Etiquetas y campos de texto
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Nombre:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(nombreField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Contraseña:"), gbc);

        gbc.gridx = 1;
        panel.add(contraseñaField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(loginButton, gbc);

        add(panel);

        // Acción del botón
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UsuarioDAO usuarioDAO = new UsuarioDAO();
                boolean esAdmin = usuarioDAO.autenticar(nombreField.getText(), new String(contraseñaField.getPassword()));
                if (esAdmin) {
                    AdminGUI adminGUI = new AdminGUI();
                    adminGUI.setVisible(true);
                    dispose();
                } else {
                    ElevadorGUI elevadorGUI = new ElevadorGUI();
                    elevadorGUI.setVisible(true);
                    dispose();
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginGUI loginGUI = new LoginGUI();
            loginGUI.setVisible(true);
        });
    }
}
