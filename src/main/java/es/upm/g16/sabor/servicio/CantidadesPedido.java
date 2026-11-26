package es.upm.g16.sabor.servicio;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lo que el cliente ha elegido en el formulario de pedido (US9, US11): cantidad de cada
 * producto de la carta y número de menús del día. En el formulario, los campos de producto
 * se llaman p_<id> y el de menús se llama menus.
 */
public class CantidadesPedido {

    public static final int MAX_CANTIDAD = 20;

    private final Map<Long, Integer> productos = new LinkedHashMap<>();
    private int menus;

    public CantidadesPedido() {
    }

    public CantidadesPedido(Map<Long, Integer> productos, int menus) {
        this.productos.putAll(productos);
        this.menus = menus;
    }

    /** Lee los parámetros del formulario. Los campos vacíos o a 0 se ignoran. */
    public static CantidadesPedido desde(Map<String, String> parametros) {
        CantidadesPedido cantidades = new CantidadesPedido();
        for (Map.Entry<String, String> campo : parametros.entrySet()) {
            String nombre = campo.getKey();
            if (nombre.equals("menus")) {
                cantidades.menus = leer(campo.getValue());
            } else if (nombre.startsWith("p_")) {
                Long id;
                try {
                    id = Long.valueOf(nombre.substring(2));
                } catch (NumberFormatException e) {
                    throw new ReglaNegocioException("El formulario de pedido no es válido.");
                }
                int cantidad = leer(campo.getValue());
                if (cantidad != 0) {
                    cantidades.productos.put(id, cantidad);
                }
            }
        }
        return cantidades;
    }

    private static int leer(String texto) {
        if (texto == null || texto.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("Las cantidades deben ser números enteros.");
        }
    }

    public Map<Long, Integer> getProductos() {
        return productos;
    }

    public int getMenus() {
        return menus;
    }
}
