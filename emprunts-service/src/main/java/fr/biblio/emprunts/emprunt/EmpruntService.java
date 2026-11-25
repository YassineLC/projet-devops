package fr.biblio.emprunts.emprunt;

import fr.biblio.emprunts.catalogue.CatalogueClient;
import fr.biblio.grpc.LivreInfo;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmpruntService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmpruntService.class);

    private final EmpruntRepository repository;
    private final CatalogueClient catalogue;
    private final int dureeJours;

    public EmpruntService(EmpruntRepository repository, CatalogueClient catalogue,
                          @Value("${emprunts.duree-jours}") int dureeJours) {
        this.repository = repository;
        this.catalogue = catalogue;
        this.dureeJours = dureeJours;
    }

    @Transactional
    public Emprunt emprunter(NouvelEmprunt demande) {
        LivreInfo livre = appelCatalogue(() -> catalogue.reserver(demande.livreId()));
        LocalDate aujourdhui = LocalDate.now();
        return repository.save(new Emprunt(demande.livreId(), livre.getTitre(), demande.emprunteur().trim(),
                aujourdhui, aujourdhui.plusDays(dureeJours)));
    }

    @Transactional
    public Emprunt rendre(Long id) {
        Emprunt emprunt = repository.findById(id)
                .orElseThrow(() -> new EmpruntException(HttpStatus.NOT_FOUND, "Emprunt introuvable : " + id));
        if (emprunt.getStatut() == Statut.RENDU) {
            throw new EmpruntException(HttpStatus.CONFLICT, "Emprunt déjà rendu : " + id);
        }
        appelCatalogue(() -> catalogue.liberer(emprunt.getLivreId()));
        emprunt.rendre(LocalDate.now());
        return emprunt;
    }

    @Transactional(readOnly = true)
    public List<Emprunt> lister(String emprunteur) {
        if (emprunteur == null || emprunteur.isBlank()) {
            return repository.findAll();
        }
        return repository.findByEmprunteurIgnoreCaseOrderByDateEmpruntDesc(emprunteur.trim());
    }

    private LivreInfo appelCatalogue(java.util.function.Supplier<LivreInfo> appel) {
        try {
            return appel.get();
        } catch (StatusRuntimeException e) {
            log.warn("Appel gRPC catalogue en échec : {}", e.getStatus());
            throw switch (e.getStatus().getCode()) {
                case NOT_FOUND -> new EmpruntException(HttpStatus.NOT_FOUND, "Livre introuvable");
                case FAILED_PRECONDITION -> new EmpruntException(HttpStatus.CONFLICT, "Plus aucun exemplaire disponible");
                default -> new EmpruntException(HttpStatus.SERVICE_UNAVAILABLE, "Catalogue indisponible");
            };
        }
    }
}
