package cl.ipss.reservas.web;

import cl.ipss.reservas.dto.ReservaForm;
import cl.ipss.reservas.model.Reserva;
import cl.ipss.reservas.service.ReservaException;
import cl.ipss.reservas.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de la parte publica (patron MVC): recibe las solicitudes HTTP, delega la logica en
 * {@link ReservaService} y elige la vista Thymeleaf que se muestra. No contiene reglas de negocio.
 */
@Controller
public class ReservaController {

    private final ReservaService servicio;

    public ReservaController(ReservaService servicio) {
        this.servicio = servicio;
    }

    /** Pagina de inicio con el acceso a reservar y a consultar reservas. */
    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    /**
     * Formulario de reserva. Cuando ya vienen fecha, hora y personas en la URL, muestra en tiempo real
     * las mesas disponibles para ese horario.
     */
    @GetMapping("/reservas/nueva")
    public String nueva(@ModelAttribute("form") ReservaForm form, Model modelo) {
        prepararFormulario(modelo, form, null);
        return "reservas/nueva";
    }

    /** Crea la reserva. Si hay errores de validacion o la mesa se ocupo, vuelve al formulario con el mensaje. */
    @PostMapping("/reservas")
    public String crear(@Valid @ModelAttribute("form") ReservaForm form, BindingResult errores,
                        Model modelo, RedirectAttributes flash) {
        if (!errores.hasErrors()) {
            try {
                Reserva reserva = servicio.crear(form);
                flash.addFlashAttribute("mensaje", "Reserva confirmada. Guarda este enlace para modificarla o cancelarla.");
                return "redirect:/reservas/" + reserva.getCodigo();
            } catch (ReservaException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        prepararFormulario(modelo, form, null);
        return "reservas/nueva";
    }

    /** Detalle de una reserva, con acciones para modificar o cancelar. */
    @GetMapping("/reservas/{codigo}")
    public String detalle(@PathVariable String codigo, Model modelo) {
        modelo.addAttribute("reserva", servicio.buscarPorCodigo(codigo));
        return "reservas/detalle";
    }

    /** Consulta las reservas de un cliente a partir de su correo. */
    @GetMapping("/reservas/mis-reservas")
    public String misReservas(@RequestParam(required = false) String email, Model modelo) {
        if (email != null && !email.isBlank()) {
            modelo.addAttribute("email", email.trim());
            modelo.addAttribute("reservas", servicio.reservasDeCliente(email));
        }
        return "reservas/mis-reservas";
    }

    /** Formulario para modificar una reserva activa; parte con sus datos actuales. */
    @GetMapping("/reservas/{codigo}/editar")
    public String editar(@PathVariable String codigo, @RequestParam(required = false) String buscar,
                         @ModelAttribute("form") ReservaForm form, Model modelo) {
        Reserva reserva = servicio.buscarPorCodigo(codigo);
        if (buscar == null) {
            // Primera visita: se rellena el formulario con los datos de la reserva.
            form.setFecha(reserva.getFecha());
            form.setHora(reserva.getHora());
            form.setPersonas(reserva.getPersonas());
            form.setMesaId(reserva.getMesa().getId());
        }
        form.setNombre(reserva.getCliente().getNombre());
        form.setEmail(reserva.getCliente().getEmail());
        form.setTelefono(reserva.getCliente().getTelefono());
        prepararFormulario(modelo, form, reserva);
        modelo.addAttribute("reserva", reserva);
        return "reservas/editar";
    }

    /** Guarda los cambios de una reserva. */
    @PostMapping("/reservas/{codigo}/editar")
    public String guardarEdicion(@PathVariable String codigo, @Valid @ModelAttribute("form") ReservaForm form,
                                 BindingResult errores, Model modelo, RedirectAttributes flash) {
        Reserva reserva = servicio.buscarPorCodigo(codigo);
        if (!errores.hasErrors()) {
            try {
                servicio.modificar(codigo, form);
                flash.addFlashAttribute("mensaje", "Reserva modificada.");
                return "redirect:/reservas/" + codigo;
            } catch (ReservaException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        prepararFormulario(modelo, form, reserva);
        modelo.addAttribute("reserva", reserva);
        return "reservas/editar";
    }

    /** Cancela la reserva y vuelve al detalle, donde queda marcada como cancelada. */
    @PostMapping("/reservas/{codigo}/cancelar")
    public String cancelar(@PathVariable String codigo, RedirectAttributes flash) {
        try {
            servicio.cancelar(codigo);
            flash.addFlashAttribute("mensaje", "Reserva cancelada.");
        } catch (ReservaException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reservas/" + codigo;
    }

    /** Agrega al modelo lo que necesitan las vistas del formulario: horarios, fecha minima y mesas libres. */
    private void prepararFormulario(Model modelo, ReservaForm form, Reserva enEdicion) {
        modelo.addAttribute("horarios", servicio.horariosDisponibles());
        modelo.addAttribute("hoy", servicio.hoy());
        if (form.getFecha() != null && form.getHora() != null && form.getPersonas() > 0) {
            modelo.addAttribute("mesas", enEdicion == null
                    ? servicio.disponibilidad(form.getFecha(), form.getHora(), form.getPersonas())
                    : servicio.disponibilidadParaEditar(enEdicion, form.getFecha(), form.getHora(), form.getPersonas()));
        }
    }
}
