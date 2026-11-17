package cl.ipss.reservas.web;

import cl.ipss.reservas.dto.MesaForm;
import cl.ipss.reservas.service.MesaService;
import cl.ipss.reservas.service.ReservaException;
import cl.ipss.reservas.service.ReservaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Zona de administracion (protegida por Spring Security): reservas del dia y configuracion de mesas. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ReservaService reservas;
    private final MesaService mesas;

    public AdminController(ReservaService reservas, MesaService mesas) {
        this.reservas = reservas;
        this.mesas = mesas;
    }

    /** Reservas de un dia (hoy por defecto) para que el personal prepare el salon. */
    @GetMapping("/reservas")
    public String reservasDelDia(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                 Model modelo) {
        LocalDate dia = fecha != null ? fecha : reservas.hoy();
        modelo.addAttribute("fecha", dia);
        modelo.addAttribute("reservas", reservas.reservasDelDia(dia));
        return "admin/reservas";
    }

    /** El personal puede cancelar cualquier reserva (por ejemplo, si el cliente avisa por telefono). */
    @PostMapping("/reservas/{codigo}/cancelar")
    public String cancelarReserva(@PathVariable String codigo,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                  RedirectAttributes flash) {
        try {
            reservas.cancelar(codigo);
            flash.addFlashAttribute("mensaje", "Reserva cancelada.");
        } catch (ReservaException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reservas?fecha=" + fecha;
    }

    /** Listado de mesas con el formulario para agregar una nueva. */
    @GetMapping("/mesas")
    public String listarMesas(Model modelo) {
        modelo.addAttribute("mesas", mesas.listar());
        if (!modelo.containsAttribute("form")) {
            modelo.addAttribute("form", new MesaForm());
        }
        return "admin/mesas";
    }

    @PostMapping("/mesas")
    public String crearMesa(@Valid @ModelAttribute("form") MesaForm form, BindingResult errores,
                            Model modelo, RedirectAttributes flash) {
        if (!errores.hasErrors()) {
            try {
                mesas.crear(form);
                flash.addFlashAttribute("mensaje", "Mesa " + form.getNumero() + " creada.");
                return "redirect:/admin/mesas";
            } catch (ReservaException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        modelo.addAttribute("mesas", mesas.listar());
        return "admin/mesas";
    }

    @PostMapping("/mesas/{id}")
    public String actualizarMesa(@PathVariable Long id, @Valid MesaForm form, BindingResult errores,
                                 RedirectAttributes flash) {
        if (errores.hasErrors()) {
            flash.addFlashAttribute("error", "Revisa los datos: número y capacidad deben ser válidos.");
            return "redirect:/admin/mesas";
        }
        try {
            mesas.actualizar(id, form);
            flash.addFlashAttribute("mensaje", "Mesa actualizada.");
        } catch (ReservaException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/mesas";
    }

    @PostMapping("/mesas/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes flash) {
        try {
            mesas.cambiarEstado(id);
            flash.addFlashAttribute("mensaje", "Estado de la mesa actualizado.");
        } catch (ReservaException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/mesas";
    }

    @PostMapping("/mesas/{id}/eliminar")
    public String eliminarMesa(@PathVariable Long id, RedirectAttributes flash) {
        try {
            mesas.eliminar(id);
            flash.addFlashAttribute("mensaje", "Mesa eliminada.");
        } catch (ReservaException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/mesas";
    }
}
