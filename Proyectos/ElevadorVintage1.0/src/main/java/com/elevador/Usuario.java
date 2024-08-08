package com.elevador;

import java.util.Objects;

public class Usuario {
    private int id;
    private String nombre;

    // Constructor
    public Usuario(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Getter for id
    public int getId() {
        return id;
    }

    // Setter for id
    public void setId(int id) {
        this.id = id;
    }

    // Getter for nombre
    public String getNombre() {
        return nombre;
    }

    // Setter for nombre
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", nombre='" + nombre + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id == usuario.id && nombre.equals(usuario.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }
}
