package es.upm.g16.sabor.servicio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas unitarias del cálculo de hash de contraseñas (CPU-21). */
class CifradoContrasenasTest {

    private final CifradoContrasenas cifrado = new CifradoContrasenas();

    @Test
    @DisplayName("CPU-21 El hash depende de la contraseña y de la sal")
    void hashConSal() {
        String sal = cifrado.generarSal();
        String hash = cifrado.hash("secreto1", sal);
        assertEquals(64, hash.length());
        assertTrue(cifrado.coincide("secreto1", sal, hash));
        assertFalse(cifrado.coincide("secreto2", sal, hash));
        assertNotEquals(hash, cifrado.hash("secreto1", cifrado.generarSal()));
    }
}
