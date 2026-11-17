package cl.ipss.reservas.service;

import cl.ipss.reservas.dto.ReservaForm;
import cl.ipss.reservas.model.Cliente;
import cl.ipss.reservas.model.EstadoReserva;
import cl.ipss.reservas.model.Mesa;
import cl.ipss.reservas.model.Reserva;
import cl.ipss.reservas.repository.ClienteRepository;
import cl.ipss.reservas.repository.MesaRepository;
import cl.ipss.reservas.repository.ReservaRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas de negocio de las reservas: disponibilidad en tiempo real, creacion, modificacion y cancelacion.
 * Los controladores solo traducen HTTP; todas las validaciones de negocio viven aqui.
 */
@Service
@Transactional(readOnly = true)
public class ReservaService {

    /** Primera y ultima hora de inicio de una reserva (cada una dura un bloque de una hora). */
    private static final int PRIMERA_HORA = 12;
    private static final int ULTIMA_HORA = 21;
    /** Sin reserva real: valor que no coincide con ningun id al consultar disponibilidad. */
    private static final Long SIN_EXCLUIR = -1L;

    private final ReservaRepository reservas;
    private final MesaRepository mesas;
    private final ClienteRepository clientes;
    private final Clock reloj;

    public ReservaService(ReservaRepository reservas, MesaRepository mesas, ClienteRepository clientes, Clock reloj) {
        this.reservas = reservas;
        this.mesas = mesas;
        this.clientes = clientes;
        this.reloj = reloj;
    }

    /** Horas en que se puede reservar: de 12:00 a 21:00, cada hora. */
    public List<LocalTime> horariosDisponibles() {
        return IntStream.rangeClosed(PRIMERA_HORA, ULTIMA_HORA).mapToObj(h -> LocalTime.of(h, 0)).toList();
    }

    /** Mesas libres para una fecha, hora y cantidad de personas. */
    public List<Mesa> disponibilidad(LocalDate fecha, LocalTime hora, int personas) {
        return mesas.findDisponibles(fecha, hora, personas, SIN_EXCLUIR);
    }

    /** Igual que {@link #disponibilidad}, pero deja libre la mesa de la reserva que se esta editando. */
    public List<Mesa> disponibilidadParaEditar(Reserva reserva, LocalDate fecha, LocalTime hora, int personas) {
        return mesas.findDisponibles(fecha, hora, personas, reserva.getId());
    }

    /** Crea una reserva nueva; si el correo ya existe, actualiza los datos de contacto del cliente. */
    @Transactional
    public Reserva crear(ReservaForm form) {
        validarFechaYHora(form.getFecha(), form.getHora());
        Mesa mesa = mesaLibre(form, SIN_EXCLUIR);

        Cliente cliente = clientes.findByEmailIgnoreCase(form.getEmail().trim())
                .orElseGet(() -> new Cliente(form.getNombre().trim(), form.getEmail().trim().toLowerCase(), form.getTelefono().trim()));
        cliente.setNombre(form.getNombre().trim());
        cliente.setTelefono(form.getTelefono().trim());
        clientes.save(cliente);

        return reservas.save(new Reserva(cliente, mesa, form.getFecha(), form.getHora(), form.getPersonas()));
    }

    /** Cambia fecha, hora, personas o mesa de una reserva activa. */
    @Transactional
    public Reserva modificar(String codigo, ReservaForm form) {
        Reserva reserva = buscarPorCodigo(codigo);
        exigirActiva(reserva);
        validarFechaYHora(form.getFecha(), form.getHora());
        Mesa mesa = mesaLibre(form, reserva.getId());

        reserva.setFecha(form.getFecha());
        reserva.setHora(form.getHora());
        reserva.setPersonas(form.getPersonas());
        reserva.setMesa(mesa);
        return reservas.save(reserva);
    }

    /** Cancela una reserva: queda como historial y la mesa vuelve a estar disponible. */
    @Transactional
    public Reserva cancelar(String codigo) {
        Reserva reserva = buscarPorCodigo(codigo);
        exigirActiva(reserva);
        reserva.setEstado(EstadoReserva.CANCELADA);
        return reservas.save(reserva);
    }

    public Reserva buscarPorCodigo(String codigo) {
        return reservas.findByCodigo(codigo).orElseThrow(() -> new ReservaException("No existe una reserva con ese código."));
    }

    /** Historial de reservas de un cliente identificado por su correo. */
    public List<Reserva> reservasDeCliente(String email) {
        return reservas.findByClienteEmailIgnoreCaseOrderByFechaDescHoraDesc(email.trim());
    }

    /** Reservas de un dia para el personal del restaurante. */
    public List<Reserva> reservasDelDia(LocalDate fecha) {
        return reservas.findByFechaOrderByHoraAscMesaNumeroAsc(fecha);
    }

    /** Fecha de hoy segun el reloj de la aplicacion (inyectable para poder probarla). */
    public LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    /** Comprueba que la mesa elegida sigue libre y cabe el grupo; evita reservas dobles. */
    private Mesa mesaLibre(ReservaForm form, Long excluir) {
        return mesas.findDisponibles(form.getFecha(), form.getHora(), form.getPersonas(), excluir).stream()
                .filter(m -> m.getId().equals(form.getMesaId()))
                .findFirst()
                .orElseThrow(() -> new ReservaException(
                        "La mesa elegida ya no está disponible para ese horario o no tiene capacidad. Elige otra."));
    }

    private void validarFechaYHora(LocalDate fecha, LocalTime hora) {
        if (!horariosDisponibles().contains(hora)) {
            throw new ReservaException("Reservamos entre las 12:00 y las 21:00, en horas exactas.");
        }
        if (LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now(reloj))) {
            throw new ReservaException("No se puede reservar en una fecha y hora que ya pasaron.");
        }
    }

    private void exigirActiva(Reserva reserva) {
        if (!reserva.isActiva()) {
            throw new ReservaException("La reserva ya está cancelada.");
        }
    }
}
