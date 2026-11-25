package fr.biblio.catalogue.livre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "openlibrary.import-au-demarrage=false")
@AutoConfigureMockMvc
class LivreControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    LivreRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.save(new Livre("/works/OL1W", "9780441172719", "Dune", "Frank Herbert", 1965, null, 3));
        repository.save(new Livre("/works/OL2W", null, "Fondation", "Isaac Asimov", 1951, null, 3));
    }

    private void ajouterLivres(int n) {
        for (int i = 0; i < n; i++) {
            repository.save(new Livre("/works/X" + i + "W", null, "Livre %02d".formatted(i), "Auteur", 2000, null, 1));
        }
    }

    @Test
    void recherche_parTitre_filtre() throws Exception {
        mockMvc.perform(get("/api/livres").param("q", "dune"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu.length()").value(1))
                .andExpect(jsonPath("$.contenu[0].titre").value("Dune"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void recherche_sansFiltre_renvoieTout() throws Exception {
        mockMvc.perform(get("/api/livres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu.length()").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void pagination_parDefaut20() throws Exception {
        ajouterLivres(23); // 25 livres au total

        mockMvc.perform(get("/api/livres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu.length()").value(20))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.taille").value(20))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get("/api/livres").param("page", "1"))
                .andExpect(jsonPath("$.contenu.length()").value(5))
                .andExpect(jsonPath("$.page").value(1));
    }

    @Test
    void pagination_trieeParTitre() throws Exception {
        mockMvc.perform(get("/api/livres"))
                .andExpect(jsonPath("$.contenu[0].titre").value("Dune"))
                .andExpect(jsonPath("$.contenu[1].titre").value("Fondation"));
    }

    @Test
    void pagination_avecRecherche() throws Exception {
        ajouterLivres(23);

        mockMvc.perform(get("/api/livres").param("q", "livre").param("taille", "10").param("page", "2"))
                .andExpect(jsonPath("$.contenu.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(23))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void pagination_tailleInvalide_400() throws Exception {
        mockMvc.perform(get("/api/livres").param("taille", "0")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/livres").param("taille", "500")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/livres").param("page", "-1")).andExpect(status().isBadRequest());
    }

    @Test
    void detail_inconnu_renvoie404() throws Exception {
        mockMvc.perform(get("/api/livres/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erreur").exists());
    }
}
