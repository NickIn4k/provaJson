package org.example;

public class Copia {
    String tipo;
    Integer disponibili;

    public Copia(String tipo, Integer disponibili) {
        this.tipo = tipo;
        this.disponibili = disponibili;
    }

    public String getTipo() {
        return tipo;
    }

    public Integer getDisponibili() {
        return disponibili;
    }
}