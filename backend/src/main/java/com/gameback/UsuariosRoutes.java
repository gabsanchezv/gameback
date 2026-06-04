package com.gameback;

import io.javalin.Javalin;
import java.sql.*;
import java.util.*;

public class UsuariosRoutes {

    public static void register(Javalin app) {

        // POST /usuarios/registro - Registrar un usuario nuevo
        app.post("/usuarios/registro", ctx -> {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String sql = "INSERT INTO usuarios (nombre, email, password) VALUES (?, ?, ?)";

            try (Connection conn = Database.connect();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, (String) body.get("nombre"));
                ps.setString(2, (String) body.get("email"));
                ps.setString(3, (String) body.get("password"));
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                int newId = keys.next() ? keys.getInt(1) : -1;
                ctx.status(201).json(Map.of("id", newId, "mensaje", "Usuario registrado correctamente"));

            } catch (SQLException e) {
                if (e.getMessage().contains("UNIQUE")) {
                    ctx.status(409).json(Map.of("error", "El email ya está registrado"));
                } else {
                    ctx.status(500).json(Map.of("error", e.getMessage()));
                }
            }
        });

        // POST /usuarios/login - Iniciar sesión
        app.post("/usuarios/login", ctx -> {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String sql = "SELECT * FROM usuarios WHERE email = ? AND password = ?";

            try (Connection conn = Database.connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, (String) body.get("email"));
                ps.setString(2, (String) body.get("password"));
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    Map<String, Object> usuario = new HashMap<>();
                    usuario.put("id", rs.getInt("id"));
                    usuario.put("nombre", rs.getString("nombre"));
                    usuario.put("email", rs.getString("email"));
                    usuario.put("mensaje", "Login correcto");
                    ctx.json(usuario);
                } else {
                    ctx.status(401).json(Map.of("error", "Email o contraseña incorrectos"));
                }
            }
        });

        // GET /usuarios - Obtener todos los usuarios
        app.get("/usuarios", ctx -> {
            List<Map<String, Object>> usuarios = new ArrayList<>();

            try (Connection conn = Database.connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, nombre, email FROM usuarios")) {

                while (rs.next()) {
                    Map<String, Object> usuario = new HashMap<>();
                    usuario.put("id", rs.getInt("id"));
                    usuario.put("nombre", rs.getString("nombre"));
                    usuario.put("email", rs.getString("email"));
                    usuarios.add(usuario);
                }
            }
            ctx.json(usuarios);
        });
    }
}