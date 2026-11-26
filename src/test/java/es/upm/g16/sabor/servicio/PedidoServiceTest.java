package es.upm.g16.sabor.servicio;

import es.upm.g16.sabor.modelo.Categoria;
import es.upm.g16.sabor.modelo.Cliente;
import es.upm.g16.sabor.modelo.EstadoPedido;
import es.upm.g16.sabor.modelo.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de US3, US4, US9, US10 y US11 (CPU-22 a CPU-37).
 * El reloj es controlable para probar el plazo de diez minutos.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:pruebaspedidos;DB_CLOSE_DELAY=-1")
@Transactional
class PedidoServiceTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 12, 1, 12, 0);
    private static final LocalDate DIA = INICIO.toLocalDate();

    @TestConfiguration
    static class RelojConfigPrueba {
        @Bean
        @Primary
        RelojDePrueba relojDePrueba() {
            return new RelojDePrueba(INICIO);
        }
    }

    @Autowired
    private PedidoService pedidos;
    @Autowired
    private CartaService carta;
    @Autowired
    private MenuService menus;
    @Autowired
    private ClienteService clientes;
    @Autowired
    private RelojDePrueba reloj;

    private Cliente ana;
    private Cliente luis;
    private Long momos;
    private Long samosa;
    private Long lassi;

    @BeforeEach
    void preparar() {
        reloj.fijar(INICIO);
        ana = clientes.registrar(new DatosRegistro("Ana Pérez", "C/ Alcalá 10", "ana@example.com", "ana", "secreto1", "secreto1"));
        luis = clientes.registrar(new DatosRegistro("Luis Gil", "C/ Goya 5", "luis@example.com", "luis", "secreto1", "secreto1"));
        momos = alta("Momos", Categoria.ENTRANTE, "6.50");
        samosa = alta("Samosa", Categoria.ENTRANTE, "5.00");
        lassi = alta("Lassi", Categoria.BEBIDA, "3.50");
    }

    private Long alta(String nombre, Categoria categoria, String precio) {
        return carta.alta(new DatosProducto(nombre, null, categoria, new BigDecimal(precio), true)).getId();
    }

    private void configurarMenuDeHoy() {
        List<Long> entrantes = List.of(momos, samosa, alta("Entrante 3", Categoria.ENTRANTE, "4.00"));
        List<Long> principales = List.of(alta("Principal 1", Categoria.PRINCIPAL, "9.00"),
                alta("Principal 2", Categoria.PRINCIPAL, "9.00"), alta("Principal 3", Categoria.PRINCIPAL, "9.00"));
        List<Long> postres = List.of(alta("Postre 1", Categoria.POSTRE, "3.00"), alta("Postre 2", Categoria.POSTRE, "3.00"));
        menus.configurar(DIA, entrantes, principales, postres, lassi);
    }

    private CantidadesPedido pedirMomos(int cantidad) {
        return new CantidadesPedido(Map.of(momos, cantidad), 0);
    }

    @Test
    @DisplayName("CPU-22 Pedido válido con producto, bebida y menú: estado Recibido e importe correcto")
    void pedidoValido() {
        configurarMenuDeHoy();
        Pedido pedido = pedidos.confirmar(ana.getId(), new CantidadesPedido(Map.of(momos, 2, lassi, 1), 1));
        assertEquals(EstadoPedido.RECIBIDO, pedido.getEstado());
        assertEquals("C/ Alcalá 10", pedido.getDireccion());
        assertEquals(INICIO, pedido.getFechaHora());
        assertEquals(3, pedido.getLineas().size());
        // 2 x 6,50 + 1 x 3,50 + 1 menú de 15,00
        assertEquals(0, new BigDecimal("31.50").compareTo(pedido.getImporteTotal()));
    }

    @Test
    @DisplayName("CPU-23 Pedido sin ningún concepto: se rechaza")
    void pedidoVacio() {
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> pedidos.confirmar(ana.getId(), new CantidadesPedido()));
        assertEquals("El pedido debe tener al menos un producto o un menú.", e.getMessage());
    }

    @Test
    @DisplayName("CPU-24 Pedir un menú de un día sin menú configurado: se rechaza")
    void menuNoConfigurado() {
        assertThrows(ReglaNegocioException.class, () -> pedidos.confirmar(ana.getId(), new CantidadesPedido(Map.of(), 1)));
    }

    @Test
    @DisplayName("CPU-25 Cantidad 0, negativa o mayor de 20: se rechaza")
    void cantidadNoValida() {
        assertThrows(ReglaNegocioException.class, () -> pedidos.confirmar(ana.getId(), pedirMomos(-1)));
        assertThrows(ReglaNegocioException.class, () -> pedidos.confirmar(ana.getId(), pedirMomos(21)));
    }

    @Test
    @DisplayName("CPU-26 Producto retirado de la carta: no se puede pedir")
    void productoRetirado() {
        carta.retirar(momos);
        assertThrows(ReglaNegocioException.class, () -> pedidos.confirmar(ana.getId(), pedirMomos(1)));
    }

    @Test
    @DisplayName("CPU-27 Cambiar el estado a Entregado: se conserva y no se altera otro pedido")
    void cambiarEstado() {
        Pedido uno = pedidos.confirmar(ana.getId(), pedirMomos(1));
        Pedido otro = pedidos.confirmar(luis.getId(), pedirMomos(2));
        pedidos.cambiarEstado(uno.getId(), EstadoPedido.EN_PREPARACION);
        pedidos.cambiarEstado(uno.getId(), EstadoPedido.ENTREGADO);
        assertEquals(EstadoPedido.ENTREGADO, pedidos.obtener(uno.getId()).getEstado());
        assertEquals(EstadoPedido.RECIBIDO, pedidos.obtener(otro.getId()).getEstado());
    }

    @Test
    @DisplayName("CPU-28 Un pedido cancelado no puede cambiar de estado; el personal no cancela")
    void estadoDeCancelado() {
        Pedido pedido = pedidos.confirmar(ana.getId(), pedirMomos(1));
        assertThrows(ReglaNegocioException.class, () -> pedidos.cambiarEstado(pedido.getId(), EstadoPedido.CANCELADO));
        pedidos.cancelar(ana.getId(), pedido.getId());
        assertThrows(ReglaNegocioException.class, () -> pedidos.cambiarEstado(pedido.getId(), EstadoPedido.ENTREGADO));
    }

    @Test
    @DisplayName("CPU-29 Número de pedido inexistente: se informa")
    void pedidoInexistente() {
        assertThrows(ReglaNegocioException.class, () -> pedidos.obtener(999_999L));
    }

    @Test
    @DisplayName("CPU-30 Un cliente solo ve sus pedidos; el de otro cliente se trata como inexistente")
    void soloPedidosPropios() {
        Pedido deAna = pedidos.confirmar(ana.getId(), pedirMomos(1));
        pedidos.confirmar(luis.getId(), pedirMomos(1));
        assertEquals(1, pedidos.pedidosDelCliente(ana.getId()).size());
        assertEquals(deAna.getId(), pedidos.pedidosDelCliente(ana.getId()).get(0).getId());
        assertThrows(ReglaNegocioException.class, () -> pedidos.obtenerDelCliente(luis.getId(), deAna.getId()));
    }

    @Test
    @DisplayName("CPU-31 Modificar a los 5 minutos: se cambia el contenido y se recalcula el importe")
    void modificarEnPlazo() {
        Pedido pedido = pedidos.confirmar(ana.getId(), pedirMomos(1));
        reloj.avanzar(Duration.ofMinutes(5));
        pedidos.modificar(ana.getId(), pedido.getId(), new CantidadesPedido(Map.of(momos, 3, samosa, 1), 0));
        Pedido modificado = pedidos.obtenerDelCliente(ana.getId(), pedido.getId());
        assertEquals(2, modificado.getLineas().size());
        // 3 x 6,50 + 1 x 5,00
        assertEquals(0, new BigDecimal("24.50").compareTo(modificado.getImporteTotal()));
    }

    @Test
    @DisplayName("CPU-32 Modificar a los 11 minutos: se rechaza y el pedido no cambia")
    void modificarFueraDePlazo() {
        Pedido pedido = pedidos.confirmar(ana.getId(), pedirMomos(1));
        reloj.avanzar(Duration.ofMinutes(11));
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> pedidos.modificar(ana.getId(), pedido.getId(), pedirMomos(5)));
        assertTrue(e.getMessage().contains("diez minutos"));
        assertEquals(0, new BigDecimal("6.50").compareTo(pedidos.obtenerDelCliente(ana.getId(), pedido.getId()).getImporteTotal()));
    }

    @Test
    @DisplayName("CPU-33 Cancelar justo a los 10 minutos: se permite y queda Cancelado")
    void cancelarEnElLimite() {
        Pedido pedido = pedidos.confirmar(ana.getId(), pedirMomos(1));
        reloj.avanzar(Duration.ofMinutes(10));
        pedidos.cancelar(ana.getId(), pedido.getId());
        assertEquals(EstadoPedido.CANCELADO, pedidos.obtenerDelCliente(ana.getId(), pedido.getId()).getEstado());
    }

    @Test
    @DisplayName("CPU-34 Cancelar pasados 10 minutos, o el pedido de otro cliente: se rechaza")
    void cancelarNoPermitido() {
        Pedido pedido = pedidos.confirmar(ana.getId(), pedirMomos(1));
        assertThrows(ReglaNegocioException.class, () -> pedidos.cancelar(luis.getId(), pedido.getId()));
        reloj.avanzar(Duration.ofMinutes(10).plusSeconds(1));
        assertThrows(ReglaNegocioException.class, () -> pedidos.cancelar(ana.getId(), pedido.getId()));
        assertEquals(EstadoPedido.RECIBIDO, pedidos.obtener(pedido.getId()).getEstado());
        assertFalse(pedidos.admiteCambios(pedido));
    }

    @Test
    @DisplayName("CPU-35 Facturación de un día: suma los pedidos de ese día y excluye los cancelados")
    void facturacionDelDia() {
        pedidos.confirmar(ana.getId(), pedirMomos(2));                                  // 13,00
        pedidos.confirmar(luis.getId(), new CantidadesPedido(Map.of(samosa, 1), 0));    // 5,00
        Pedido cancelado = pedidos.confirmar(ana.getId(), pedirMomos(1));               // 6,50 (se cancela)
        pedidos.cancelar(ana.getId(), cancelado.getId());
        reloj.fijar(INICIO.plusDays(1));
        pedidos.confirmar(ana.getId(), pedirMomos(4));                                  // otro día

        FacturaDia factura = pedidos.facturacion(DIA);
        assertEquals(2, factura.numeroPedidos());
        assertEquals(0, new BigDecimal("18.00").compareTo(factura.total()));
    }

    @Test
    @DisplayName("CPU-36 Facturación de un día sin pedidos: 0 €")
    void facturacionSinPedidos() {
        FacturaDia factura = pedidos.facturacion(LocalDate.of(2026, 1, 1));
        assertEquals(0, factura.numeroPedidos());
        assertEquals(0, BigDecimal.ZERO.compareTo(factura.total()));
        assertThrows(ReglaNegocioException.class, () -> pedidos.facturacion(null));
    }

    @Test
    @DisplayName("CPU-37 Un cambio de precio posterior no altera el importe de pedidos ya hechos")
    void precioAplicadoSeConserva() {
        Pedido pedido = pedidos.confirmar(ana.getId(), pedirMomos(2));
        carta.modificar(momos, new DatosProducto("Momos", null, Categoria.ENTRANTE, new BigDecimal("9.00"), true));
        assertEquals(0, new BigDecimal("13.00").compareTo(pedidos.obtener(pedido.getId()).getImporteTotal()));
    }
}
