package cl.ipss.reservas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Reserva de una mesa para una fecha y una hora. Cada reserva tiene un codigo aleatorio que se usa en las URL
 * publicas, de modo que nadie pueda modificar o cancelar la reserva de otra persona adivinando un numero.
 */
@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String codigo = UUID.randomUUID().toString();

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false)
    private int personas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoReserva estado = EstadoReserva.ACTIVA;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creadaEn = LocalDateTime.now();

    protected Reserva() {
        // Constructor exigido por JPA
    }

    public Reserva(Cliente cliente, Mesa mesa, LocalDate fecha, LocalTime hora, int personas) {
        this.cliente = cliente;
        this.mesa = mesa;
        this.fecha = fecha;
        this.hora = hora;
        this.personas = personas;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public Cliente getCliente() { return cliente; }
    public Mesa getMesa() { return mesa; }
    public void setMesa(Mesa mesa) { this.mesa = mesa; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public int getPersonas() { return personas; }
    public void setPersonas(int personas) { this.personas = personas; }
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
    public LocalDateTime getCreadaEn() { return creadaEn; }

    public boolean isActiva() {
        return estado == EstadoReserva.ACTIVA;
    }
}
