package fr.biblio.emprunts.emprunt;

import org.springframework.http.HttpStatus;

public class EmpruntException extends RuntimeException {

    private final HttpStatus statut;

    public EmpruntException(HttpStatus statut, String message) {
        super(message);
        this.statut = statut;
    }

    public HttpStatus getStatut() {
        return statut;
    }
}
