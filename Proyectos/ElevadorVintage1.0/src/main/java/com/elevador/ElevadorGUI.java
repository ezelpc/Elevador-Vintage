package com.elevador;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ElevadorGUI extends JFrame {
    private int pisoActual = 0;
    private JTextField nombreField;
    private JButton[] botonesPisos;
    private JButton subirButton;
    private JButton bajarButton;
    private IndicadorAnalogico indicadorPiso;
    private Queue<Peticion> peticiones;
    private boolean enMovimiento = false;
    private boolean subir = true; // Controla la dirección del movimiento

    public ElevadorGUI() {
        setTitle("Elevador Vintage");
        setSize(500, 400); // Tamaño ajustado para mejor visibilidad
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        nombreField = new JTextField(20);
        subirButton = new JButton("Subir");
        bajarButton = new JButton("Bajar");
        indicadorPiso = new IndicadorAnalogico(); // Usar el indicador analógico
        peticiones = new LinkedList<>();

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 3)); // Ajuste del diseño para incluir el indicador analógico
        panel.setBackground(new Color(255, 240, 220)); // Color vintage para el fondo

        panel.add(new JLabel("Ingresa Tu Nombre Porfavor:", SwingConstants.CENTER));
        panel.add(nombreField);
        panel.add(new JLabel(""));

        botonesPisos = new JButton[8];
        for (int i = 0; i < botonesPisos.length; i++) {
            botonesPisos[i] = new JButton(String.valueOf(i));
            botonesPisos[i].setFont(new Font("Courier New", Font.BOLD, 18)); // Fuente vintage
            botonesPisos[i].setBackground(new Color(210, 180, 140)); // Estilo vintage
            botonesPisos[i].setOpaque(true);
            botonesPisos[i].addActionListener(new PisoButtonListener(i));
            panel.add(botonesPisos[i]);
        }

        panel.add(subirButton);
        panel.add(bajarButton);
        panel.add(indicadorPiso); // Agregar el indicador analógico

        add(panel);

        subirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                subir = true;
                procesarPeticiones();
            }
        });

        bajarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                subir = false;
                procesarPeticiones();
            }
        });
    }

    private class PisoButtonListener implements ActionListener {
        private final int pisoDestino;

        public PisoButtonListener(int pisoDestino) {
            this.pisoDestino = pisoDestino;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            registrarAbordaje(pisoDestino);
            if (!enMovimiento) {
                procesarPeticiones();
            }
        }
    }

    private void procesarPeticiones() {
        enMovimiento = true;
        new Thread(() -> {
            Map<Integer, Integer> peticionesPorPiso = new HashMap<>();
            while (!peticiones.isEmpty()) {
                Peticion peticion = peticiones.poll();
                peticionesPorPiso.merge(peticion.getPisoDestino(), 1, Integer::sum);
            }

            while (!peticionesPorPiso.isEmpty()) {
                boolean debeSubir = peticionesPorPiso.keySet().stream().anyMatch(p -> p > pisoActual);
                boolean debeBajar = peticionesPorPiso.keySet().stream().anyMatch(p -> p < pisoActual);

                // Ajusta la dirección de movimiento
                if (debeSubir && !debeBajar) {
                    subir = true;
                } else if (debeBajar && !debeSubir) {
                    subir = false;
                }

                while ((subir && pisoActual < peticionesPorPiso.keySet().stream().max(Integer::compareTo).orElse(pisoActual)) ||
                        (!subir && pisoActual > peticionesPorPiso.keySet().stream().min(Integer::compareTo).orElse(pisoActual))) {
                    if (subir) {
                        pisoActual++;
                    } else {
                        pisoActual--;
                    }
                    actualizarIndicador();
                    try {
                        Thread.sleep(2500); // Espera de 3.5 segundos entre pisos
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }

                int usuariosBajaron = peticionesPorPiso.getOrDefault(pisoActual, 0);
                peticionesPorPiso.remove(pisoActual);
                mostrarMensaje(usuariosBajaron);
            }

            enMovimiento = false;
        }).start();
    }

    private void actualizarIndicador() {
        SwingUtilities.invokeLater(() -> {
            indicadorPiso.setPisoActual(pisoActual); // Actualizar el indicador de piso
        });
    }

    private void registrarAbordaje(int pisoDestino) {
        String nombre = nombreField.getText();
        if (!nombre.isEmpty()) {
            peticiones.add(new Peticion(nombre, pisoActual, pisoDestino));
            registrarAbordajeEnBD(new Peticion(nombre, pisoActual, pisoDestino)); // Registrar en BD
            // Resetea los campos después de registrar
            nombreField.setText("");
            for (JButton boton : botonesPisos) {
                boton.setEnabled(true);
            }
        }
    }

    private void mostrarMensaje(int usuariosBajaron) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Piso destino alcanzado: " + pisoActual +
                    "\nUsuarios que bajaron: " + usuariosBajaron, "Alerta", JOptionPane.INFORMATION_MESSAGE);
            try {
                Thread.sleep(2000); // Muestra el mensaje durante 5 segundos
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void registrarAbordajeEnBD(Peticion peticion) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO abordajes (nombre_usuario, piso_subida, piso_bajada) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, peticion.nombre);
            stmt.setInt(2, peticion.pisoOrigen);
            stmt.setInt(3, peticion.pisoDestino);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ElevadorGUI::new);
    }

    private static class Peticion {
        private final String nombre;
        private final int pisoOrigen;
        private final int pisoDestino;

        public Peticion(String nombre, int pisoOrigen, int pisoDestino) {
            this.nombre = nombre;
            this.pisoOrigen = pisoOrigen;
            this.pisoDestino = pisoDestino;
        }

        public int getPisoDestino() {
            return pisoDestino;
        }
    }
}

// Clase IndicadorAnalogico
class IndicadorAnalogico extends JPanel {
    private int pisoActual = 0;

    public IndicadorAnalogico() {
        setPreferredSize(new Dimension(100, 200)); // Tamaño del indicador
    }

    public void setPisoActual(int pisoActual) {
        this.pisoActual = pisoActual;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dibuja el indicador analógico
        g2d.setColor(Color.BLACK);
        g2d.drawRect(20, 20, 60, 160);
        g2d.drawLine(50, 20, 50, 180);

        g2d.setColor(Color.RED);
        g2d.fillRect(40, 180 - pisoActual * 20, 20, 20); // Dibuja el indicador de piso actual
    }
}
