package fr.biblio.catalogue.livre;

public class StockEpuiseException extends RuntimeException {

    public StockEpuiseException(Long id) {
        super("Plus aucun exemplaire disponible pour le livre " + id);
    }
}
