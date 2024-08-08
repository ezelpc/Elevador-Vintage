package com.elevador;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;

public class ElevadorGUI extends JFrame {
    private int pisoActual = 0;
    private JTextField nombreField;
    private JButton[] botonesPisos;
    private JButton subirButton;
    private JButton bajarButton;
    private AnalogIndicator indicadorPiso;
    private Queue<Peticion> peticiones;
    private boolean enMovimiento = false;
    private boolean subir = true; // Controla la dirección del movimiento

    public ElevadorGUI() {
        setTitle("Elevador Vintage");
        setSize(500, 500); // Aumentar el tamaño para acomodar el indicador completo
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        nombreField = new JTextField(20);
        subirButton = new JButton("Subir");
        bajarButton = new JButton("Bajar");
        indicadorPiso = new AnalogIndicator(); // Crear el indicador analógico
        peticiones = new LinkedList<>();

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BorderLayout());
        panelPrincipal.setBackground(new Color(255, 250, 240)); // Color vintage para el fondo

        // Panel para el indicador rectangular
        JPanel panelIndicador = new JPanel();
        panelIndicador.setLayout(new BorderLayout());
        panelIndicador.add(indicadorPiso, BorderLayout.CENTER);
        panelPrincipal.add(panelIndicador, BorderLayout.NORTH);

        // Panel para los controles
        JPanel panelControles = new JPanel();
        panelControles.setLayout(new GridLayout(6, 3)); // Ajustado para acomodar los botones y los campos
        panelControles.setBackground(new Color(255, 250, 240)); // Color vintage para el fondo

        panelControles.add(new JLabel("Ingresa Tu Nombre (Opcional):"));
        panelControles.add(nombreField);
        panelControles.add(new JLabel(""));

        botonesPisos = new JButton[8];
        for (int i = 0; i < botonesPisos.length; i++) {
            botonesPisos[i] = new JButton(String.valueOf(i));
            botonesPisos[i].setFont(new Font("Arial", Font.BOLD, 16));
            botonesPisos[i].setBackground(new Color(220, 220, 220)); // Estilo vintage
            botonesPisos[i].setOpaque(true);
            botonesPisos[i].addActionListener(new PisoButtonListener(i));
            panelControles.add(botonesPisos[i]);
        }

        panelControles.add(subirButton);
        panelControles.add(bajarButton);

        panelPrincipal.add(panelControles, BorderLayout.CENTER);

        add(panelPrincipal);

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
            String nombreUsuario = nombreField.getText().trim();
            if (!nombreUsuario.isEmpty()) {
                // Registrar la petición con nombre
                registrarAbordaje(pisoDestino, nombreUsuario);
            } else {
                // Mover el indicador sin registrar en la BD ni mostrar alerta
                moverIndicador(pisoDestino);
            }
        }
    }

    private void moverIndicador(int pisoDestino) {
        new Thread(() -> {
            while (pisoActual != pisoDestino) {
                if (pisoActual < pisoDestino) {
                    pisoActual++;
                } else {
                    pisoActual--;
                }
                indicadorPiso.setPisoActual(pisoActual); // Actualiza el indicador
                try {
                    Thread.sleep(3500); // Espera de 3.5 segundos entre pisos
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
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
                    indicadorPiso.setPisoActual(pisoActual); // Actualiza el indicador
                    try {
                        Thread.sleep(1800); // Espera de 3.5 segundos entre pisos
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

    private void mostrarMensaje(int usuariosBajaron) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Piso destino alcanzado: " + pisoActual +
                    "\nUsuarios que bajaron: " + usuariosBajaron, "Alerta", JOptionPane.INFORMATION_MESSAGE);
            try {
                Thread.sleep(5000); // Muestra el mensaje durante 5 segundos
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void registrarAbordaje(int pisoDestino, String nombre) {
        Peticion peticion = new Peticion(nombre, pisoActual, pisoDestino);
        peticiones.add(peticion);
        registrarAbordajeEnBD(peticion); // Registrar en BD
        // Resetea el campo de nombre después de registrar
        nombreField.setText("");
        for (JButton boton : botonesPisos) {
            boton.setEnabled(true);
        }
        if (!enMovimiento) {
            procesarPeticiones();
        }
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

    // Clase interna para el indicador rectangular
    private static class AnalogIndicator extends JPanel {
        private int pisoActual = 0;
        private final int maxPisos = 7; // Número máximo de pisos

        public AnalogIndicator() {
            setPreferredSize(new Dimension(400, 100)); // Tamaño del indicador
        }

        public void setPisoActual(int pisoActual) {
            this.pisoActual = pisoActual;
            repaint(); // Redibuja el panel cuando se actualice el piso
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            int width = getWidth();
            int height = getHeight();
            int sectionWidth = width / (maxPisos + 1);
            int rectHeight = height - 20;
            int yOffset = 10;

            // Dibuja el rectángulo fraccionado
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, yOffset, width, rectHeight);

            // Dibuja las secciones y los números
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            for (int i = 0; i <= maxPisos; i++) {
                int x = (i * sectionWidth);
                g2d.drawRect(x, yOffset, sectionWidth, rectHeight);
                int numX = x + (sectionWidth / 2) - 5;
                int numY = yOffset + (rectHeight / 2) + 5;
                g2d.drawString(String.valueOf(i), numX, numY);
            }

            // Dibuja la flecha del indicador
            g2d.setColor(Color.RED);
            int arrowSize = 10;
            int arrowX = (pisoActual * sectionWidth) + (sectionWidth / 2);
            int arrowY = yOffset + rectHeight + arrowSize;
            int[] xPoints = {arrowX - arrowSize, arrowX, arrowX + arrowSize};
            int[] yPoints = {arrowY - arrowSize, arrowY, arrowY - arrowSize};
            g2d.fillPolygon(xPoints, yPoints, 3);
        }
    }
}
