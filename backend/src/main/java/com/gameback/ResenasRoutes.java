package com.gameback;

import io.javalin.Javalin;
import java.sql.*;
import java.util.*;

public class ResenasRoutes {

    public static void register(Javalin app) {

        // GET /resenas/:id_juego - Obtener reseñas de un juego
        app.get("/resenas/{id_juego}", ctx -> {
            int idJuego = Integer.parseInt(ctx.pathParam("id_juego"));
            List<Map<String, Object>> resenas = new ArrayList<>();

            try (Connection conn = Database.connect();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM resenas WHERE id_juego = ?")) {

                ps.setInt(1, idJuego);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    Map<String, Object> resena = new HashMap<>();
                    resena.put("id", rs.getInt("id"));
                    resena.put("id_juego", rs.getInt("id_juego"));
                    resena.put("autor", rs.getString("autor"));
                    resena.put("estrellas", rs.getInt("estrellas"));
                    resena.put("texto", rs.getString("texto"));
                    resena.put("fecha", rs.getString("fecha"));
                    resenas.add(resena);
                }
            }
            ctx.json(resenas);
        });

        // GET /resenas - Obtener todas las reseñas
        app.get("/resenas", ctx -> {
            List<Map<String, Object>> resenas = new ArrayList<>();

            try (Connection conn = Database.connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT r.*, v.titulo as titulo_juego FROM resenas r JOIN videojuegos v ON r.id_juego = v.id ORDER BY r.id DESC")) {

                while (rs.next()) {
                    Map<String, Object> resena = new HashMap<>();
                    resena.put("id", rs.getInt("id"));
                    resena.put("id_juego", rs.getInt("id_juego"));
                    resena.put("titulo_juego", rs.getString("titulo_juego"));
                    resena.put("autor", rs.getString("autor"));
                    resena.put("estrellas", rs.getInt("estrellas"));
                    resena.put("texto", rs.getString("texto"));
                    resena.put("fecha", rs.getString("fecha"));
                    resenas.add(resena);
                }
            }
            ctx.json(resenas);
        });

        // POST /resenas - Añadir una reseña
        app.post("/resenas", ctx -> {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String sql = "INSERT INTO resenas (id_juego, autor, estrellas, texto, fecha) VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = Database.connect();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, ((Number) body.get("id_juego")).intValue());
                ps.setString(2, (String) body.get("autor"));
                ps.setInt(3, ((Number) body.get("estrellas")).intValue());
                ps.setString(4, (String) body.get("texto"));
                ps.setString(5, java.time.LocalDate.now().toString());
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                int newId = keys.next() ? keys.getInt(1) : -1;
                ctx.status(201).json(Map.of("id", newId, "mensaje", "Reseña publicada correctamente"));
            }
        });

        // DELETE /resenas/:id - Borrar una reseña
        app.delete("/resenas/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));

            try (Connection conn = Database.connect();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM resenas WHERE id = ?")) {

                ps.setInt(1, id);
                ps.executeUpdate();
                ctx.json(Map.of("mensaje", "Reseña eliminada correctamente"));
            }
        });
    }
}