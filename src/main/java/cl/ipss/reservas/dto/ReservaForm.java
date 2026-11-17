package cl.ipss.reservas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Datos que llegan desde el formulario de reserva. Se separa de la entidad para validar la entrada
 * sin exponer el modelo de base de datos directamente a la vista.
 */
public class ReservaForm {

    @NotNull(message = "Elige una fecha")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "Elige una hora")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime hora;

    @Min(value = 1, message = "Debe ser al menos 1 persona")
    @Max(value = 12, message = "Para grupos de más de 12 personas llama al restaurante")
    private int personas = 2;

    @NotNull(message = "Elige una mesa disponible")
    private Long mesaId;

    @NotBlank(message = "Escribe tu nombre")
    @Size(max = 80, message = "El nombre es demasiado largo")
    private String nombre;

    @NotBlank(message = "Escribe tu correo")
    @Email(message = "El correo no es válido")
    @Size(max = 120, message = "El correo es demasiado largo")
    private String email;

    @NotBlank(message = "Escribe tu teléfono")
    @Pattern(regexp = "^[0-9+ ]{8,20}$", message = "El teléfono debe tener entre 8 y 20 dígitos")
    private String telefono;

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public int getPersonas() { return personas; }
    public void setPersonas(int personas) { this.personas = personas; }
    public Long getMesaId() { return mesaId; }
    public void setMesaId(Long mesaId) { this.mesaId = mesaId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
