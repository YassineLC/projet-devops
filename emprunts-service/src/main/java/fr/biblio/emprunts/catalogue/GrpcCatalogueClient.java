package fr.biblio.emprunts.catalogue;

import fr.biblio.grpc.CatalogueGrpc;
import fr.biblio.grpc.LivreId;
import fr.biblio.grpc.LivreInfo;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class GrpcCatalogueClient implements CatalogueClient {

    private static final long DELAI_SECONDES = 3;

    private final CatalogueGrpc.CatalogueBlockingStub stub;

    public GrpcCatalogueClient(GrpcChannelFactory canaux) {
        this.stub = CatalogueGrpc.newBlockingStub(canaux.createChannel("catalogue"));
    }

    @Override
    public LivreInfo reserver(long livreId) {
        return stub.withDeadlineAfter(DELAI_SECONDES, TimeUnit.SECONDS)
                .reserverExemplaire(LivreId.newBuilder().setId(livreId).build());
    }

    @Override
    public void prechauffer() {
        try {
            // Livre 0 inexistant : seule la connexion compte, NOT_FOUND est attendu
            stub.withDeadlineAfter(15, TimeUnit.SECONDS).getLivre(LivreId.newBuilder().setId(0).build());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() != Status.Code.NOT_FOUND) {
                throw e;
            }
        }
    }

    @Override
    public LivreInfo liberer(long livreId) {
        return stub.withDeadlineAfter(DELAI_SECONDES, TimeUnit.SECONDS)
                .libererExemplaire(LivreId.newBuilder().setId(livreId).build());
    }
}
