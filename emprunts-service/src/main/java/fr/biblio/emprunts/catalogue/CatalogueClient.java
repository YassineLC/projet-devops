package fr.biblio.emprunts.catalogue;

import fr.biblio.grpc.LivreInfo;

/**
 * Accès au catalogue. Lève io.grpc.StatusRuntimeException en cas d'erreur.
 */
public interface CatalogueClient {

    LivreInfo reserver(long livreId);

    LivreInfo liberer(long livreId);
}
