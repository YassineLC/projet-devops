package fr.biblio.catalogue.livre;

import fr.biblio.catalogue.openlibrary.ImportService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/livres")
public class LivreController {

    private final LivreService service;
    private final ImportService importService;

    public LivreController(LivreService service, ImportService importService) {
        this.service = service;
        this.importService = importService;
    }

    @GetMapping
    public PageLivres rechercher(@RequestParam(required = false) String q,
                                 @RequestParam(defaultValue = "0") @Min(0) int page,
                                 @RequestParam(defaultValue = "20") @Min(1) @Max(50) int taille) {
        return service.rechercher(q, page, taille);
    }

    @GetMapping("/{id}")
    public Livre detail(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PostMapping("/import")
    public Map<String, Integer> importer(@RequestParam @NotBlank String q,
                                         @RequestParam(defaultValue = "12") @Min(1) @Max(50) int limite) {
        return Map.of("importes", importService.importer(q, limite));
    }
}
