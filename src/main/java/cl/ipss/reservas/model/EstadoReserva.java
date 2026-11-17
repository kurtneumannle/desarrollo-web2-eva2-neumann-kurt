package cl.ipss.reservas.model;

/** Estados posibles de una reserva. Una reserva cancelada libera la mesa pero se conserva como historial. */
public enum EstadoReserva {
    ACTIVA("Activa"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoReserva(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Texto que se muestra al usuario en las vistas. */
    public String getEtiqueta() {
        return etiqueta;
    }
}
