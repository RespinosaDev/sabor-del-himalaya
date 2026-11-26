package es.upm.g16.sabor.servicio;

import es.upm.g16.sabor.modelo.Cliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas unitarias de US5, US6 y US7 (CPU-13 a CPU-21). */
@SpringBootTest
@Transactional
class ClienteServiceTest {

    @Autowired
    private ClienteService clientes;

    private DatosRegistro datos(String login) {
        return new DatosRegistro("Ana Pérez", "C/ Alcalá 10, Madrid", "ana@example.com", login, "secreto1", "secreto1");
    }

    @Test
    @DisplayName("CPU-13 Registro válido: cuenta creada sin activar y con enlace de activación")
    void registroValido() {
        Cliente c = clientes.registrar(datos("ana"));
        assertNotNull(c.getId());
        assertFalse(c.isActivo());
        assertNotNull(c.getTokenActivacion());
        assertNotEquals("secreto1", c.getHashContrasena());
    }

    @Test
    @DisplayName("CPU-14 Registro con login ya usado: se rechaza")
    void loginRepetido() {
        clientes.registrar(datos("ana"));
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> clientes.registrar(datos("ANA")));
        assertTrue(e.getMessage().contains("ya está registrado"));
    }

    @Test
    @DisplayName("CPU-15 Registro con correo mal escrito o contraseñas distintas: se rechaza")
    void datosRegistroNoValidos() {
        DatosRegistro correoMalo = datos("luis");
        correoMalo.setEmail("luis.example.com");
        assertThrows(ReglaNegocioException.class, () -> clientes.registrar(correoMalo));
        DatosRegistro distintas = datos("luis");
        distintas.setConfirmacion("otra-cosa");
        assertThrows(ReglaNegocioException.class, () -> clientes.registrar(distintas));
    }

    @Test
    @DisplayName("CPU-16 Activar con enlace válido: la cuenta pasa a activa y el enlace deja de servir")
    void activarValido() {
        Cliente c = clientes.registrar(datos("ana"));
        String token = c.getTokenActivacion();
        Cliente activado = clientes.activar(token);
        assertTrue(activado.isActivo());
        assertNull(activado.getTokenActivacion());
        assertThrows(ReglaNegocioException.class, () -> clientes.activar(token));
    }

    @Test
    @DisplayName("CPU-17 Activar con enlace inválido: mensaje explicativo")
    void activarInvalido() {
        assertThrows(ReglaNegocioException.class, () -> clientes.activar("no-existe"));
        assertThrows(ReglaNegocioException.class, () -> clientes.activar(""));
    }

    @Test
    @DisplayName("CPU-18 Login correcto con cuenta activa")
    void loginCorrecto() {
        Cliente c = clientes.registrar(datos("ana"));
        clientes.activar(c.getTokenActivacion());
        assertEquals(c.getId(), clientes.autenticar("ana", "secreto1").getId());
    }

    @Test
    @DisplayName("CPU-19 Login con cuenta sin activar: no se permite")
    void loginSinActivar() {
        clientes.registrar(datos("ana"));
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> clientes.autenticar("ana", "secreto1"));
        assertTrue(e.getMessage().contains("no está activada"));
    }

    @Test
    @DisplayName("CPU-20 Login con contraseña incorrecta o usuario inexistente: error")
    void loginIncorrecto() {
        Cliente c = clientes.registrar(datos("ana"));
        clientes.activar(c.getTokenActivacion());
        assertThrows(ReglaNegocioException.class, () -> clientes.autenticar("ana", "mala"));
        assertThrows(ReglaNegocioException.class, () -> clientes.autenticar("nadie", "secreto1"));
    }
}
