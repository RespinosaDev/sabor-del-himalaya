package es.upm.g16.sabor.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Añade a todas las vistas el nombre del cliente con sesión iniciada (para la cabecera).
 */
@ControllerAdvice
public class DatosComunes {

    @ModelAttribute("clienteNombre")
    public Object clienteNombre(HttpServletRequest peticion) {
        HttpSession sesion = peticion.getSession(false);
        return sesion == null ? null : sesion.getAttribute(ClienteController.SESION_NOMBRE);
    }
}
