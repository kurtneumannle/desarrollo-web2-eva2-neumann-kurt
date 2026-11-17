package cl.ipss.reservas.service;

/** Error de negocio de las reservas (mesa ocupada, fecha pasada, reserva inexistente...). El mensaje se muestra al usuario. */
public class ReservaException extends RuntimeException {

    public ReservaException(String mensaje) {
        super(mensaje);
    }
}
