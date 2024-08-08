package com.elevador;

public class Abordaje {
    private int id;
    private String nombreUsuario;
    private int pisoSubida;
    private int pisoBajada;

    public Abordaje(int id, String nombreUsuario, int pisoSubida, int pisoBajada) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.pisoSubida = pisoSubida;
        this.pisoBajada = pisoBajada;
    }

    public int getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public int getPisoSubida() {
        return pisoSubida;
    }

    public int getPisoBajada() {
        return pisoBajada;
    }
}
