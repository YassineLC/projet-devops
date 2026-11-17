package fr.biblio.catalogue.livre;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LivreRepository extends JpaRepository<Livre, Long> {

    List<Livre> findByTitreContainingIgnoreCaseOrAuteursContainingIgnoreCase(String titre, String auteurs);

    boolean existsByCleOpenLibrary(String cleOpenLibrary);
}
