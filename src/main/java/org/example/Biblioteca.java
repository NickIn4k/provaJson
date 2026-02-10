package org.example;

import java.util.ArrayList;
import java.util.List;

public class Biblioteca {
    ArrayList<Libro> libro;

    public Biblioteca(ArrayList<Libro> libro) {
        this.libro = libro;
    }

    public Biblioteca() {
        this.libro = new ArrayList<>();
    }

    public ArrayList<Libro> getLibro() {
        return libro;
    }
}
