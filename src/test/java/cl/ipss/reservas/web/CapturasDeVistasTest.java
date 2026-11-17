package cl.ipss.reservas.web;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import cl.ipss.reservas.RelojDePrueba;
import cl.ipss.reservas.dto.ReservaForm;
import cl.ipss.reservas.model.Mesa;
import cl.ipss.reservas.model.Reserva;
import cl.ipss.reservas.repository.MesaRepository;
import cl.ipss.reservas.service.ReservaService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Utilidad para el informe: renderiza las vistas reales con datos de ejemplo y las guarda como HTML autonomo
 * (con el CSS incrustado) para fotografiarlas. Solo corre si se pasa -Dcapturas.dir=carpeta; en una ejecucion normal
 * de las pruebas se omite.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RelojDePrueba.class)
@Transactional
class CapturasDeVistasTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private MesaRepository mesas;
    @Autowired
    private ReservaService servicio;

    private String guardar(Path dir, String nombre, String url) throws Exception {
        String html = mvc.perform(get(url)).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String css = Files.readString(Path.of("src/main/resources/static/css/app.css"));
        html = html.replace("<link rel=\"stylesheet\" href=\"/css/app.css\">", "<style>" + css + "</style>");
        Files.writeString(dir.resolve(nombre + ".html"), html);
        return html;
    }

    private ReservaForm form(Mesa mesa, int hora, int personas, String nombre, String email) {
        ReservaForm f = new ReservaForm();
        f.setFecha(LocalDate.of(2026, 12, 3));
        f.setHora(LocalTime.of(hora, 0));
        f.setPersonas(personas);
        f.setMesaId(mesa.getId());
        f.setNombre(nombre);
        f.setEmail(email);
        f.setTelefono("+56 9 1234 5678");
        return f;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void generaVistasParaElInforme() throws Exception {
        String carpeta = System.getProperty("capturas.dir");
        assumeTrue(carpeta != null, "Sin -Dcapturas.dir no se generan vistas");
        Path dir = Path.of(carpeta);
        Files.createDirectories(dir);

        int[] capacidades = {2, 2, 2, 4, 4, 4, 6, 8};
        Mesa[] salon = new Mesa[capacidades.length];
        for (int i = 0; i < capacidades.length; i++) {
            salon[i] = mesas.save(new Mesa(i + 1, capacidades[i]));
        }
        Reserva propia = servicio.crear(form(salon[3], 20, 3, "Carolina Soto", "carolina.soto@ejemplo.cl"));
        servicio.crear(form(salon[0], 20, 2, "Matías Rojas", "matias.rojas@ejemplo.cl"));
        servicio.crear(form(salon[6], 21, 5, "Valentina Pérez", "valentina.perez@ejemplo.cl"));
        Reserva cancelada = servicio.crear(form(salon[4], 19, 4, "Carolina Soto", "carolina.soto@ejemplo.cl"));
        servicio.cancelar(cancelada.getCodigo());

        guardar(dir, "01_inicio", "/");
        guardar(dir, "02_disponibilidad", "/reservas/nueva?fecha=2026-12-03&hora=20:00&personas=3");
        guardar(dir, "03_reserva", "/reservas/" + propia.getCodigo());
        guardar(dir, "04_modificar", "/reservas/" + propia.getCodigo() + "/editar");
        guardar(dir, "05_mis_reservas", "/reservas/mis-reservas?email=carolina.soto@ejemplo.cl");
        guardar(dir, "06_cancelada", "/reservas/" + cancelada.getCodigo());
        guardar(dir, "07_admin_reservas", "/admin/reservas?fecha=2026-12-03");
        guardar(dir, "08_admin_mesas", "/admin/mesas");
        guardar(dir, "09_movil", "/reservas/nueva?fecha=2026-12-03&hora=20:00&personas=3");
    }
}
