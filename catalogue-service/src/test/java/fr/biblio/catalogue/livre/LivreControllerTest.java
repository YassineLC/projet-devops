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

    @Test
    void recherche_parTitre_filtre() throws Exception {
        mockMvc.perform(get("/api/livres").param("q", "dune"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titre").value("Dune"));
    }

    @Test
    void recherche_sansFiltre_renvoieTout() throws Exception {
        mockMvc.perform(get("/api/livres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void detail_inconnu_renvoie404() throws Exception {
        mockMvc.perform(get("/api/livres/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erreur").exists());
    }
}
