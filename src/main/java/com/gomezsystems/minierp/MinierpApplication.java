package com.gomezsystems.minierp;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling // 🔥 ENCENDEMOS EL RELOJ AQUÍ
public class MinierpApplication {
	
	@PostConstruct
	public void init() {
		// Fijar zona horaria de Chile para asegurar que los robots (ej. CobranzaRobotService)
		// operen a la hora local, sin importar donde esté alojado el servidor en la nube.
		TimeZone.setDefault(TimeZone.getTimeZone("America/Santiago"));
	}

	public static void main(String[] args) {
		SpringApplication.run(MinierpApplication.class, args);
	}
}