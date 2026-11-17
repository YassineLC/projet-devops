package fr.biblio.emprunts.emprunt;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpruntRepository extends JpaRepository<Emprunt, Long> {

    List<Emprunt> findByEmprunteurIgnoreCaseOrderByDateEmpruntDesc(String emprunteur);
}
