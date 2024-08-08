package com.elevador;

public class Elevador {
    private int pisoActual;

    public Elevador() {
        this.pisoActual = 1;
    }

    public void subir() {
        pisoActual++;
    }

    public void bajar() {
        pisoActual--;
    }

    public int getPisoActual() {
        return pisoActual;
    }
}
