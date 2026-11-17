package cl.ipss.reservas.web;

import cl.ipss.reservas.service.ReservaException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Captura los errores de negocio que ningun controlador manejo (por ejemplo, un codigo de reserva inexistente). */
@ControllerAdvice
public class ManejadorDeErrores {

    @ExceptionHandler(ReservaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String reservaNoEncontrada(ReservaException e, Model modelo) {
        modelo.addAttribute("mensajeError", e.getMessage());
        return "error-negocio";
    }
}
