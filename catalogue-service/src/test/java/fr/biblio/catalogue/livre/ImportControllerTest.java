package fr.biblio.catalogue.livre;

import fr.biblio.catalogue.openlibrary.ImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "openlibrary.import-au-demarrage=false")
@AutoConfigureMockMvc
class ImportControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ImportService importService;

    @Test
    void import_renvoieNombreImportes() throws Exception {
        when(importService.importer("tolkien", 5)).thenReturn(4);

        mockMvc.perform(post("/api/livres/import").param("q", "tolkien").param("limite", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importes").value(4));
    }

    @Test
    void import_sansRequete_400() throws Exception {
        mockMvc.perform(post("/api/livres/import"))
                .andExpect(status().isBadRequest());
    }
}
