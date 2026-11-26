package es.upm.g16.sabor.web;

import es.upm.g16.sabor.modelo.Categoria;
import es.upm.g16.sabor.modelo.MenuDiario;
import es.upm.g16.sabor.modelo.Producto;
import es.upm.g16.sabor.servicio.CartaService;
import es.upm.g16.sabor.servicio.MenuService;
import es.upm.g16.sabor.servicio.ReglaNegocioException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Pantallas del chef para configurar el menú diario (US2).
 */
@Controller
@RequestMapping("/personal/menu")
public class MenuController {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MenuService menus;
    private final CartaService carta;

    public MenuController(MenuService menus, CartaService carta) {
        this.menus = menus;
        this.carta = carta;
    }

    @GetMapping
    public String formulario(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                             Model model) {
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        MenuDiario menu = menus.consultar(dia).orElse(null);
        prepararFormulario(model, dia, menu);
        return "menu/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                          @RequestParam(required = false) List<Long> entrantes,
                          @RequestParam(required = false) List<Long> principales,
                          @RequestParam(required = false) List<Long> postres,
                          @RequestParam(required = false) Long bebida,
                          Model model, RedirectAttributes redirect) {
        try {
            menus.configurar(fecha, entrantes, principales, postres, bebida);
            redirect.addFlashAttribute("mensaje", "¡Menú del " + fecha.format(FORMATO) + " guardado!");
            return "redirect:/personal/menu?fecha=" + fecha;
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            prepararFormulario(model, fecha, null);
            model.addAttribute("selEntrantes", entrantes == null ? new ArrayList<Long>() : entrantes);
            model.addAttribute("selPrincipales", principales == null ? new ArrayList<Long>() : principales);
            model.addAttribute("selPostres", postres == null ? new ArrayList<Long>() : postres);
            model.addAttribute("selBebida", bebida);
            return "menu/formulario";
        }
    }

    @PostMapping("/sin-configurar")
    public String sinConfigurar(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                RedirectAttributes redirect) {
        menus.marcarSinConfigurar(fecha);
        redirect.addFlashAttribute("mensaje", "El " + fecha.format(FORMATO) + " queda sin menú configurado.");
        return "redirect:/personal/menu?fecha=" + fecha;
    }

    @PostMapping("/plato")
    public String nuevoPlato(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                             @RequestParam(required = false) String nombre,
                             @RequestParam(required = false) String descripcion,
                             @RequestParam(required = false) Categoria categoria,
                             RedirectAttributes redirect) {
        try {
            if (categoria == null) {
                throw new ReglaNegocioException("Hay que elegir la categoría del plato.");
            }
            Producto plato = menus.crearPlatoFueraDeCarta(nombre, descripcion, categoria);
            redirect.addFlashAttribute("mensaje", "Plato \"" + plato.getNombre() + "\" guardado para los menús.");
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/personal/menu?fecha=" + fecha;
    }

    private void prepararFormulario(Model model, LocalDate fecha, MenuDiario menu) {
        model.addAttribute("fecha", fecha);
        model.addAttribute("fechaTexto", fecha.format(FORMATO));
        model.addAttribute("menu", menu);
        model.addAttribute("precioMenu", MenuDiario.PRECIO_MENU);
        model.addAttribute("opcionesEntrantes", carta.listarPorCategoria(Categoria.ENTRANTE));
        model.addAttribute("opcionesPrincipales", carta.listarPorCategoria(Categoria.PRINCIPAL));
        model.addAttribute("opcionesPostres", carta.listarPorCategoria(Categoria.POSTRE));
        model.addAttribute("opcionesBebidas", carta.listarPorCategoria(Categoria.BEBIDA));
        model.addAttribute("categoriasPlato", new Categoria[]{Categoria.ENTRANTE, Categoria.PRINCIPAL, Categoria.POSTRE});
        boolean conMenu = menu != null && menu.isConfigurado();
        model.addAttribute("selEntrantes", conMenu ? ids(menu.getEntrantes()) : new ArrayList<Long>());
        model.addAttribute("selPrincipales", conMenu ? ids(menu.getPrincipales()) : new ArrayList<Long>());
        model.addAttribute("selPostres", conMenu ? ids(menu.getPostres()) : new ArrayList<Long>());
        model.addAttribute("selBebida", conMenu && menu.getBebida() != null ? menu.getBebida().getId() : null);
    }

    private static List<Long> ids(List<Producto> productos) {
        List<Long> ids = new ArrayList<>();
        for (Producto p : productos) {
            ids.add(p.getId());
        }
        return ids;
    }
}
