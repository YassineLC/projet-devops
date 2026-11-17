package fr.biblio.emprunts.emprunt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NouvelEmprunt(@NotNull Long livreId, @NotBlank @Size(max = 80) String emprunteur) {
}
