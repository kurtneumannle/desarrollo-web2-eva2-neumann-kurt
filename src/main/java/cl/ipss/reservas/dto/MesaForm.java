package cl.ipss.reservas.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** Datos del formulario de administracion de mesas. */
public class MesaForm {

    @Min(value = 1, message = "El número de mesa debe ser positivo")
    private int numero;

    @Min(value = 1, message = "La capacidad mínima es 1")
    @Max(value = 20, message = "La capacidad máxima es 20")
    private int capacidad = 4;

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
}
