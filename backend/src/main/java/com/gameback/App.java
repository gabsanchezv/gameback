package com.gameback;

import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        Database.init();

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> it.anyHost());
            });
        }).start(8080);

        JuegosRoutes.register(app);
        ResenasRoutes.register(app);
        UsuariosRoutes.register(app);

        System.out.println("GameBack API corriendo en http://localhost:8080");
    }
}