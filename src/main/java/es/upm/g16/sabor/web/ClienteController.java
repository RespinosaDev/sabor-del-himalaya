package es.upm.g16.sabor.web;

import es.upm.g16.sabor.modelo.Cliente;
import es.upm.g16.sabor.servicio.ClienteService;
import es.upm.g16.sabor.servicio.CorreoService;
import es.upm.g16.sabor.servicio.DatosRegistro;
import es.upm.g16.sabor.servicio.ReglaNegocioException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Registro (US5), activación (US6) e inicio de sesión (US7) de clientes.
 */
@Controller
public class ClienteController {

    static final String SESION_ID = "clienteId";
    static final String SESION_NOMBRE = "clienteNombre";

    private final ClienteService clientes;
    private final CorreoService correo;
    private final boolean mostrarEnlace;

    public ClienteController(ClienteService clientes, CorreoService correo,
                             @Value("${app.mostrar-enlace-activacion:false}") boolean mostrarEnlace) {
        this.clientes = clientes;
        this.correo = correo;
        this.mostrarEnlace = mostrarEnlace;
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("datos", new DatosRegistro());
        return "cliente/registro";
    }

    @PostMapping("/registro")
    public String registrar(@ModelAttribute("datos") DatosRegistro datos, Model model) {
        try {
            Cliente cliente = clientes.registrar(datos);
            model.addAttribute("cliente", cliente);
            if (mostrarEnlace) {
                model.addAttribute("enlace", correo.enlaceActivacion(cliente));
            }
            return "cliente/registro-ok";
        } catch (ReglaNegocioException e) {
            datos.setContrasena(null);
            datos.setConfirmacion(null);
            model.addAttribute("error", e.getMessage());
            return "cliente/registro";
        }
    }

    @GetMapping("/activar")
    public String activar(@RequestParam(required = false) String token, Model model) {
        try {
            Cliente cliente = clientes.activar(token);
            model.addAttribute("activada", true);
            model.addAttribute("nombre", cliente.getNombre());
        } catch (ReglaNegocioException e) {
            model.addAttribute("activada", false);
            model.addAttribute("error", e.getMessage());
        }
        return "cliente/activacion";
    }

    @GetMapping("/login")
    public String login() {
        return "cliente/login";
    }

    @PostMapping("/login")
    public String entrar(@RequestParam(required = false) String login, @RequestParam(required = false) String contrasena,
                         HttpSession sesion, Model model) {
        try {
            Cliente cliente = clientes.autenticar(login, contrasena);
            sesion.setAttribute(SESION_ID, cliente.getId());
            sesion.setAttribute(SESION_NOMBRE, cliente.getNombre());
            return "redirect:/cliente";
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("login", login);
            return "cliente/login";
        }
    }

    @GetMapping("/cliente")
    public String zonaCliente(HttpSession sesion, Model model) {
        if (sesion.getAttribute(SESION_ID) == null) {
            return "redirect:/login";
        }
        model.addAttribute("nombre", sesion.getAttribute(SESION_NOMBRE));
        return "cliente/inicio";
    }

    @PostMapping("/logout")
    public String salir(HttpSession sesion) {
        sesion.invalidate();
        return "redirect:/";
    }
}
