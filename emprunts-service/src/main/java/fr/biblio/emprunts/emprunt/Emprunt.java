package fr.biblio.emprunts.emprunt;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "emprunts")
public class Emprunt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long livreId;

    private String titreLivre;

    @Column(nullable = false)
    private String emprunteur;

    @Column(nullable = false)
    private LocalDate dateEmprunt;

    @Column(nullable = false)
    private LocalDate dateRetourPrevue;

    private LocalDate dateRetour;

    protected Emprunt() {
    }

    public Emprunt(Long livreId, String titreLivre, String emprunteur, LocalDate dateEmprunt, LocalDate dateRetourPrevue) {
        this.livreId = livreId;
        this.titreLivre = titreLivre;
        this.emprunteur = emprunteur;
        this.dateEmprunt = dateEmprunt;
        this.dateRetourPrevue = dateRetourPrevue;
    }

    public Statut getStatut() {
        if (dateRetour != null) {
            return Statut.RENDU;
        }
        return LocalDate.now().isAfter(dateRetourPrevue) ? Statut.EN_RETARD : Statut.EN_COURS;
    }

    void rendre(LocalDate date) {
        this.dateRetour = date;
    }

    public Long getId() { return id; }
    public Long getLivreId() { return livreId; }
    public String getTitreLivre() { return titreLivre; }
    public String getEmprunteur() { return emprunteur; }
    public LocalDate getDateEmprunt() { return dateEmprunt; }
    public LocalDate getDateRetourPrevue() { return dateRetourPrevue; }
    public LocalDate getDateRetour() { return dateRetour; }
}
