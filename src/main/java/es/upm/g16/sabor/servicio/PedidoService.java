package es.upm.g16.sabor.servicio;

import es.upm.g16.sabor.modelo.Cliente;
import es.upm.g16.sabor.modelo.EstadoPedido;
import es.upm.g16.sabor.modelo.LineaPedido;
import es.upm.g16.sabor.modelo.MenuDiario;
import es.upm.g16.sabor.modelo.Pedido;
import es.upm.g16.sabor.modelo.Producto;
import es.upm.g16.sabor.repositorio.ClienteRepository;
import es.upm.g16.sabor.repositorio.MenuDiarioRepository;
import es.upm.g16.sabor.repositorio.PedidoRepository;
import es.upm.g16.sabor.repositorio.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pedidos a domicilio: realizar (US9), consultar el estado (US10), modificar o cancelar en los
 * diez primeros minutos (US11), actualizar el estado desde el restaurante (US3) y facturación diaria (US4).
 */
@Service
@Transactional
public class PedidoService {

    /** Plazo para modificar o cancelar, medido desde la confirmación (CA-US11.4). */
    public static final Duration PLAZO_CAMBIOS = Duration.ofMinutes(10);

    private final PedidoRepository pedidos;
    private final ClienteRepository clientes;
    private final ProductoRepository productos;
    private final MenuDiarioRepository menus;
    private final Clock reloj;

    public PedidoService(PedidoRepository pedidos, ClienteRepository clientes, ProductoRepository productos,
                         MenuDiarioRepository menus, Clock reloj) {
        this.pedidos = pedidos;
        this.clientes = clientes;
        this.productos = productos;
        this.menus = menus;
        this.reloj = reloj;
    }

    /** Conceptos e importe que se muestran antes de confirmar (CA-US9.2). No guarda nada. */
    @Transactional(readOnly = true)
    public List<LineaPedido> previsualizar(CantidadesPedido cantidades) {
        return construirLineas(cantidades, LocalDate.now(reloj));
    }

    /** Suma de los importes de una lista de conceptos. */
    public static BigDecimal total(List<LineaPedido> lineas) {
        BigDecimal total = BigDecimal.ZERO;
        for (LineaPedido linea : lineas) {
            total = total.add(linea.getImporte());
        }
        return total;
    }

    /** Confirma un pedido nuevo en estado Recibido (CA-US9.3). */
    public Pedido confirmar(Long clienteId, CantidadesPedido cantidades) {
        Cliente cliente = cliente(clienteId);
        LocalDateTime ahora = LocalDateTime.now(reloj);
        List<LineaPedido> lineas = construirLineas(cantidades, ahora.toLocalDate());
        Pedido pedido = new Pedido(cliente, cliente.getDireccion(), ahora);
        pedido.reemplazarLineas(lineas);
        return pedidos.save(pedido);
    }

    /** Pedidos del cliente, del más reciente al más antiguo (US10). */
    @Transactional(readOnly = true)
    public List<Pedido> pedidosDelCliente(Long clienteId) {
        return pedidos.findByClienteIdOrderByFechaHoraDesc(clienteId);
    }

    /** Un pedido del cliente. Si no existe o es de otro cliente, se responde igual (CA-US10.2). */
    @Transactional(readOnly = true)
    public Pedido obtenerDelCliente(Long clienteId, Long pedidoId) {
        return propio(clienteId, pedidoId);
    }

    /** Indica si todavía se puede modificar o cancelar el pedido. */
    @Transactional(readOnly = true)
    public boolean admiteCambios(Pedido pedido) {
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            return false;
        }
        Duration transcurrido = Duration.between(pedido.getFechaHora(), LocalDateTime.now(reloj));
        return transcurrido.compareTo(PLAZO_CAMBIOS) <= 0;
    }

    /** Cambia el contenido de un pedido propio y recalcula el importe (CA-US11.1). */
    public Pedido modificar(Long clienteId, Long pedidoId, CantidadesPedido cantidades) {
        Pedido pedido = propio(clienteId, pedidoId);
        comprobarPlazo(pedido, "modificar");
        pedido.reemplazarLineas(construirLineas(cantidades, pedido.getFechaHora().toLocalDate()));
        return pedidos.save(pedido);
    }

    /** Cancela un pedido propio dentro del plazo (CA-US11.2). */
    public Pedido cancelar(Long clienteId, Long pedidoId) {
        Pedido pedido = propio(clienteId, pedidoId);
        comprobarPlazo(pedido, "cancelar");
        pedido.setEstado(EstadoPedido.CANCELADO);
        return pedidos.save(pedido);
    }

    /** Todos los pedidos, para el personal del restaurante. */
    @Transactional(readOnly = true)
    public List<Pedido> todos() {
        return pedidos.findAllByOrderByFechaHoraDesc();
    }

    @Transactional(readOnly = true)
    public Pedido obtener(Long pedidoId) {
        if (pedidoId == null) {
            throw new ReglaNegocioException("Hay que indicar el número de pedido.");
        }
        return pedidos.findById(pedidoId)
                .orElseThrow(() -> new ReglaNegocioException("No existe ningún pedido con el número " + pedidoId + "."));
    }

    /** El personal cambia el estado de un pedido; los demás pedidos no se tocan (CA-US3.2, CA-US3.3). */
    public Pedido cambiarEstado(Long pedidoId, EstadoPedido nuevo) {
        Pedido pedido = obtener(pedidoId);
        if (nuevo == null) {
            throw new ReglaNegocioException("Hay que elegir el nuevo estado.");
        }
        if (nuevo == EstadoPedido.CANCELADO) {
            throw new ReglaNegocioException("Un pedido solo lo cancela el cliente dentro de los diez primeros minutos.");
        }
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new ReglaNegocioException("El pedido " + pedidoId + " está cancelado y no puede cambiar de estado.");
        }
        pedido.setEstado(nuevo);
        return pedidos.save(pedido);
    }

    /** Importe total de los pedidos a domicilio de una fecha; los cancelados no cuentan (US4). */
    @Transactional(readOnly = true)
    public FacturaDia facturacion(LocalDate fecha) {
        if (fecha == null) {
            throw new ReglaNegocioException("Hay que indicar la fecha.");
        }
        List<Pedido> delDia = pedidos.findByFechaHoraGreaterThanEqualAndFechaHoraLessThan(
                fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
        BigDecimal total = BigDecimal.ZERO;
        int contados = 0;
        for (Pedido pedido : delDia) {
            if (pedido.getEstado() != EstadoPedido.CANCELADO) {
                total = total.add(pedido.getImporteTotal());
                contados++;
            }
        }
        return new FacturaDia(fecha, contados, total);
    }

    private Cliente cliente(Long clienteId) {
        if (clienteId == null) {
            throw new ReglaNegocioException("Hay que iniciar sesión para hacer un pedido.");
        }
        return clientes.findById(clienteId)
                .orElseThrow(() -> new ReglaNegocioException("El cliente indicado no existe."));
    }

    private Pedido propio(Long clienteId, Long pedidoId) {
        return pedidos.findById(pedidoId)
                .filter(p -> p.getCliente().getId().equals(clienteId))
                .orElseThrow(() -> new ReglaNegocioException("El pedido indicado no existe."));
    }

    private void comprobarPlazo(Pedido pedido, String accion) {
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new ReglaNegocioException("El pedido " + pedido.getId() + " ya está cancelado.");
        }
        if (!admiteCambios(pedido)) {
            throw new ReglaNegocioException("No se puede " + accion + " el pedido " + pedido.getId()
                    + ": han pasado más de diez minutos desde que se confirmó.");
        }
    }

    private List<LineaPedido> construirLineas(CantidadesPedido cantidades, LocalDate fechaMenu) {
        List<LineaPedido> lineas = new ArrayList<>();
        for (Map.Entry<Long, Integer> elegido : cantidades.getProductos().entrySet()) {
            int cantidad = elegido.getValue();
            comprobarCantidad(cantidad);
            Producto producto = productos.findById(elegido.getKey())
                    .orElseThrow(() -> new ReglaNegocioException("Uno de los productos elegidos no existe."));
            if (producto.isRetirado() || !producto.isEnCarta()) {
                throw new ReglaNegocioException("\"" + producto.getNombre() + "\" ya no está disponible en la carta.");
            }
            lineas.add(LineaPedido.deProducto(producto, cantidad));
        }
        if (cantidades.getMenus() != 0) {
            comprobarCantidad(cantidades.getMenus());
            MenuDiario menu = menus.findByFecha(fechaMenu).filter(MenuDiario::isConfigurado)
                    .orElseThrow(() -> new ReglaNegocioException("El menú del día no está disponible: no se puede pedir."));
            lineas.add(LineaPedido.deMenu(menu, cantidades.getMenus()));
        }
        if (lineas.isEmpty()) {
            throw new ReglaNegocioException("El pedido debe tener al menos un producto o un menú.");
        }
        return lineas;
    }

    private static void comprobarCantidad(int cantidad) {
        if (cantidad < 1 || cantidad > CantidadesPedido.MAX_CANTIDAD) {
            throw new ReglaNegocioException("Las cantidades deben estar entre 1 y " + CantidadesPedido.MAX_CANTIDAD + ".");
        }
    }
}
