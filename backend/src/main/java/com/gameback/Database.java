package com.gameback;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {

    private static final String URL = "jdbc:sqlite:gameback.db";

    public static Connection connect() throws Exception {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS videojuegos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    titulo TEXT NOT NULL,
                    genero TEXT,
                    anio INTEGER,
                    nota_metacritic INTEGER,
                    plataformas TEXT,
                    descripcion TEXT,
                    emoji TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS resenas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_juego INTEGER NOT NULL,
                    autor TEXT NOT NULL,
                    estrellas INTEGER NOT NULL,
                    texto TEXT NOT NULL,
                    fecha TEXT NOT NULL,
                    FOREIGN KEY (id_juego) REFERENCES videojuegos(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tiendas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_juego INTEGER NOT NULL,
                    nombre TEXT NOT NULL,
                    url TEXT,
                    plataforma TEXT,
                    FOREIGN KEY (id_juego) REFERENCES videojuegos(id)
                )
            """);

            System.out.println("Base de datos inicializada correctamente.");

        } catch (Exception e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }
}