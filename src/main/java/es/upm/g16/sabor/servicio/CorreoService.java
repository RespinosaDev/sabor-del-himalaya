package es.upm.g16.sabor.servicio;

import es.upm.g16.sabor.modelo.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envío del correo con el enlace de activación . Si no hay servidor de
 * correo configurado (spring.mail.host), el enlace se escribe en el log.
 */
@Service
public class CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoService.class);

    private final ObjectProvider<JavaMailSender> servidorCorreo;
    private final String urlBase;
    private final String remitente;

    public CorreoService(ObjectProvider<JavaMailSender> servidorCorreo,
                         @Value("${app.url-base:http://localhost:8080}") String urlBase,
                         @Value("${app.correo.remitente:no-reply@sabordelhimalaya.es}") String remitente) {
        this.servidorCorreo = servidorCorreo;
        this.urlBase = urlBase;
        this.remitente = remitente;
    }

    public String enlaceActivacion(Cliente cliente) {
        return urlBase + "/activar?token=" + cliente.getTokenActivacion();
    }

    public void enviarActivacion(Cliente cliente) {
        String enlace = enlaceActivacion(cliente);
        JavaMailSender servidor = servidorCorreo.getIfAvailable();
        if (servidor == null) {
            log.info("Correo de activación para {} <{}>: {}", cliente.getLogin(), cliente.getEmail(), enlace);
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(cliente.getEmail());
        mensaje.setSubject("Activa tu cuenta en Sabor del Himalaya");
        mensaje.setText("Hola " + cliente.getNombre() + ",\n\n"
                + "Gracias por registrarte. Para activar tu cuenta entra en este enlace:\n" + enlace + "\n\n"
                + "Sabor del Himalaya");
        try {
            servidor.send(mensaje);
        } catch (MailException e) {
            log.warn("No se pudo enviar el correo a {}: {}. Enlace: {}", cliente.getEmail(), e.getMessage(), enlace);
        }
    }
}
