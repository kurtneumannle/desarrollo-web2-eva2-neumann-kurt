package cl.ipss.reservas.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Muestra el formulario de ingreso del personal; la autenticacion la resuelve Spring Security. */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
