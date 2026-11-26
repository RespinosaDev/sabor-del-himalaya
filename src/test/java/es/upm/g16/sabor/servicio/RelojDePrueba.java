package es.upm.g16.sabor.servicio;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Reloj controlable para probar el plazo de diez minutos de US11. */
class RelojDePrueba extends Clock {

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");

    private Instant instante;

    RelojDePrueba(LocalDateTime inicio) {
        fijar(inicio);
    }

    void fijar(LocalDateTime momento) {
        this.instante = momento.atZone(ZONA).toInstant();
    }

    void avanzar(Duration duracion) {
        this.instante = instante.plus(duracion);
    }

    @Override
    public ZoneId getZone() {
        return ZONA;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return instante;
    }
}
