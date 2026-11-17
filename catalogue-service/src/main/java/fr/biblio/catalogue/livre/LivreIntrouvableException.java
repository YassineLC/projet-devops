package fr.biblio.catalogue.livre;

public class LivreIntrouvableException extends RuntimeException {

    public LivreIntrouvableException(Long id) {
        super("Livre introuvable : " + id);
    }
}
