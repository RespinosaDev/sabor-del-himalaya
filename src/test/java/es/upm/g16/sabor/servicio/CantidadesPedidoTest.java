package es.upm.g16.sabor.servicio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Pruebas unitarias de la lectura del formulario de pedido (CPU-38 y CPU-39). No necesitan Spring. */
class CantidadesPedidoTest {

    @Test
    @DisplayName("CPU-38 Se leen las cantidades válidas y se ignoran los campos vacíos o a 0")
    void lecturaValida() {
        Map<String, String> formulario = new LinkedHashMap<>();
        formulario.put("p_3", "2");
        formulario.put("p_4", "0");
        formulario.put("p_5", "");
        formulario.put("menus", "1");
        formulario.put("otro", "x");
        CantidadesPedido cantidades = CantidadesPedido.desde(formulario);
        assertEquals(1, cantidades.getProductos().size());
        assertEquals(2, cantidades.getProductos().get(3L));
        assertFalse(cantidades.getProductos().containsKey(4L));
        assertEquals(1, cantidades.getMenus());
    }

    @Test
    @DisplayName("CPU-39 Cantidad no numérica o campo mal formado: se rechaza")
    void lecturaNoValida() {
        assertThrows(ReglaNegocioException.class, () -> CantidadesPedido.desde(Map.of("p_3", "dos")));
        assertThrows(ReglaNegocioException.class, () -> CantidadesPedido.desde(Map.of("p_abc", "1")));
        assertThrows(ReglaNegocioException.class, () -> CantidadesPedido.desde(Map.of("menus", "1.5")));
    }
}
