package fr.biblio.catalogue.livre;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LivreService {

    private final LivreRepository repository;

    public LivreService(LivreRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Livre> rechercher(String q) {
        if (q == null || q.isBlank()) {
            return repository.findAll();
        }
        return repository.findByTitreContainingIgnoreCaseOrAuteursContainingIgnoreCase(q, q);
    }

    @Transactional(readOnly = true)
    public Livre trouver(Long id) {
        return repository.findById(id).orElseThrow(() -> new LivreIntrouvableException(id));
    }
}
