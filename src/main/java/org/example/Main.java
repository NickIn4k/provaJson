package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        String pathSchema = "src/main/resources/libreria.schema.json";
        String pathLibreria = "src/main/resources/libreria.json";

        // Validazione
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode schemaNode = mapper.readTree(new File(pathSchema));
            JsonNode libreriaNode = mapper.readTree(new File(pathLibreria));

            JsonSchemaFactory fct = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema schema = fct.getSchema(schemaNode);

            Set<ValidationMessage> errors = schema.validate(libreriaNode);

            if(!errors.isEmpty()) {
                for (ValidationMessage error : errors)
                    System.err.println(error.getMessage());
            }
            else
                System.out.println("JSON valido!");
        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
        }

        // Parsing
        String json = null;
        try {
            json = Files.readString(Path.of(pathLibreria));
        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
        }

        Gson gson = new Gson();
        Appoggio ap = gson.fromJson(json, Appoggio.class);

        ArrayList<Libro> libri = ap.getBiblioteca().getLibro();
        for(Libro l : libri) {
            System.out.println(l.getAnno() + ", " + l.getTitolo() + ", " + l.getAutore().nome + " " + l.getAutore().cognome);

            System.out.println("Generi:");
            for(String g : l.getGeneri())
                System.out.println(g);

            System.out.println("Copie:");
            for(Copia c : l.getCopie())
                System.out.println(c.getTipo() + " " + c.getDisponibili());
        }

        // Caricamento nel DB MySQL
        String url = "jdbc:mysql://localhost:3306/";
        String db = "db_libreria_verifica";
        String user = "root";
        String pwd = "";

        try{
            Connection conn = DriverManager.getConnection(url + db, user, pwd);
            for(Libro l : libri) {
                // 1. inserisci autore
                String sql = "INSERT INTO autore (nome, cognome, nazionalita) VALUES (?, ?, ?)";

                PreparedStatement stmtInAutore = conn.prepareStatement(sql);
                stmtInAutore.setString(1, l.getAutore().getNome());
                stmtInAutore.setString(2, l.getAutore().getCognome());
                stmtInAutore.setString(3, l.getAutore().getNazionalita());

                int rows = stmtInAutore.executeUpdate();
                if(rows <= 0){
                    System.err.println("Errore inserimento!");
                    System.exit(-1);
                }
                stmtInAutore.close();

                // 2. select id autore
                sql = "SELECT idAutore FROM autore WHERE nome = ? AND cognome = ?";
                Integer idAutore = -1;

                PreparedStatement stmtSelAutore = conn.prepareStatement(sql);
                stmtSelAutore.setString(1, l.getAutore().getNome());
                stmtSelAutore.setString(2, l.getAutore().getCognome());

                ResultSet rs = stmtSelAutore.executeQuery();
                while(rs.next())
                    idAutore = rs.getInt("idAutore");
                rs.close();

                if(idAutore == -1){
                    System.err.println("Errore ricerca");
                    System.exit(-1);
                }
                stmtSelAutore.close();

                // 3. inserimento libro
                sql = "INSERT INTO libro (titolo, anno, idAutore) VALUES (?, ?, ?)";

                PreparedStatement stmtInLibro = conn.prepareStatement(sql);
                stmtInLibro.setString(1, l.getTitolo());
                stmtInLibro.setInt(2, l.getAnno());
                stmtInLibro.setInt(3, idAutore);

                rows = stmtInLibro.executeUpdate();
                if(rows <= 0){
                    System.err.println("Errore inserimento!");
                    System.exit(-1);
                }
                stmtInLibro.close();

                // 4. select libro
                sql = "SELECT idLibro FROM libro WHERE titolo = ? AND anno = ?";
                Integer idLibro = -1;

                PreparedStatement stmtSelLibro = conn.prepareStatement(sql);
                stmtSelLibro.setString(1,l.getTitolo());
                stmtSelLibro.setInt(2, l.getAnno());

                ResultSet rs2 = stmtSelLibro.executeQuery();
                while(rs2.next())
                    idLibro = rs2.getInt("idLibro");
                rs2.close();

                if(idLibro == -1){
                    System.err.println("Errore ricerca");
                    System.exit(-1);
                }
                stmtSelLibro.close();

                // 5. Inserimento dei generi
                sql = "INSERT INTO genere (nome, idLibro) VALUES (?, ?)";
                PreparedStatement stmtInGenere = conn.prepareStatement(sql);

                for(String g : l.getGeneri()){
                    stmtInGenere.setString(1,g);
                    stmtInGenere.setInt(2, idLibro);
                    rows = stmtInGenere.executeUpdate();

                    if(rows <= 0){
                        System.err.println("Errore inserimento!");
                        System.exit(-1);
                    }
                }
                stmtInGenere.close();

                // 6. Inserimento delle copie
                sql = "INSERT INTO copia (tipo, disponibili, idLibro) VALUES (?, ?, ?)";
                PreparedStatement stmtInCopie = conn.prepareStatement(sql);

                for(Copia c : l.getCopie()){
                    stmtInCopie.setString(1, c.getTipo());
                    stmtInCopie.setInt(2, c.getDisponibili());
                    stmtInCopie.setInt(3, idLibro);

                    rows = stmtInCopie.executeUpdate();
                    if(rows <= 0) {
                        System.err.println("Errore inserimento!");
                        System.exit(-1);
                    }
                }
                stmtInCopie.close();

                System.out.println("Caricamento completato!");
            }
        } catch (RuntimeException | SQLException e) {
            System.err.println("Errore di connessione: " + e.getMessage());
        }
    }
}