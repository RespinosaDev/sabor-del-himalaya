package es.upm.g16.sabor.web;

import es.upm.g16.sabor.modelo.LineaPedido;
import es.upm.g16.sabor.modelo.Pedido;
import es.upm.g16.sabor.servicio.CantidadesPedido;
import es.upm.g16.sabor.servicio.CartaService;
import es.upm.g16.sabor.servicio.MenuService;
import es.upm.g16.sabor.servicio.PedidoService;
import es.upm.g16.sabor.servicio.ReglaNegocioException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Pantallas del cliente con sesión: hacer un pedido (US9), ver su estado (US10) y modificarlo
 * o cancelarlo en los diez primeros minutos (US11).
 */
@Controller
@RequestMapping("/cliente/pedidos")
public class PedidoClienteController {

    private static final String LOGIN = "redirect:/login";

    private final PedidoService pedidos;
    private final CartaService carta;
    private final MenuService menus;

    public PedidoClienteController(PedidoService pedidos, CartaService carta, MenuService menus) {
        this.pedidos = pedidos;
        this.carta = carta;
        this.menus = menus;
    }

    @GetMapping
    public String lista(HttpSession sesion, Model model) {
        Long clienteId = clienteId(sesion);
        if (clienteId == null) {
            return LOGIN;
        }
        model.addAttribute("pedidos", pedidos.pedidosDelCliente(clienteId));
        return "pedido/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpSession sesion, Model model) {
        if (clienteId(sesion) == null) {
            return LOGIN;
        }
        prepararFormulario(model, new CantidadesPedido(), "/cliente/pedidos/revisar", "Revisar pedido");
        return "pedido/formulario";
    }

    /** Muestra los conceptos y el total antes de confirmar (CA-US9.2). */
    @PostMapping("/revisar")
    public String revisar(@RequestParam Map<String, String> parametros, HttpSession sesion, Model model) {
        if (clienteId(sesion) == null) {
            return LOGIN;
        }
        try {
            CantidadesPedido cantidades = CantidadesPedido.desde(parametros);
            List<LineaPedido> lineas = pedidos.previsualizar(cantidades);
            model.addAttribute("lineas", lineas);
            model.addAttribute("total", PedidoService.total(lineas));
            model.addAttribute("cantidades", cantidades);
            return "pedido/resumen";
        } catch (ReglaNegocioException e) {
            prepararFormulario(model, cantidadesParaRepintar(parametros), "/cliente/pedidos/revisar", "Revisar pedido");
            model.addAttribute("error", e.getMessage());
            return "pedido/formulario";
        }
    }

    @PostMapping("/confirmar")
    public String confirmar(@RequestParam Map<String, String> parametros, HttpSession sesion,
                            Model model, RedirectAttributes redirect) {
        Long clienteId = clienteId(sesion);
        if (clienteId == null) {
            return LOGIN;
        }
        try {
            Pedido pedido = pedidos.confirmar(clienteId, CantidadesPedido.desde(parametros));
            redirect.addFlashAttribute("mensaje", "¡Pedido aceptado! Tu número de pedido es " + pedido.getId() + ".");
            return "redirect:/cliente/pedidos/" + pedido.getId();
        } catch (ReglaNegocioException e) {
            prepararFormulario(model, cantidadesParaRepintar(parametros), "/cliente/pedidos/revisar", "Revisar pedido");
            model.addAttribute("error", e.getMessage());
            return "pedido/formulario";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, HttpSession sesion, Model model, RedirectAttributes redirect) {
        Long clienteId = clienteId(sesion);
        if (clienteId == null) {
            return LOGIN;
        }
        try {
            Pedido pedido = pedidos.obtenerDelCliente(clienteId, id);
            model.addAttribute("pedido", pedido);
            model.addAttribute("admiteCambios", pedidos.admiteCambios(pedido));
            return "pedido/detalle";
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/cliente/pedidos";
        }
    }

    @GetMapping("/{id}/modificar")
    public String modificar(@PathVariable Long id, HttpSession sesion, Model model, RedirectAttributes redirect) {
        Long clienteId = clienteId(sesion);
        if (clienteId == null) {
            return LOGIN;
        }
        try {
            Pedido pedido = pedidos.obtenerDelCliente(clienteId, id);
            if (!pedidos.admiteCambios(pedido)) {
                redirect.addFlashAttribute("error", "No se puede modificar el pedido " + id
                        + ": pasaron diez minutos desde la confirmación, el restaurante ya lo ha empezado a preparar o está cancelado.");
                return "redirect:/cliente/pedidos/" + id;
            }
            prepararFormulario(model, cantidadesDe(pedido), "/cliente/pedidos/" + id + "/modificar", "Guardar cambios");
            model.addAttribute("idPedido", id);
            return "pedido/formulario";
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/cliente/pedidos";
        }
    }

    @PostMapping("/{id}/modificar")
    public String guardarCambios(@PathVariable Long id, @RequestParam Map<String, String> parametros,
                                 HttpSession sesion, Model model, RedirectAttributes redirect) {
        Long clienteId = clienteId(sesion);
        if (clienteId == null) {
            return LOGIN;
        }
        try {
            pedidos.modificar(clienteId, id, CantidadesPedido.desde(parametros));
            redirect.addFlashAttribute("mensaje", "Pedido modificado: el importe se ha recalculado.");
            return "redirect:/cliente/pedidos/" + id;
        } catch (ReglaNegocioException e) {
            prepararFormulario(model, cantidadesParaRepintar(parametros), "/cliente/pedidos/" + id + "/modificar", "Guardar cambios");
            model.addAttribute("idPedido", id);
            model.addAttribute("error", e.getMessage());
            return "pedido/formulario";
        }
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, HttpSession sesion, RedirectAttributes redirect) {
        Long clienteId = clienteId(sesion);
        if (clienteId == null) {
            return LOGIN;
        }
        try {
            pedidos.cancelar(clienteId, id);
            redirect.addFlashAttribute("mensaje", "Pedido " + id + " cancelado.");
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cliente/pedidos/" + id;
    }

    private void prepararFormulario(Model model, CantidadesPedido cantidades, String accion, String boton) {
        LocalDate hoy = LocalDate.now();
        model.addAttribute("porCategoria", carta.listarCartaPorCategoria());
        model.addAttribute("hayMenu", menus.consultar(hoy).filter(m -> m.isConfigurado()).isPresent());
        model.addAttribute("cantidades", cantidades);
        model.addAttribute("accion", accion);
        model.addAttribute("boton", boton);
    }

    /**
     * Cantidades con las que se vuelve a pintar el formulario tras un error. Si el propio formulario
     * no se puede leer (texto en vez de número, campo mal formado), se pinta vacío en lugar de
     * volver a lanzar la excepción, que acababa en un error 500.
     */
    private static CantidadesPedido cantidadesParaRepintar(Map<String, String> parametros) {
        try {
            return CantidadesPedido.desde(parametros);
        } catch (ReglaNegocioException e) {
            return new CantidadesPedido();
        }
    }

    private static CantidadesPedido cantidadesDe(Pedido pedido) {
        Map<Long, Integer> productos = new java.util.LinkedHashMap<>();
        int menus = 0;
        for (LineaPedido linea : pedido.getLineas()) {
            if (linea.getProducto() != null) {
                productos.put(linea.getProducto().getId(), linea.getCantidad());
            } else {
                menus += linea.getCantidad();
            }
        }
        return new CantidadesPedido(productos, menus);
    }

    private static Long clienteId(HttpSession sesion) {
        Object id = sesion.getAttribute(ClienteController.SESION_ID);
        return id instanceof Long ? (Long) id : null;
    }
}
