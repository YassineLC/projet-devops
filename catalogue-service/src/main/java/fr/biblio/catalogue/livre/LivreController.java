package fr.biblio.catalogue.livre;

import fr.biblio.catalogue.openlibrary.ImportService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/livres")
@Validated
public class LivreController {

    private final LivreService service;
    private final ImportService importService;

    public LivreController(LivreService service, ImportService importService) {
        this.service = service;
        this.importService = importService;
    }

    @GetMapping
    public List<Livre> rechercher(@RequestParam(required = false) String q) {
        return service.rechercher(q);
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
