package cl.ipss.reservas.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import cl.ipss.reservas.RelojDePrueba;
import cl.ipss.reservas.model.Mesa;
import cl.ipss.reservas.repository.MesaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Pruebas de la capa web: vistas, formulario de reserva y proteccion de la zona de administracion. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RelojDePrueba.class)
@Transactional
class ReservaWebTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private MesaRepository mesas;

    @Test
    void laPaginaDeInicioEsPublica() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("index"));
    }

    @Test
    void elFormularioMuestraLasMesasLibresDelHorario() throws Exception {
        mesas.save(new Mesa(1, 4));

        mvc.perform(get("/reservas/nueva").param("fecha", "2026-12-02").param("hora", "20:00").param("personas", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Para 4 personas")));
    }

    @Test
    void reservarRedirigeAlDetalleYMuestraLosDatos() throws Exception {
        Mesa mesa = mesas.save(new Mesa(1, 4));

        mvc.perform(post("/reservas").with(csrf())
                        .param("fecha", "2026-12-02").param("hora", "20:00").param("personas", "3")
                        .param("mesaId", String.valueOf(mesa.getId()))
                        .param("nombre", "Carolina Soto").param("email", "caro@correo.cl").param("telefono", "+56912345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/reservas/*"));
    }

    @Test
    void unFormularioInvalidoVuelveConErrores() throws Exception {
        mesas.save(new Mesa(1, 4));

        mvc.perform(post("/reservas").with(csrf())
                        .param("fecha", "2026-12-02").param("hora", "20:00").param("personas", "3")
                        .param("nombre", "").param("email", "no-es-correo").param("telefono", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservas/nueva"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("El correo no es válido")));
    }

    @Test
    void unaReservaInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/reservas/no-existe")).andExpect(status().isNotFound());
    }

    @Test
    void laZonaDeAdministracionExigeIngreso() throws Exception {
        mvc.perform(get("/admin/mesas")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void elPersonalVeLasMesasYPuedeAgregarUna() throws Exception {
        mvc.perform(get("/admin/mesas")).andExpect(status().isOk()).andExpect(view().name("admin/mesas"));
        mvc.perform(post("/admin/mesas").with(csrf()).param("numero", "9").param("capacidad", "4"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void unUsuarioSinRolAdminNoEntraAlAdmin() throws Exception {
        mvc.perform(get("/admin/mesas")).andExpect(status().isForbidden());
    }
}
