package fr.biblio.catalogue.openlibrary;

import fr.biblio.catalogue.livre.LivreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ImportAuDemarrage implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ImportAuDemarrage.class);

    private final ImportService importService;
    private final LivreRepository repository;
    private final OpenLibraryProperties proprietes;

    public ImportAuDemarrage(ImportService importService, LivreRepository repository, OpenLibraryProperties proprietes) {
        this.importService = importService;
        this.repository = repository;
        this.proprietes = proprietes;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!proprietes.importAuDemarrage() || repository.count() > 0) {
            return;
        }
        for (String requete : importService.requetesInitiales()) {
            try {
                int n = importService.importer(requete, proprietes.limite());
                log.info("Import Open Library '{}' : {} livres", requete, n);
            } catch (Exception e) {
                log.error("Import Open Library '{}' impossible : {}", requete, e.getMessage());
            }
        }
    }
}
