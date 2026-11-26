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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas unitarias de US8, consulta de la carta por categorías (CPU-40 y CPU-41). */
@SpringBootTest
@Transactional
class CartaPorCategoriaTest {

    @Autowired
    private CartaService carta;

    @Test
    @DisplayName("CPU-40 La carta se agrupa por categoría con nombre, descripción y precio de cada producto")
    void agrupadaPorCategoria() {
        carta.alta(new DatosProducto("Momos", "Al vapor", Categoria.ENTRANTE, new BigDecimal("6.50"), true));
        carta.alta(new DatosProducto("Samosa", "De patata", Categoria.ENTRANTE, new BigDecimal("5.00"), true));
        carta.alta(new DatosProducto("Lassi", "De mango", Categoria.BEBIDA, new BigDecimal("3.50"), true));

        Map<Categoria, List<Producto>> agrupada = carta.listarCartaPorCategoria();
        assertEquals(2, agrupada.get(Categoria.ENTRANTE).size());
        assertEquals(1, agrupada.get(Categoria.BEBIDA).size());
        assertFalse(agrupada.containsKey(Categoria.POSTRE));
        Producto momos = agrupada.get(Categoria.ENTRANTE).get(0);
        assertEquals("Momos", momos.getNombre());
        assertEquals("Al vapor", momos.getDescripcion());
        assertEquals(0, new BigDecimal("6.50").compareTo(momos.getPrecio()));
    }

    @Test
    @DisplayName("CPU-41 La carta no muestra productos retirados ni platos fuera de carta")
    void sinRetiradosNiFueraDeCarta() {
        Producto retirado = carta.alta(new DatosProducto("Retirado", null, Categoria.PRINCIPAL, new BigDecimal("9.00"), true));
        carta.retirar(retirado.getId());
        carta.alta(new DatosProducto("Solo menú", null, Categoria.PRINCIPAL, BigDecimal.ZERO, false));
        assertTrue(carta.listarCartaPorCategoria().getOrDefault(Categoria.PRINCIPAL, List.of()).isEmpty());
    }
}
