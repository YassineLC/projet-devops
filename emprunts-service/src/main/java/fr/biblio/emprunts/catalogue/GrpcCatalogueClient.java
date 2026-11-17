package fr.biblio.emprunts.catalogue;

import fr.biblio.grpc.CatalogueGrpc;
import fr.biblio.grpc.LivreId;
import fr.biblio.grpc.LivreInfo;
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
    public LivreInfo liberer(long livreId) {
        return stub.withDeadlineAfter(DELAI_SECONDES, TimeUnit.SECONDS)
                .libererExemplaire(LivreId.newBuilder().setId(livreId).build());
    }
}
