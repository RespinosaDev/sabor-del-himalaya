package es.upm.g16.sabor.repositorio;

import es.upm.g16.sabor.modelo.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteIdOrderByFechaHoraDesc(Long clienteId);

    List<Pedido> findAllByOrderByFechaHoraDesc();

    List<Pedido> findByFechaHoraGreaterThanEqualAndFechaHoraLessThan(LocalDateTime desde, LocalDateTime hasta);
}
