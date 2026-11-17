package cl.ipss.reservas;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Fija "hoy" en 1 de diciembre de 2026, 10:00, para que las pruebas no dependan de la fecha real. */
@TestConfiguration
public class RelojDePrueba {

    @Bean
    @Primary
    public Clock relojFijo() {
        return Clock.fixed(Instant.parse("2026-12-01T13:00:00Z"), ZoneId.of("America/Santiago"));
    }
}
