package es.upm.g16.sabor.servicio;

import es.upm.g16.sabor.modelo.Categoria;
import es.upm.g16.sabor.modelo.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas unitarias de US1 (CPU-01 a CPU-06). */
@SpringBootTest
@Transactional
class CartaServiceTest {

    @Autowired
    private CartaService carta;

    private DatosProducto momos() {
        return new DatosProducto("Momos", "Empanadillas al vapor", Categoria.ENTRANTE, new BigDecimal("6.50"), true);
    }

    @Test
    @DisplayName("CPU-01 Alta de un producto válido: queda en la carta")
    void altaProductoValido() {
        Producto p = carta.alta(momos());
        assertNotNull(p.getId());
        assertTrue(carta.listarCarta().stream().anyMatch(x -> x.getId().equals(p.getId())));
    }

    @Test
    @DisplayName("CPU-02 Alta sin nombre: se rechaza con mensaje")
    void altaSinNombre() {
        DatosProducto d = momos();
        d.setNombre("  ");
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> carta.alta(d));
        assertEquals("El nombre del producto es obligatorio.", e.getMessage());
    }

    @Test
    @DisplayName("CPU-03 Alta con precio negativo o cero en la carta: se rechaza")
    void altaPrecioNoValido() {
        DatosProducto negativo = momos();
        negativo.setPrecio(new BigDecimal("-1"));
        assertThrows(ReglaNegocioException.class, () -> carta.alta(negativo));
        DatosProducto cero = momos();
        cero.setPrecio(BigDecimal.ZERO);
        assertThrows(ReglaNegocioException.class, () -> carta.alta(cero));
    }

    @Test
    @DisplayName("CPU-04 Alta sin categoría: se rechaza")
    void altaSinCategoria() {
        DatosProducto d = momos();
        d.setCategoria(null);
        assertThrows(ReglaNegocioException.class, () -> carta.alta(d));
    }

    @Test
    @DisplayName("CPU-05 Modificar un producto: cambian sus datos")
    void modificarProducto() {
        Producto p = carta.alta(momos());
        DatosProducto cambios = new DatosProducto("Momos de pollo", "Rellenos de pollo", Categoria.ENTRANTE, new BigDecimal("7.00"), true);
        Producto modificado = carta.modificar(p.getId(), cambios);
        assertEquals("Momos de pollo", modificado.getNombre());
        assertEquals(0, new BigDecimal("7.00").compareTo(modificado.getPrecio()));
    }

    @Test
    @DisplayName("CPU-06 Retirar un producto: deja de estar en la carta pero no se borra")
    void retirarProducto() {
        Producto p = carta.alta(momos());
        carta.retirar(p.getId());
        List<Producto> lista = carta.listarCarta();
        assertFalse(lista.stream().anyMatch(x -> x.getId().equals(p.getId())));
        assertTrue(carta.obtener(p.getId()).isRetirado());
    }
}
