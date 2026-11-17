package fr.biblio.catalogue.openlibrary;

import fr.biblio.catalogue.livre.Livre;
import fr.biblio.catalogue.livre.LivreRepository;
import fr.biblio.catalogue.openlibrary.OpenLibraryClient.DocOpenLibrary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ImportService {

    private static final String URL_COUVERTURE = "https://covers.openlibrary.org/b/id/%d-L.jpg";

    private final OpenLibraryClient client;
    private final LivreRepository repository;
    private final OpenLibraryProperties proprietes;

    public ImportService(OpenLibraryClient client, LivreRepository repository, OpenLibraryProperties proprietes) {
        this.client = client;
        this.repository = repository;
        this.proprietes = proprietes;
    }

    @Transactional
    public int importer(String q, int limite) {
        int crees = 0;
        for (DocOpenLibrary doc : client.rechercher(q, limite)) {
            if (doc.key() == null || doc.title() == null || repository.existsByCleOpenLibrary(doc.key())) {
                continue;
            }
            repository.save(versLivre(doc));
            crees++;
        }
        return crees;
    }

    private Livre versLivre(DocOpenLibrary doc) {
        String auteurs = doc.auteurs() == null ? null : String.join(", ", doc.auteurs());
        String isbn = doc.isbn() == null || doc.isbn().isEmpty() ? null : doc.isbn().getFirst();
        String couverture = doc.couverture() == null ? null : URL_COUVERTURE.formatted(doc.couverture());
        return new Livre(doc.key(), isbn, doc.title(), auteurs, doc.annee(), couverture,
                proprietes.exemplairesParDefaut());
    }

    public List<String> requetesInitiales() {
        return proprietes.requetesInitiales() == null ? List.of() : proprietes.requetesInitiales();
    }
}
