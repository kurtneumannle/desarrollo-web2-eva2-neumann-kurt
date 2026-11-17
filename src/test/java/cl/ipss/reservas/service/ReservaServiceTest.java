package cl.ipss.reservas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cl.ipss.reservas.RelojDePrueba;
import cl.ipss.reservas.dto.ReservaForm;
import cl.ipss.reservas.model.EstadoReserva;
import cl.ipss.reservas.model.Mesa;
import cl.ipss.reservas.model.Reserva;
import cl.ipss.reservas.repository.MesaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** Pruebas de las reglas de negocio de las reservas contra una base H2 real (se deshacen al terminar cada prueba). */
@SpringBootTest
@ActiveProfiles("test")
@Import(RelojDePrueba.class)
@Transactional
class ReservaServiceTest {

    private static final LocalDate MANANA = LocalDate.of(2026, 12, 2);
    private static final LocalTime HORA = LocalTime.of(20, 0);

    @Autowired
    private ReservaService servicio;
    @Autowired
    private MesaRepository mesas;

    private Mesa mesaDe2;
    private Mesa mesaDe6;

    @BeforeEach
    void crearMesas() {
        mesaDe2 = mesas.save(new Mesa(1, 2));
        mesaDe6 = mesas.save(new Mesa(2, 6));
    }

    private ReservaForm form(Mesa mesa, LocalDate fecha, LocalTime hora, int personas, String email) {
        ReservaForm f = new ReservaForm();
        f.setFecha(fecha);
        f.setHora(hora);
        f.setPersonas(personas);
        f.setMesaId(mesa.getId());
        f.setNombre("Carolina Soto");
        f.setEmail(email);
        f.setTelefono("+56 9 1234 5678");
        return f;
    }

    @Test
    void creaUnaReservaYLaMesaDejaDeEstarDisponible() {
        Reserva reserva = servicio.crear(form(mesaDe2, MANANA, HORA, 2, "caro@correo.cl"));

        assertThat(reserva.getId()).isNotNull();
        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.ACTIVA);
        assertThat(servicio.disponibilidad(MANANA, HORA, 2)).containsExactly(mesaDe6);
    }

    @Test
    void noPermiteReservarDosVecesLaMismaMesaEnElMismoHorario() {
        servicio.crear(form(mesaDe2, MANANA, HORA, 2, "uno@correo.cl"));

        assertThatThrownBy(() -> servicio.crear(form(mesaDe2, MANANA, HORA, 2, "dos@correo.cl")))
                .isInstanceOf(ReservaException.class)
                .hasMessageContaining("ya no está disponible");
    }

    @Test
    void rechazaUnaMesaConCapacidadInsuficiente() {
        assertThatThrownBy(() -> servicio.crear(form(mesaDe2, MANANA, HORA, 4, "caro@correo.cl")))
                .isInstanceOf(ReservaException.class);
    }

    @Test
    void rechazaFechasPasadasYHorasFueraDeHorario() {
        assertThatThrownBy(() -> servicio.crear(form(mesaDe2, LocalDate.of(2026, 11, 30), HORA, 2, "caro@correo.cl")))
                .isInstanceOf(ReservaException.class)
                .hasMessageContaining("ya pasaron");
        assertThatThrownBy(() -> servicio.crear(form(mesaDe2, MANANA, LocalTime.of(23, 0), 2, "caro@correo.cl")))
                .isInstanceOf(ReservaException.class)
                .hasMessageContaining("12:00");
    }

    @Test
    void cancelarLiberaLaMesa() {
        Reserva reserva = servicio.crear(form(mesaDe2, MANANA, HORA, 2, "caro@correo.cl"));

        Reserva cancelada = servicio.cancelar(reserva.getCodigo());

        assertThat(cancelada.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
        assertThat(servicio.disponibilidad(MANANA, HORA, 2)).contains(mesaDe2);
        assertThatThrownBy(() -> servicio.cancelar(reserva.getCodigo())).isInstanceOf(ReservaException.class);
    }

    @Test
    void modificarCambiaHorarioYMesaSinChocarConSiMisma() {
        Reserva reserva = servicio.crear(form(mesaDe2, MANANA, HORA, 2, "caro@correo.cl"));

        // La misma mesa sigue disponible para su propia reserva, aunque cambie solo la hora.
        servicio.modificar(reserva.getCodigo(), form(mesaDe2, MANANA, LocalTime.of(19, 0), 2, "caro@correo.cl"));
        Reserva cambiada = servicio.modificar(reserva.getCodigo(), form(mesaDe6, MANANA.plusDays(1), LocalTime.of(19, 0), 5, "caro@correo.cl"));

        assertThat(cambiada.getMesa()).isEqualTo(mesaDe6);
        assertThat(cambiada.getFecha()).isEqualTo(MANANA.plusDays(1));
        assertThat(cambiada.getPersonas()).isEqualTo(5);
    }

    @Test
    void reutilizaAlClientePorCorreoYListaSuHistorial() {
        servicio.crear(form(mesaDe2, MANANA, HORA, 2, "caro@correo.cl"));
        servicio.crear(form(mesaDe6, MANANA, HORA, 5, "CARO@correo.cl"));

        List<Reserva> historial = servicio.reservasDeCliente("caro@correo.cl");

        assertThat(historial).hasSize(2);
        assertThat(historial.get(0).getCliente().getId()).isEqualTo(historial.get(1).getCliente().getId());
    }

    @Test
    void unaMesaInactivaNoApareceComoDisponible() {
        mesaDe2.setActiva(false);
        mesas.save(mesaDe2);

        assertThat(servicio.disponibilidad(MANANA, HORA, 2)).containsExactly(mesaDe6);
    }

    @Test
    void codigoInexistenteLanzaError() {
        assertThatThrownBy(() -> servicio.buscarPorCodigo("no-existe")).isInstanceOf(ReservaException.class);
    }
}
