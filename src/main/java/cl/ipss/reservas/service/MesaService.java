package cl.ipss.reservas.service;

import cl.ipss.reservas.dto.MesaForm;
import cl.ipss.reservas.model.Mesa;
import cl.ipss.reservas.repository.MesaRepository;
import cl.ipss.reservas.repository.ReservaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Administracion de las mesas del salon (zona del personal). */
@Service
@Transactional(readOnly = true)
public class MesaService {

    private final MesaRepository mesas;
    private final ReservaRepository reservas;

    public MesaService(MesaRepository mesas, ReservaRepository reservas) {
        this.mesas = mesas;
        this.reservas = reservas;
    }

    public List<Mesa> listar() {
        return mesas.findAllByOrderByNumeroAsc();
    }

    @Transactional
    public Mesa crear(MesaForm form) {
        if (mesas.existsByNumero(form.getNumero())) {
            throw new ReservaException("Ya existe la mesa " + form.getNumero() + ".");
        }
        return mesas.save(new Mesa(form.getNumero(), form.getCapacidad()));
    }

    @Transactional
    public Mesa actualizar(Long id, MesaForm form) {
        Mesa mesa = buscar(id);
        if (mesas.existsByNumeroAndIdNot(form.getNumero(), id)) {
            throw new ReservaException("Ya existe la mesa " + form.getNumero() + ".");
        }
        mesa.setNumero(form.getNumero());
        mesa.setCapacidad(form.getCapacidad());
        return mesas.save(mesa);
    }

    /** Activa o desactiva una mesa (por ejemplo, por mantencion) sin perder su historial de reservas. */
    @Transactional
    public Mesa cambiarEstado(Long id) {
        Mesa mesa = buscar(id);
        mesa.setActiva(!mesa.isActiva());
        return mesas.save(mesa);
    }

    /** Elimina una mesa solo si nunca tuvo reservas; si las tuvo hay que desactivarla para conservar el historial. */
    @Transactional
    public void eliminar(Long id) {
        Mesa mesa = buscar(id);
        if (reservas.existsByMesaId(id)) {
            throw new ReservaException("La mesa " + mesa.getNumero() + " tiene reservas; desactívala en vez de eliminarla.");
        }
        mesas.delete(mesa);
    }

    private Mesa buscar(Long id) {
        return mesas.findById(id).orElseThrow(() -> new ReservaException("La mesa no existe."));
    }
}
