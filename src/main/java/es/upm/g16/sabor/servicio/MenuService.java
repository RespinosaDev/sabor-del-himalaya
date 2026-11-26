package es.upm.g16.sabor.servicio;

import es.upm.g16.sabor.modelo.Categoria;
import es.upm.g16.sabor.modelo.MenuDiario;
import es.upm.g16.sabor.modelo.Producto;
import es.upm.g16.sabor.repositorio.MenuDiarioRepository;
import es.upm.g16.sabor.repositorio.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Elaboración del menú diario (US2).
 */
@Service
@Transactional
public class MenuService {

    public static final int NUM_ENTRANTES = 3;
    public static final int NUM_PRINCIPALES = 3;
    public static final int NUM_POSTRES = 2;

    private final MenuDiarioRepository menus;
    private final ProductoRepository productos;
    private final CartaService carta;

    public MenuService(MenuDiarioRepository menus, ProductoRepository productos, CartaService carta) {
        this.menus = menus;
        this.productos = productos;
        this.carta = carta;
    }

    @Transactional(readOnly = true)
    public Optional<MenuDiario> consultar(LocalDate fecha) {
        return menus.findByFecha(fecha);
    }

    /** Guarda el menú de una fecha con los platos elegidos. */
    public MenuDiario configurar(LocalDate fecha, List<Long> entrantes, List<Long> principales,
                                 List<Long> postres, Long bebida) {
        if (fecha == null) {
            throw new ReglaNegocioException("Hay que indicar la fecha del menú.");
        }
        List<Producto> e = elegir(entrantes, Categoria.ENTRANTE, NUM_ENTRANTES, "entrantes");
        List<Producto> p = elegir(principales, Categoria.PRINCIPAL, NUM_PRINCIPALES, "platos principales");
        List<Producto> d = elegir(postres, Categoria.POSTRE, NUM_POSTRES, "postres");
        if (bebida == null) {
            throw new ReglaNegocioException("Hay que elegir la bebida del menú.");
        }
        Producto b = disponible(bebida, Categoria.BEBIDA);

        MenuDiario menu = menus.findByFecha(fecha).orElseGet(() -> new MenuDiario(fecha));
        menu.asignarPlatos(e, p, d, b);
        menu.setConfigurado(true);
        return menus.save(menu);
    }

    /** Registra que ese día no hay menú. */
    public MenuDiario marcarSinConfigurar(LocalDate fecha) {
        if (fecha == null) {
            throw new ReglaNegocioException("Hay que indicar la fecha del menú.");
        }
        MenuDiario menu = menus.findByFecha(fecha).orElseGet(() -> new MenuDiario(fecha));
        menu.vaciar();
        return menus.save(menu);
    }

    /** Crea un plato preparado fuera de la carta que queda guardado para otros menús. */
    public Producto crearPlatoFueraDeCarta(String nombre, String descripcion, Categoria categoria) {
        if (categoria == Categoria.BEBIDA) {
            throw new ReglaNegocioException("Los platos fuera de carta deben ser entrantes, principales o postres.");
        }
        return carta.alta(new DatosProducto(nombre, descripcion, categoria, BigDecimal.ZERO, false));
    }

    private List<Producto> elegir(List<Long> ids, Categoria categoria, int numero, String texto) {
        List<Long> lista = ids == null ? new ArrayList<>() : ids;
        if (lista.size() != numero) {
            throw new ReglaNegocioException("Hay que elegir exactamente " + numero + " " + texto + ".");
        }
        if (new HashSet<>(lista).size() != lista.size()) {
            throw new ReglaNegocioException("No se puede repetir un plato en los " + texto + ".");
        }
        List<Producto> elegidos = new ArrayList<>();
        for (Long id : lista) {
            elegidos.add(disponible(id, categoria));
        }
        return elegidos;
    }

    private Producto disponible(Long id, Categoria categoria) {
        Producto producto = productos.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("Uno de los platos elegidos no existe."));
        if (producto.isRetirado()) {
            throw new ReglaNegocioException("El plato \"" + producto.getNombre() + "\" está retirado.");
        }
        if (producto.getCategoria() != categoria) {
            throw new ReglaNegocioException("\"" + producto.getNombre() + "\" no es de la categoría "
                    + categoria.getEtiqueta().toLowerCase() + ".");
        }
        return producto;
    }
}
