package fr.biblio.emprunts.catalogue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Exécuté avant que le pod ne soit déclaré prêt : le premier emprunt ne paie plus
 * l'ouverture du canal gRPC (sinon ~3 s, au-delà du délai d'appel).
 */
@Component
public class PrechauffageCatalogue implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PrechauffageCatalogue.class);

    private final CatalogueClient catalogue;

    public PrechauffageCatalogue(CatalogueClient catalogue) {
        this.catalogue = catalogue;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            catalogue.prechauffer();
            log.info("Canal gRPC vers le catalogue prêt");
        } catch (Exception e) {
            log.warn("Préchauffage gRPC impossible (catalogue indisponible ?) : {}", e.getMessage());
        }
    }
}
