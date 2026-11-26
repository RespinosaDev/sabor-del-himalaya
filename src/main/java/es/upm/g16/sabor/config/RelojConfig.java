package es.upm.g16.sabor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Reloj de la aplicación. Se inyecta (en vez de llamar a LocalDateTime.now()) para poder
 * comprobar en las pruebas el plazo de diez minutos de US11.
 */
@Configuration
public class RelojConfig {

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }
}
