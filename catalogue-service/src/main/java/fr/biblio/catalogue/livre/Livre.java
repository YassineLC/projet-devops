package fr.biblio.catalogue.livre;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "livres")
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cleOpenLibrary;

    private String isbn;

    @Column(nullable = false)
    private String titre;

    private String auteurs;

    private Integer annee;

    private String couvertureUrl;

    private int exemplairesTotal;

    private int exemplairesDisponibles;

    protected Livre() {
    }

    public Livre(String cleOpenLibrary, String isbn, String titre, String auteurs,
                 Integer annee, String couvertureUrl, int exemplaires) {
        this.cleOpenLibrary = cleOpenLibrary;
        this.isbn = isbn;
        this.titre = titre;
        this.auteurs = auteurs;
        this.annee = annee;
        this.couvertureUrl = couvertureUrl;
        this.exemplairesTotal = exemplaires;
        this.exemplairesDisponibles = exemplaires;
    }

    public Long getId() { return id; }
    public String getCleOpenLibrary() { return cleOpenLibrary; }
    public String getIsbn() { return isbn; }
    public String getTitre() { return titre; }
    public String getAuteurs() { return auteurs; }
    public Integer getAnnee() { return annee; }
    public String getCouvertureUrl() { return couvertureUrl; }
    public int getExemplairesTotal() { return exemplairesTotal; }
    public int getExemplairesDisponibles() { return exemplairesDisponibles; }
}
