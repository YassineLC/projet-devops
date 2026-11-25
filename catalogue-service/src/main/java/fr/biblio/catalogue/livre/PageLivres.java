package fr.biblio.catalogue.livre;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageLivres(List<Livre> contenu, int page, int taille, long totalElements, int totalPages) {

    static PageLivres de(Page<Livre> page) {
        return new PageLivres(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
