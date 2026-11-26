package es.upm.g16.sabor.config;

import es.upm.g16.sabor.modelo.Categoria;
import es.upm.g16.sabor.modelo.Producto;
import es.upm.g16.sabor.repositorio.ProductoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Carga una carta de ejemplo la primera vez que se arranca la aplicación.
 * Se desactiva con app.datos-iniciales=false.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final ProductoRepository productos;
    private final boolean activado;

    public DatosIniciales(ProductoRepository productos, @Value("${app.datos-iniciales:true}") boolean activado) {
        this.productos = productos;
        this.activado = activado;
    }

    @Override
    public void run(String... args) {
        if (!activado || productos.count() > 0) {
            return;
        }
        guardar("Momos de verduras", "Empanadillas al vapor rellenas de verduras con salsa de tomate y sésamo", Categoria.ENTRANTE, "6.50");
        guardar("Samosas", "Dos samosas de patata y guisantes", Categoria.ENTRANTE, "5.00");
        guardar("Ensalada himalaya", "Lechuga, pepino, tomate, cebolla roja y aliño de limón y comino", Categoria.ENTRANTE, "6.00");
        guardar("Sopa de lentejas", "Dal suave con cilantro", Categoria.ENTRANTE, "5.50");
        guardar("Dal bhat", "Arroz, lentejas, curry de verduras y encurtido", Categoria.PRINCIPAL, "11.50");
        guardar("Thukpa", "Sopa de fideos con pollo y verduras", Categoria.PRINCIPAL, "10.50");
        guardar("Curry de cordero", "Cordero guisado con especias nepalíes y arroz basmati", Categoria.PRINCIPAL, "13.00");
        guardar("Pollo choila", "Pollo a la brasa marinado con especias", Categoria.PRINCIPAL, "12.00");
        guardar("Kheer", "Arroz con leche al cardamomo", Categoria.POSTRE, "4.00");
        guardar("Sel roti", "Rosquilla de arroz dulce", Categoria.POSTRE, "4.50");
        guardar("Lassi de mango", "Bebida de yogur y mango", Categoria.BEBIDA, "3.50");
        guardar("Té masala", "Té negro con leche y especias", Categoria.BEBIDA, "2.50");
        guardar("Agua mineral", "Botella de 50 cl", Categoria.BEBIDA, "1.80");
    }

    private void guardar(String nombre, String descripcion, Categoria categoria, String precio) {
        productos.save(new Producto(nombre, descripcion, categoria, new BigDecimal(precio), true));
    }
}
