package fr.biblio.catalogue.livre;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ErreurHandler {

    @ExceptionHandler(LivreIntrouvableException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> introuvable(LivreIntrouvableException e) {
        return Map.of("erreur", e.getMessage());
    }
}
