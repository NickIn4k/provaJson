package org.example;

import java.util.ArrayList;

public class Libro {
    String titolo;
    Integer anno;
    Autore autore;
    ArrayList<String> generi;
    ArrayList<Copia> copie;

    public Libro(String titolo, Integer anno, Autore autore, ArrayList<String> generi, ArrayList<Copia> copie) {
        this.titolo = titolo;
        this.anno = anno;
        this.autore = autore;
        this.generi = generi;
        this.copie = copie;
    }

    public String getTitolo() {
        return titolo;
    }

    public Integer getAnno() {
        return anno;
    }

    public Autore getAutore() {
        return autore;
    }

    public ArrayList<String> getGeneri() {
        return generi;
    }

    public ArrayList<Copia> getCopie() {
        return copie;
    }
}
