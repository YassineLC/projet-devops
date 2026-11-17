package fr.biblio.catalogue.livre;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/livres")
public class LivreController {

    private final LivreService service;

    public LivreController(LivreService service) {
        this.service = service;
    }

    @GetMapping
    public List<Livre> rechercher(@RequestParam(required = false) String q) {
        return service.rechercher(q);
    }

    @GetMapping("/{id}")
    public Livre detail(@PathVariable Long id) {
        return service.trouver(id);
    }
}
