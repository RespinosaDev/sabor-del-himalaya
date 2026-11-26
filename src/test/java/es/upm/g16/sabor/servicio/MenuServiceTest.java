package es.upm.g16.sabor.servicio;

import es.upm.g16.sabor.modelo.Categoria;
import es.upm.g16.sabor.modelo.MenuDiario;
import es.upm.g16.sabor.modelo.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas unitarias de US2 (CPU-07 a CPU-12). */
@SpringBootTest
@Transactional
class MenuServiceTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 10);

    @Autowired
    private MenuService menus;

    @Autowired
    private CartaService carta;

    private final List<Long> entrantes = new ArrayList<>();
    private final List<Long> principales = new ArrayList<>();
    private final List<Long> postres = new ArrayList<>();
    private Long bebida;

    @BeforeEach
    void crearProductos() {
        for (int i = 1; i <= 3; i++) {
            entrantes.add(alta("Entrante " + i, Categoria.ENTRANTE));
            principales.add(alta("Principal " + i, Categoria.PRINCIPAL));
        }
        postres.add(alta("Postre 1", Categoria.POSTRE));
        postres.add(alta("Postre 2", Categoria.POSTRE));
        bebida = alta("Agua", Categoria.BEBIDA);
    }

    private Long alta(String nombre, Categoria categoria) {
        return carta.alta(new DatosProducto(nombre, null, categoria, new BigDecimal("5.00"), true)).getId();
    }

    @Test
    @DisplayName("CPU-07 Configurar un menú completo (3+3+2 y bebida) a 15 €")
    void configurarMenuCompleto() {
        MenuDiario menu = menus.configurar(DIA, entrantes, principales, postres, bebida);
        assertTrue(menu.isConfigurado());
        assertEquals(3, menu.getEntrantes().size());
        assertEquals(3, menu.getPrincipales().size());
        assertEquals(2, menu.getPostres().size());
        assertEquals(0, new BigDecimal("15.00").compareTo(menu.getPrecio()));
    }

    @Test
    @DisplayName("CPU-08 Menú con dos entrantes: se rechaza")
    void menuConDosEntrantes() {
        List<Long> dos = entrantes.subList(0, 2);
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> menus.configurar(DIA, dos, principales, postres, bebida));
        assertEquals("Hay que elegir exactamente 3 entrantes.", e.getMessage());
    }

    @Test
    @DisplayName("CPU-09 Plato de otra categoría en los postres: se rechaza")
    void platoDeOtraCategoria() {
        List<Long> malos = List.of(postres.get(0), principales.get(0));
        assertThrows(ReglaNegocioException.class, () -> menus.configurar(DIA, entrantes, principales, malos, bebida));
    }

    @Test
    @DisplayName("CPU-10 Menú sin bebida: se rechaza")
    void menuSinBebida() {
        assertThrows(ReglaNegocioException.class, () -> menus.configurar(DIA, entrantes, principales, postres, null));
    }

    @Test
    @DisplayName("CPU-11 Dejar un día sin menú: queda registrado como no configurado")
    void diaSinMenu() {
        menus.configurar(DIA, entrantes, principales, postres, bebida);
        MenuDiario menu = menus.marcarSinConfigurar(DIA);
        assertFalse(menu.isConfigurado());
        assertTrue(menu.getEntrantes().isEmpty());
        assertFalse(menus.consultar(DIA).get().isConfigurado());
    }

    @Test
    @DisplayName("CPU-12 Plato fuera de carta: se guarda y se puede usar en el menú")
    void platoFueraDeCarta() {
        Producto plato = menus.crearPlatoFueraDeCarta("Curry del chef", "Solo para el menú", Categoria.PRINCIPAL);
        assertFalse(plato.isEnCarta());
        assertTrue(carta.listarPorCategoria(Categoria.PRINCIPAL).stream().anyMatch(p -> p.getId().equals(plato.getId())));
        assertFalse(carta.listarCarta().stream().anyMatch(p -> p.getId().equals(plato.getId())));
        List<Long> conPlato = List.of(principales.get(0), principales.get(1), plato.getId());
        assertTrue(menus.configurar(DIA, entrantes, conPlato, postres, bebida).isConfigurado());
    }
}
