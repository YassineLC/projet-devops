package fr.biblio.catalogue.grpc;

import fr.biblio.catalogue.livre.Livre;
import fr.biblio.catalogue.livre.LivreIntrouvableException;
import fr.biblio.catalogue.livre.LivreService;
import fr.biblio.catalogue.livre.StockEpuiseException;
import fr.biblio.grpc.CatalogueGrpc;
import fr.biblio.grpc.LivreId;
import fr.biblio.grpc.LivreInfo;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class CatalogueGrpcService extends CatalogueGrpc.CatalogueImplBase {

    private final LivreService livreService;

    public CatalogueGrpcService(LivreService livreService) {
        this.livreService = livreService;
    }

    @Override
    public void getLivre(LivreId requete, StreamObserver<LivreInfo> reponse) {
        repondre(reponse, () -> livreService.trouver(requete.getId()));
    }

    @Override
    public void reserverExemplaire(LivreId requete, StreamObserver<LivreInfo> reponse) {
        repondre(reponse, () -> livreService.reserver(requete.getId()));
    }

    @Override
    public void libererExemplaire(LivreId requete, StreamObserver<LivreInfo> reponse) {
        repondre(reponse, () -> livreService.liberer(requete.getId()));
    }

    private void repondre(StreamObserver<LivreInfo> reponse, Supplier<Livre> action) {
        try {
            Livre livre = action.get();
            reponse.onNext(LivreInfo.newBuilder()
                    .setId(livre.getId())
                    .setTitre(livre.getTitre())
                    .setExemplairesDisponibles(livre.getExemplairesDisponibles())
                    .build());
            reponse.onCompleted();
        } catch (LivreIntrouvableException e) {
            reponse.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (StockEpuiseException e) {
            reponse.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
