package es.upm.g16.sabor.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Concepto de un pedido (US9): un producto de la carta o un menú diario, con su cantidad
 * y el precio aplicado en el momento de pedir (el precio del producto puede cambiar después).
 */
@Entity
public class LineaPedido {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Pedido pedido;

    @ManyToOne
    private Producto producto;

    @ManyToOne
    private MenuDiario menu;

    @Column(nullable = false, length = 120)
    private String concepto;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precioUnitario;

    protected LineaPedido() {
    }

    private LineaPedido(Producto producto, MenuDiario menu, String concepto, int cantidad, BigDecimal precioUnitario) {
        this.producto = producto;
        this.menu = menu;
        this.concepto = concepto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public static LineaPedido deProducto(Producto producto, int cantidad) {
        return new LineaPedido(producto, null, producto.getNombre(), cantidad, producto.getPrecio());
    }

    public static LineaPedido deMenu(MenuDiario menu, int cantidad) {
        return new LineaPedido(null, menu, "Menú del día " + menu.getFecha().format(FORMATO), cantidad, menu.getPrecio());
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Producto getProducto() {
        return producto;
    }

    public MenuDiario getMenu() {
        return menu;
    }

    public String getConcepto() {
        return concepto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    /** Precio unitario por cantidad. */
    public BigDecimal getImporte() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
