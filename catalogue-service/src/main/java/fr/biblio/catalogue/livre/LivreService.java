package fr.biblio.catalogue.livre;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class LivreService {

    private final LivreRepository repository;

    public LivreService(LivreRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PageLivres rechercher(String q, int page, int taille) {
        Pageable pageable = PageRequest.of(page, taille, Sort.by("titre").and(Sort.by("id")));
        if (q == null || q.isBlank()) {
            return PageLivres.de(repository.findAll(pageable));
        }
        String terme = q.trim();
        return PageLivres.de(
                repository.findByTitreContainingIgnoreCaseOrAuteursContainingIgnoreCase(terme, terme, pageable));
    }

    @Transactional(readOnly = true)
    public Livre trouver(Long id) {
        return repository.findById(id).orElseThrow(() -> new LivreIntrouvableException(id));
    }

    @Transactional
    public Livre reserver(Long id) {
        if (repository.reserver(id) == 0) {
            trouver(id);
            throw new StockEpuiseException(id);
        }
        return trouver(id);
    }

    @Transactional
    public Livre liberer(Long id) {
        repository.liberer(id);
        return trouver(id);
    }
}
