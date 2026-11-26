package es.upm.g16.sabor.web;

import es.upm.g16.sabor.modelo.Categoria;
import es.upm.g16.sabor.servicio.CartaService;
import es.upm.g16.sabor.servicio.DatosProducto;
import es.upm.g16.sabor.servicio.ReglaNegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pantallas del personal para gestionar la carta (US1).
 */
@Controller
@RequestMapping("/personal/carta")
public class CartaController {

    private final CartaService carta;

    public CartaController(CartaService carta) {
        this.carta = carta;
    }

    @ModelAttribute("categorias")
    public Categoria[] categorias() {
        return Categoria.values();
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("productos", carta.listarCarta());
        return "carta/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("producto", new DatosProducto());
        model.addAttribute("idProducto", null);
        return "carta/formulario";
    }

    @PostMapping("/nuevo")
    public String guardarNuevo(@ModelAttribute("producto") DatosProducto datos, Model model, RedirectAttributes redirect) {
        try {
            carta.alta(datos);
            redirect.addFlashAttribute("mensaje", "¡Producto añadido a la carta!");
            return "redirect:/personal/carta";
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("idProducto", null);
            return "carta/formulario";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("producto", DatosProducto.desde(carta.obtener(id)));
            model.addAttribute("idProducto", id);
            return "carta/formulario";
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/personal/carta";
        }
    }

    @PostMapping("/{id}/editar")
    public String guardarCambios(@PathVariable Long id, @ModelAttribute("producto") DatosProducto datos,
                                 Model model, RedirectAttributes redirect) {
        try {
            carta.modificar(id, datos);
            redirect.addFlashAttribute("mensaje", "¡Producto modificado!");
            return "redirect:/personal/carta";
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("idProducto", id);
            return "carta/formulario";
        }
    }

    @PostMapping("/{id}/retirar")
    public String retirar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            carta.retirar(id);
            redirect.addFlashAttribute("mensaje", "¡Producto retirado de la carta!");
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/personal/carta";
    }
}
