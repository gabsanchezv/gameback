package com.gameback;

import io.javalin.Javalin;
import java.sql.*;
import java.util.*;

public class JuegosRoutes {

    public static void register(Javalin app) {

        // GET /juegos - Obtener todos los juegos
        app.get("/juegos", ctx -> {
            List<Map<String, Object>> juegos = new ArrayList<>();
            try (Connection conn = Database.connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM videojuegos")) {

                while (rs.next()) {
                    Map<String, Object> juego = new HashMap<>();
                    juego.put("id", rs.getInt("id"));
                    juego.put("titulo", rs.getString("titulo"));
                    juego.put("genero", rs.getString("genero"));
                    juego.put("anio", rs.getInt("anio"));
                    juego.put("nota_metacritic", rs.getInt("nota_metacritic"));
                    juego.put("plataformas", rs.getString("plataformas"));
                    juego.put("descripcion", rs.getString("descripcion"));
                    juego.put("emoji", rs.getString("emoji"));
                    juegos.add(juego);
                }
            }
            ctx.json(juegos);
        });

        // GET /juegos/:id - Obtener un juego por ID
        app.get("/juegos/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            try (Connection conn = Database.connect();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM videojuegos WHERE id = ?")) {

                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    Map<String, Object> juego = new HashMap<>();
                    juego.put("id", rs.getInt("id"));
                    juego.put("titulo", rs.getString("titulo"));
                    juego.put("genero", rs.getString("genero"));
                    juego.put("anio", rs.getInt("anio"));
                    juego.put("nota_metacritic", rs.getInt("nota_metacritic"));
                    juego.put("plataformas", rs.getString("plataformas"));
                    juego.put("descripcion", rs.getString("descripcion"));
                    juego.put("emoji", rs.getString("emoji"));
                    ctx.json(juego);
                } else {
                    ctx.status(404).json(Map.of("error", "Juego no encontrado"));
                }
            }
        });

        // POST /juegos - Añadir un juego nuevo
        app.post("/juegos", ctx -> {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String sql = "INSERT INTO videojuegos (titulo, genero, anio, nota_metacritic, plataformas, descripcion, emoji) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = Database.connect();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, (String) body.get("titulo"));
                ps.setString(2, (String) body.get("genero"));
                ps.setInt(3, body.get("anio") != null ? ((Number) body.get("anio")).intValue() : 0);
                ps.setInt(4, body.get("nota_metacritic") != null ? ((Number) body.get("nota_metacritic")).intValue() : 70);
                ps.setString(5, (String) body.get("plataformas"));
                ps.setString(6, (String) body.get("descripcion"));
                ps.setString(7, body.get("emoji") != null ? (String) body.get("emoji") : "🎮");
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                int newId = keys.next() ? keys.getInt(1) : -1;
                ctx.status(201).json(Map.of("id", newId, "mensaje", "Juego añadido correctamente"));
            }
        });

        // PUT /juegos/:id - Editar un juego
        app.put("/juegos/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String sql = "UPDATE videojuegos SET titulo=?, genero=?, anio=?, nota_metacritic=?, plataformas=?, descripcion=?, emoji=? WHERE id=?";

            try (Connection conn = Database.connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, (String) body.get("titulo"));
                ps.setString(2, (String) body.get("genero"));
                ps.setInt(3, body.get("anio") != null ? ((Number) body.get("anio")).intValue() : 0);
                ps.setInt(4, body.get("nota_metacritic") != null ? ((Number) body.get("nota_metacritic")).intValue() : 70);
                ps.setString(5, (String) body.get("plataformas"));
                ps.setString(6, (String) body.get("descripcion"));
                ps.setString(7, body.get("emoji") != null ? (String) body.get("emoji") : "🎮");
                ps.setInt(8, id);
                ps.executeUpdate();

                ctx.json(Map.of("mensaje", "Juego actualizado correctamente"));
            }
        });

        // DELETE /juegos/:id - Borrar un juego
        app.delete("/juegos/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));

            try (Connection conn = Database.connect();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM videojuegos WHERE id = ?")) {

                ps.setInt(1, id);
                ps.executeUpdate();
                ctx.json(Map.of("mensaje", "Juego eliminado correctamente"));
            }
        });
    }
}