package es.upm.g16.sabor.servicio;

import es.upm.g16.sabor.modelo.Cliente;
import es.upm.g16.sabor.repositorio.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Registro (US5), activación de cuenta (US6) e inicio de sesión (US7) de clientes.
 */
@Service
@Transactional
public class ClienteService {

    static final int MIN_CONTRASENA = 6;
    private static final Pattern FORMATO_LOGIN = Pattern.compile("[A-Za-z0-9._-]{3,40}");
    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final ClienteRepository clientes;
    private final CifradoContrasenas cifrado;
    private final CorreoService correo;

    public ClienteService(ClienteRepository clientes, CifradoContrasenas cifrado, CorreoService correo) {
        this.clientes = clientes;
        this.cifrado = cifrado;
        this.correo = correo;
    }

    public Cliente registrar(DatosRegistro datos) {
        String nombre = obligatorio(datos.getNombre(), "el nombre");
        String direccion = obligatorio(datos.getDireccion(), "la dirección de entrega");
        String email = obligatorio(datos.getEmail(), "el correo electrónico");
        String login = obligatorio(datos.getLogin(), "el login");
        String contrasena = datos.getContrasena() == null ? "" : datos.getContrasena();

        if (!FORMATO_EMAIL.matcher(email).matches()) {
            throw new ReglaNegocioException("El correo electrónico no tiene un formato válido.");
        }
        if (!FORMATO_LOGIN.matcher(login).matches()) {
            throw new ReglaNegocioException("El login debe tener entre 3 y 40 caracteres (letras, números, punto, guion o guion bajo).");
        }
        if (contrasena.length() < MIN_CONTRASENA) {
            throw new ReglaNegocioException("La contraseña debe tener al menos " + MIN_CONTRASENA + " caracteres.");
        }
        if (!contrasena.equals(datos.getConfirmacion())) {
            throw new ReglaNegocioException("Las dos contraseñas no coinciden.");
        }
        if (clientes.existsByLoginIgnoreCase(login)) {
            throw new ReglaNegocioException("El login \"" + login + "\" ya está registrado. Elige otro.");
        }

        String sal = cifrado.generarSal();
        String token = UUID.randomUUID().toString().replace("-", "");
        Cliente cliente = new Cliente(nombre, direccion, login, email, cifrado.hash(contrasena, sal), sal, token);
        cliente = clientes.save(cliente);
        correo.enviarActivacion(cliente);
        return cliente;
    }

    public Cliente activar(String token) {
        if (token == null || token.isBlank()) {
            throw new ReglaNegocioException("El enlace de activación no es válido.");
        }
        Cliente cliente = clientes.findByTokenActivacion(token.trim())
                .orElseThrow(() -> new ReglaNegocioException(
                        "El enlace de activación no es válido o la cuenta ya está activada."));
        cliente.activar();
        return clientes.save(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente autenticar(String login, String contrasena) {
        if (login == null || login.isBlank() || contrasena == null || contrasena.isEmpty()) {
            throw new ReglaNegocioException("Introduce el login y la contraseña.");
        }
        Cliente cliente = clientes.findByLoginIgnoreCase(login.trim())
                .filter(c -> cifrado.coincide(contrasena, c.getSal(), c.getHashContrasena()))
                .orElseThrow(() -> new ReglaNegocioException("Login o contraseña incorrectos."));
        if (!cliente.isActivo()) {
            throw new ReglaNegocioException("La cuenta todavía no está activada. Revisa tu correo y usa el enlace de activación.");
        }
        return cliente;
    }

    private static String obligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("Falta " + campo + ".");
        }
        return valor.trim();
    }
}
