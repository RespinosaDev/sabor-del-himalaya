package es.upm.g16.sabor.web;

import es.upm.g16.sabor.servicio.CartaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Consulta pública de la carta por categorías (US8). El menú del día se consulta en InicioController.
 */
@Controller
public class ExplorarController {

    private final CartaService carta;

    public ExplorarController(CartaService carta) {
        this.carta = carta;
    }

    @GetMapping("/carta")
    public String carta(Model model) {
        model.addAttribute("porCategoria", carta.listarCartaPorCategoria());
        return "carta/consulta";
    }
}
