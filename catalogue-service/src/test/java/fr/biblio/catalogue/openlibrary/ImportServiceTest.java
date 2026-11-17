package fr.biblio.catalogue.openlibrary;

import fr.biblio.catalogue.livre.Livre;
import fr.biblio.catalogue.livre.LivreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.startsWith;

@DataJpaTest
class ImportServiceTest {

    private static final String REPONSE = """
            {"docs": [
              {"key": "/works/OL1W", "title": "Dune", "author_name": ["Frank Herbert"],
               "first_publish_year": 1965, "isbn": ["9780441172719", "0441172717"], "cover_i": 123},
              {"key": "/works/OL2W", "title": "Sans couverture", "author_name": ["A", "B"]}
            ]}
            """;

    @Autowired
    LivreRepository repository;

    MockRestServiceServer serveur;
    ImportService importService;
    OpenLibraryProperties proprietes;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        serveur = MockRestServiceServer.bindTo(builder).build();
        proprietes = new OpenLibraryProperties("https://openlibrary.org", List.of("dune"), 12, 3, true);
        importService = new ImportService(new OpenLibraryClient(builder, proprietes), repository, proprietes);
    }

    @Test
    void importe_et_construitCouverture() {
        serveur.expect(requestTo(startsWith("https://openlibrary.org/search.json?q=dune")))
                .andRespond(withSuccess(REPONSE, MediaType.APPLICATION_JSON));

        int importes = importService.importer("dune", 12);

        assertThat(importes).isEqualTo(2);
        Livre dune = repository.findAll().stream().filter(l -> l.getTitre().equals("Dune")).findFirst().orElseThrow();
        assertThat(dune.getCouvertureUrl()).isEqualTo("https://covers.openlibrary.org/b/id/123-L.jpg");
        assertThat(dune.getIsbn()).isEqualTo("9780441172719");
        assertThat(dune.getAuteurs()).isEqualTo("Frank Herbert");
        assertThat(dune.getExemplairesDisponibles()).isEqualTo(3);
        Livre sans = repository.findAll().stream().filter(l -> l.getTitre().equals("Sans couverture")).findFirst().orElseThrow();
        assertThat(sans.getCouvertureUrl()).isNull();
        assertThat(sans.getIsbn()).isNull();
        assertThat(sans.getAuteurs()).isEqualTo("A, B");
    }

    @Test
    void dedoublonne_parCle() {
        serveur.expect(manyTimes(), requestTo(startsWith("https://openlibrary.org/search.json")))
                .andRespond(withSuccess(REPONSE, MediaType.APPLICATION_JSON));

        importService.importer("dune", 12);
        int secondImport = importService.importer("dune", 12);

        assertThat(secondImport).isZero();
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void demarrage_openLibraryEnPanne_neCassePas() {
        serveur.expect(manyTimes(), requestTo(startsWith("https://openlibrary.org/search.json")))
                .andRespond(withServerError());
        ImportAuDemarrage demarrage = new ImportAuDemarrage(importService, repository, proprietes);

        assertThatCode(() -> demarrage.run(null)).doesNotThrowAnyException();
        assertThat(repository.count()).isZero();
    }
}
