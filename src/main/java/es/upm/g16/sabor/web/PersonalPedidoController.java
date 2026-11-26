package es.upm.g16.sabor.web;

import es.upm.g16.sabor.modelo.EstadoPedido;
import es.upm.g16.sabor.servicio.FacturaDia;
import es.upm.g16.sabor.servicio.PedidoService;
import es.upm.g16.sabor.servicio.ReglaNegocioException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Pantallas del personal: actualizar el estado de los pedidos (US3) y consultar la facturación diaria (US4).
 */
@Controller
@RequestMapping("/personal")
public class PersonalPedidoController {

    private final PedidoService pedidos;

    public PersonalPedidoController(PedidoService pedidos) {
        this.pedidos = pedidos;
    }

    @ModelAttribute("estados")
    public EstadoPedido[] estados() {
        return new EstadoPedido[]{EstadoPedido.RECIBIDO, EstadoPedido.EN_PREPARACION,
                EstadoPedido.EN_REPARTO, EstadoPedido.ENTREGADO};
    }

    @GetMapping("/pedidos")
    public String lista(Model model) {
        model.addAttribute("pedidos", pedidos.todos());
        return "personal/pedidos";
    }

    @PostMapping("/pedidos/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam(required = false) EstadoPedido estado,
                                RedirectAttributes redirect) {
        try {
            pedidos.cambiarEstado(id, estado);
            redirect.addFlashAttribute("mensaje", "Pedido " + id + " actualizado.");
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/personal/pedidos";
    }

    @GetMapping("/facturacion")
    public String facturacion(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                              Model model) {
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        FacturaDia factura = pedidos.facturacion(dia);
        model.addAttribute("fecha", dia);
        model.addAttribute("factura", factura);
        return "personal/facturacion";
    }
}
