package fr.biblio.emprunts.emprunt;

import fr.biblio.emprunts.catalogue.CatalogueClient;
import fr.biblio.grpc.LivreInfo;
import io.grpc.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmpruntControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EmpruntRepository repository;

    @MockitoBean
    CatalogueClient catalogue;

    private static final LivreInfo DUNE = LivreInfo.newBuilder().setId(1).setTitre("Dune").setExemplairesDisponibles(2).build();

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    private String corps(Object livreId, String emprunteur) {
        return "{\"livreId\": " + livreId + ", \"emprunteur\": \"" + emprunteur + "\"}";
    }

    @Test
    void emprunter_ok_201_retourPrevuA14Jours() throws Exception {
        when(catalogue.reserver(1)).thenReturn(DUNE);

        mockMvc.perform(post("/api/emprunts").contentType(MediaType.APPLICATION_JSON).content(corps(1, "Alice")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titreLivre").value("Dune"))
                .andExpect(jsonPath("$.emprunteur").value("Alice"))
                .andExpect(jsonPath("$.statut").value("EN_COURS"))
                .andExpect(jsonPath("$.dateRetourPrevue").value(LocalDate.now().plusDays(14).toString()));
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void emprunter_sansStock_409_rienEnregistre() throws Exception {
        when(catalogue.reserver(1)).thenThrow(Status.FAILED_PRECONDITION.withDescription("plus d'exemplaire").asRuntimeException());

        mockMvc.perform(post("/api/emprunts").contentType(MediaType.APPLICATION_JSON).content(corps(1, "Alice")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erreur").exists());
        assertThat(repository.count()).isZero();
    }

    @Test
    void emprunter_livreInconnu_404() throws Exception {
        when(catalogue.reserver(42)).thenThrow(Status.NOT_FOUND.asRuntimeException());

        mockMvc.perform(post("/api/emprunts").contentType(MediaType.APPLICATION_JSON).content(corps(42, "Alice")))
                .andExpect(status().isNotFound());
    }

    @Test
    void emprunter_catalogueInjoignable_503_rienEnregistre() throws Exception {
        when(catalogue.reserver(1)).thenThrow(Status.UNAVAILABLE.asRuntimeException());

        mockMvc.perform(post("/api/emprunts").contentType(MediaType.APPLICATION_JSON).content(corps(1, "Alice")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.erreur").exists());
        assertThat(repository.count()).isZero();
    }

    @Test
    void emprunter_emprunteurVide_400() throws Exception {
        mockMvc.perform(post("/api/emprunts").contentType(MediaType.APPLICATION_JSON).content(corps(1, " ")))
                .andExpect(status().isBadRequest());
        verify(catalogue, never()).reserver(1);
    }

    @Test
    void lister_parEmprunteur() throws Exception {
        repository.save(new Emprunt(1L, "Dune", "Alice", LocalDate.now(), LocalDate.now().plusDays(14)));
        repository.save(new Emprunt(2L, "Fondation", "Bob", LocalDate.now(), LocalDate.now().plusDays(14)));

        mockMvc.perform(get("/api/emprunts").param("emprunteur", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titreLivre").value("Dune"));
    }

    @Test
    void rendre_ok_libereExemplaire() throws Exception {
        Emprunt e = repository.save(new Emprunt(1L, "Dune", "Alice", LocalDate.now(), LocalDate.now().plusDays(14)));
        when(catalogue.liberer(1)).thenReturn(DUNE);

        mockMvc.perform(post("/api/emprunts/{id}/retour", e.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("RENDU"));
        verify(catalogue, times(1)).liberer(1);
    }

    @Test
    void rendre_dejaRendu_409_pasDeLiberation() throws Exception {
        Emprunt e = repository.save(new Emprunt(1L, "Dune", "Alice", LocalDate.now(), LocalDate.now().plusDays(14)));
        when(catalogue.liberer(1)).thenReturn(DUNE);
        mockMvc.perform(post("/api/emprunts/{id}/retour", e.getId())).andExpect(status().isOk());

        mockMvc.perform(post("/api/emprunts/{id}/retour", e.getId()))
                .andExpect(status().isConflict());
        verify(catalogue, times(1)).liberer(1);
    }

    @Test
    void statut_enRetard() throws Exception {
        repository.save(new Emprunt(1L, "Dune", "Alice", LocalDate.now().minusDays(15), LocalDate.now().minusDays(1)));

        mockMvc.perform(get("/api/emprunts").param("emprunteur", "Alice"))
                .andExpect(jsonPath("$[0].statut").value("EN_RETARD"));
    }
}
