package cl.ipss.reservas.repository;

import cl.ipss.reservas.model.Mesa;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Acceso a datos de mesas, incluida la consulta de disponibilidad. */
public interface MesaRepository extends JpaRepository<Mesa, Long> {

    List<Mesa> findAllByOrderByNumeroAsc();

    boolean existsByNumero(int numero);

    boolean existsByNumeroAndIdNot(int numero, Long id);

    /**
     * Mesas activas con capacidad suficiente que no tienen una reserva activa en esa fecha y hora.
     * El parametro {@code excluir} permite ignorar una reserva (la que se esta modificando) para que su propia
     * mesa siga apareciendo como disponible; con un valor que no exista (-1) no se excluye nada.
     * Se ordenan de la mas ajustada a la mas grande, para no ocupar una mesa de 8 con una pareja.
     */
    @Query("""
            select m from Mesa m
            where m.activa = true
              and m.capacidad >= :personas
              and not exists (
                  select r.id from Reserva r
                  where r.mesa = m
                    and r.fecha = :fecha
                    and r.hora = :hora
                    and r.estado = cl.ipss.reservas.model.EstadoReserva.ACTIVA
                    and r.id <> :excluir)
            order by m.capacidad, m.numero
            """)
    List<Mesa> findDisponibles(@Param("fecha") LocalDate fecha,
                               @Param("hora") LocalTime hora,
                               @Param("personas") int personas,
                               @Param("excluir") Long excluir);
}
