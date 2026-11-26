package es.upm.g16.sabor.servicio;

import es.upm.g16.sabor.modelo.Categoria;
import es.upm.g16.sabor.modelo.Producto;
import es.upm.g16.sabor.repositorio.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Gestión de la carta (US1): alta, modificación y retirada de productos.
 */
@Service
@Transactional
public class CartaService {

    static final int MAX_NOMBRE = 80;
    static final int MAX_DESCRIPCION = 300;

    private static final Comparator<Producto> ORDEN_CARTA =
            Comparator.comparing(Producto::getCategoria).thenComparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER);

    private final ProductoRepository productos;

    public CartaService(ProductoRepository productos) {
        this.productos = productos;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarCarta() {
        List<Producto> carta = new ArrayList<>(productos.findByRetiradoFalseAndEnCartaTrue());
        carta.sort(ORDEN_CARTA);
        return carta;
    }

    /** Carta agrupada por categoría, solo con las categorías que tienen productos (US8). */
    @Transactional(readOnly = true)
    public Map<Categoria, List<Producto>> listarCartaPorCategoria() {
        Map<Categoria, List<Producto>> agrupada = new EnumMap<>(Categoria.class);
        for (Producto producto : listarCarta()) {
            agrupada.computeIfAbsent(producto.getCategoria(), c -> new ArrayList<>()).add(producto);
        }
        return agrupada;
    }

    /** Productos disponibles de una categoría, de la carta o fuera de ella (para el menú). */
    @Transactional(readOnly = true)
    public List<Producto> listarPorCategoria(Categoria categoria) {
        List<Producto> lista = new ArrayList<>(productos.findByRetiradoFalseAndCategoria(categoria));
        lista.sort(ORDEN_CARTA);
        return lista;
    }

    @Transactional(readOnly = true)
    public Producto obtener(Long id) {
        return productos.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("El producto indicado no existe."));
    }

    public Producto alta(DatosProducto datos) {
        validar(datos);
        Producto producto = new Producto(datos.getNombre().trim(), limpiar(datos.getDescripcion()),
                datos.getCategoria(), datos.getPrecio(), datos.isEnCarta());
        return productos.save(producto);
    }

    public Producto modificar(Long id, DatosProducto datos) {
        Producto producto = obtener(id);
        if (producto.isRetirado()) {
            throw new ReglaNegocioException("No se puede modificar un producto retirado.");
        }
        validar(datos);
        producto.setNombre(datos.getNombre().trim());
        producto.setDescripcion(limpiar(datos.getDescripcion()));
        producto.setCategoria(datos.getCategoria());
        producto.setPrecio(datos.getPrecio());
        producto.setEnCarta(datos.isEnCarta());
        return productos.save(producto);
    }

    /** Retira el producto: deja de ofrecerse pero se conserva para los pedidos ya hechos. */
    public void retirar(Long id) {
        Producto producto = obtener(id);
        producto.setRetirado(true);
        productos.save(producto);
    }

    private void validar(DatosProducto datos) {
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new ReglaNegocioException("El nombre del producto es obligatorio.");
        }
        if (datos.getNombre().trim().length() > MAX_NOMBRE) {
            throw new ReglaNegocioException("El nombre no puede tener más de " + MAX_NOMBRE + " caracteres.");
        }
        if (datos.getDescripcion() != null && datos.getDescripcion().trim().length() > MAX_DESCRIPCION) {
            throw new ReglaNegocioException("La descripción no puede tener más de " + MAX_DESCRIPCION + " caracteres.");
        }
        if (datos.getCategoria() == null) {
            throw new ReglaNegocioException("Hay que elegir una categoría.");
        }
        if (datos.getPrecio() == null) {
            throw new ReglaNegocioException("El precio es obligatorio.");
        }
        if (datos.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El precio no puede ser negativo.");
        }
        if (datos.isEnCarta() && datos.getPrecio().compareTo(BigDecimal.ZERO) == 0) {
            throw new ReglaNegocioException("Un producto de la carta debe tener un precio mayor que 0.");
        }
    }

    private static String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
