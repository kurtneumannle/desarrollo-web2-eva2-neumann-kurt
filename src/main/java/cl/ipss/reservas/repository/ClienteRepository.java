package cl.ipss.reservas.repository;

import cl.ipss.reservas.model.Cliente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de clientes. Spring Data genera la implementacion a partir del nombre del metodo. */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByEmailIgnoreCase(String email);
}
