package cl.ipss.reservas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Mesa del restaurante. El personal puede cambiar su capacidad o desactivarla desde la zona de administracion. */
@Entity
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numero visible de la mesa en el salon; no se repite. */
    @Column(nullable = false, unique = true)
    private int numero;

    @Column(nullable = false)
    private int capacidad;

    /** Una mesa inactiva no aparece como disponible (por ejemplo, si esta en mantencion). */
    @Column(nullable = false)
    private boolean activa = true;

    protected Mesa() {
        // Constructor exigido por JPA
    }

    public Mesa(int numero, int capacidad) {
        this.numero = numero;
        this.capacidad = capacidad;
    }

    public Long getId() { return id; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
