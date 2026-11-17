package cl.ipss.reservas.repository;

import cl.ipss.reservas.model.Reserva;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de reservas. */
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    Optional<Reserva> findByCodigo(String codigo);

    /** Reservas de un dia, ordenadas por hora y numero de mesa (vista del personal). */
    List<Reserva> findByFechaOrderByHoraAscMesaNumeroAsc(LocalDate fecha);

    /** Historial de un cliente, de la mas reciente a la mas antigua. */
    List<Reserva> findByClienteEmailIgnoreCaseOrderByFechaDescHoraDesc(String email);

    boolean existsByMesaId(Long mesaId);
}
