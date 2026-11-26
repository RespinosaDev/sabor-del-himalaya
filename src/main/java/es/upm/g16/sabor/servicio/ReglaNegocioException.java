package es.upm.g16.sabor.servicio;

/**
 * Error de validación o de regla de negocio. El mensaje se muestra al usuario.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
