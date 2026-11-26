package es.upm.g16.sabor.repositorio;

import es.upm.g16.sabor.modelo.MenuDiario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MenuDiarioRepository extends JpaRepository<MenuDiario, Long> {

    Optional<MenuDiario> findByFecha(LocalDate fecha);
}
