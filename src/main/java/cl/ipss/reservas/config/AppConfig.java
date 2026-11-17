package cl.ipss.reservas.config;

import cl.ipss.reservas.model.Mesa;
import cl.ipss.reservas.repository.MesaRepository;
import java.time.Clock;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Beans generales: el reloj del sistema y las mesas iniciales del restaurante. */
@Configuration
public class AppConfig {

    /** Reloj inyectable: en las pruebas se reemplaza por uno fijo para no depender de la fecha real. */
    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }

    /** Carga ocho mesas de ejemplo cuando la base esta vacia (solo en desarrollo, no en pruebas). */
    @Bean
    @Profile("dev")
    public CommandLineRunner mesasIniciales(MesaRepository mesas) {
        return args -> {
            if (mesas.count() == 0) {
                int[] capacidades = {2, 2, 2, 4, 4, 4, 6, 8};
                for (int i = 0; i < capacidades.length; i++) {
                    mesas.save(new Mesa(i + 1, capacidades[i]));
                }
            }
        };
    }
}
