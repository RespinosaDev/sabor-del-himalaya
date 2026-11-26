package es.upm.g16.sabor.web;

import es.upm.g16.sabor.servicio.MenuService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class InicioController {

    private final MenuService menus;

    public InicioController(MenuService menus) {
        this.menus = menus;
    }

    @GetMapping("/")
    public String inicio() {
        return "inicio";
    }

    /** Consulta pública del menú de una fecha (hoy por defecto): si no está configurado se indica (CA-US2.4, CA-US8.3). */
    @GetMapping("/menu-del-dia")
    public String menuDelDia(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                             Model model) {
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        model.addAttribute("fecha", dia);
        model.addAttribute("esHoy", dia.equals(LocalDate.now()));
        model.addAttribute("menu", menus.consultar(dia).filter(m -> m.isConfigurado()).orElse(null));
        return "menu/del-dia";
    }
}
