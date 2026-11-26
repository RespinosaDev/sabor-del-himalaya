package es.upm.g16.sabor.repositorio;

import es.upm.g16.sabor.modelo.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByLoginIgnoreCase(String login);

    Optional<Cliente> findByLoginIgnoreCase(String login);

    Optional<Cliente> findByTokenActivacion(String tokenActivacion);
}
