package fr.biblio.emprunts.emprunt;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ErreurHandler {

    @ExceptionHandler(EmpruntException.class)
    public ResponseEntity<Map<String, String>> emprunt(EmpruntException e) {
        return ResponseEntity.status(e.getStatut()).body(Map.of("erreur", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> validation(MethodArgumentNotValidException e) {
        return Map.of("erreur", "Requête invalide : livreId et emprunteur sont obligatoires");
    }
}
