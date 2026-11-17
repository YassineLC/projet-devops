package fr.biblio.emprunts.emprunt;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/emprunts")
public class EmpruntController {

    private final EmpruntService service;

    public EmpruntController(EmpruntService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Emprunt emprunter(@Valid @RequestBody NouvelEmprunt demande) {
        return service.emprunter(demande);
    }

    @GetMapping
    public List<Emprunt> lister(@RequestParam(required = false) String emprunteur) {
        return service.lister(emprunteur);
    }

    @PostMapping("/{id}/retour")
    public Emprunt rendre(@PathVariable Long id) {
        return service.rendre(id);
    }
}
