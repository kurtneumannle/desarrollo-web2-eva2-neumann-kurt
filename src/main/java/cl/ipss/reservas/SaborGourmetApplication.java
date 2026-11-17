package cl.ipss.reservas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion de reservas del restaurante Sabor Gourmet.
 * Spring Boot autoconfigura MVC, Thymeleaf, JPA y la seguridad segun las dependencias del pom.xml.
 */
@SpringBootApplication
public class SaborGourmetApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaborGourmetApplication.class, args);
    }
}
