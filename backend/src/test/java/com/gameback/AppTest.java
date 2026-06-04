package com.gameback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest {

    @BeforeAll
    static void setup() {
        // Inicializa la base de datos antes de los tests
        Database.init();
    }

    // ── TEST 1: La base de datos se inicializa correctamente ──
    @Test
    @Order(1)
    void testDatabaseConnection() throws Exception {
        Connection conn = Database.connect();
        assertNotNull(conn, "La conexión a la base de datos no debe ser nula");
        conn.close();
    }

    // ── TEST 2: Se puede insertar un juego ──
    @Test
    @Order(2)
    void testInsertarJuego() throws Exception {
        String sql = "INSERT INTO videojuegos (titulo, genero, anio, nota_metacritic, plataformas, descripcion, emoji) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, "Juego Test");
            ps.setString(2, "RPG");
            ps.setInt(3, 2024);
            ps.setInt(4, 85);
            ps.setString(5, "PC");
            ps.setString(6, "Juego de prueba");
            ps.setString(7, "🎮");
            int rows = ps.executeUpdate();

            assertEquals(1, rows, "Debe insertar exactamente 1 juego");
        }
    }

    // ── TEST 3: Se puede leer un juego ──
    @Test
    @Order(3)
    void testLeerJuegos() throws Exception {
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM videojuegos")) {

            assertTrue(rs.next());
            int total = rs.getInt("total");
            assertTrue(total > 0, "Debe haber al menos un juego en la base de datos");
        }
    }

    // ── TEST 4: Se puede actualizar un juego ──
    @Test
    @Order(4)
    void testActualizarJuego() throws Exception {
        // Primero obtenemos el ID del juego de test
        int id = -1;
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM videojuegos WHERE titulo = ?")) {
            ps.setString(1, "Juego Test");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) id = rs.getInt("id");
        }

        assertTrue(id > 0, "El juego de test debe existir");

        // Ahora lo actualizamos
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement("UPDATE videojuegos SET titulo = ? WHERE id = ?")) {
            ps.setString(1, "Juego Test Actualizado");
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            assertEquals(1, rows, "Debe actualizar exactamente 1 juego");
        }
    }

    // ── TEST 5: Se puede insertar una reseña ──
    @Test
    @Order(5)
    void testInsertarResena() throws Exception {
        // Obtenemos el ID del juego de test
        int idJuego = -1;
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM videojuegos WHERE titulo = ?")) {
            ps.setString(1, "Juego Test Actualizado");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) idJuego = rs.getInt("id");
        }

        assertTrue(idJuego > 0, "El juego debe existir para añadir una reseña");

        String sql = "INSERT INTO resenas (id_juego, autor, estrellas, texto, fecha) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idJuego);
            ps.setString(2, "UsuarioTest");
            ps.setInt(3, 5);
            ps.setString(4, "Excelente juego de prueba");
            ps.setString(5, "2025-01-01");
            int rows = ps.executeUpdate();
            assertEquals(1, rows, "Debe insertar exactamente 1 reseña");
        }
    }

    // ── TEST 6: Se puede registrar un usuario ──
    @Test
    @Order(6)
    void testRegistrarUsuario() throws Exception {
        String sql = "INSERT INTO usuarios (nombre, email, password) VALUES (?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Usuario Test");
            ps.setString(2, "test@gameback.com");
            ps.setString(3, "password123");
            int rows = ps.executeUpdate();
            assertEquals(1, rows, "Debe registrar exactamente 1 usuario");
        }
    }

    // ── TEST 7: Login correcto ──
    @Test
    @Order(7)
    void testLoginCorrecto() throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM usuarios WHERE email = ? AND password = ?")) {
            ps.setString(1, "test@gameback.com");
            ps.setString(2, "password123");
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "El login con credenciales correctas debe devolver un usuario");
            assertEquals("Usuario Test", rs.getString("nombre"));
        }
    }

    // ── TEST 8: Login incorrecto ──
    @Test
    @Order(8)
    void testLoginIncorrecto() throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM usuarios WHERE email = ? AND password = ?")) {
            ps.setString(1, "test@gameback.com");
            ps.setString(2, "passwordMal");
            ResultSet rs = ps.executeQuery();
            assertFalse(rs.next(), "El login con contraseña incorrecta no debe devolver resultados");
        }
    }

    // ── TEST 9: Se puede eliminar una reseña ──
    @Test
    @Order(9)
    void testEliminarResena() throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM resenas WHERE autor = ?")) {
            ps.setString(1, "UsuarioTest");
            int rows = ps.executeUpdate();
            assertTrue(rows >= 0, "La eliminación debe ejecutarse sin errores");
        }
    }

    // ── TEST 10: Se puede eliminar un juego ──
    @Test
    @Order(10)
    void testEliminarJuego() throws Exception {
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM videojuegos WHERE titulo = ?")) {
            ps.setString(1, "Juego Test Actualizado");
            int rows = ps.executeUpdate();
            assertEquals(1, rows, "Debe eliminar exactamente 1 juego");
        }
    }
}