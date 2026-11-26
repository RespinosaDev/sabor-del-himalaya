package es.upm.g16.sabor.repositorio;

import es.upm.g16.sabor.modelo.Categoria;
import es.upm.g16.sabor.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByRetiradoFalseAndEnCartaTrue();

    List<Producto> findByRetiradoFalseAndCategoria(Categoria categoria);
}
