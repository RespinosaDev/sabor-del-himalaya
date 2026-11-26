package es.upm.g16.sabor.servicio;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Cálculo del hash de las contraseñas (SHA-256 con sal aleatoria por cliente).
 */
@Component
public class CifradoContrasenas {

    private final SecureRandom aleatorio = new SecureRandom();

    public String generarSal() {
        byte[] sal = new byte[16];
        aleatorio.nextBytes(sal);
        return HexFormat.of().formatHex(sal);
    }

    public String hash(String contrasena, String sal) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(sal.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(sha.digest(contrasena.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    public boolean coincide(String contrasena, String sal, String hashGuardado) {
        byte[] calculado = hash(contrasena, sal).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(calculado, hashGuardado.getBytes(StandardCharsets.UTF_8));
    }
}
