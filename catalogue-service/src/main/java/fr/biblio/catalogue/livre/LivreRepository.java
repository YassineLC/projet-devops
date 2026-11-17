package fr.biblio.catalogue.livre;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LivreRepository extends JpaRepository<Livre, Long> {

    List<Livre> findByTitreContainingIgnoreCaseOrAuteursContainingIgnoreCase(String titre, String auteurs);

    boolean existsByCleOpenLibrary(String cleOpenLibrary);

    // Décrément atomique : jamais de stock négatif, même avec des emprunts simultanés
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Livre l SET l.exemplairesDisponibles = l.exemplairesDisponibles - 1 "
            + "WHERE l.id = :id AND l.exemplairesDisponibles > 0")
    int reserver(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Livre l SET l.exemplairesDisponibles = l.exemplairesDisponibles + 1 "
            + "WHERE l.id = :id AND l.exemplairesDisponibles < l.exemplairesTotal")
    int liberer(@Param("id") Long id);
}
