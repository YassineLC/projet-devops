package fr.biblio.catalogue.grpc;

import fr.biblio.catalogue.livre.Livre;
import fr.biblio.catalogue.livre.LivreRepository;
import fr.biblio.grpc.CatalogueGrpc;
import fr.biblio.grpc.LivreId;
import fr.biblio.grpc.LivreInfo;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.grpc.test.autoconfigure.AutoConfigureTestGrpcTransport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.client.GrpcChannelFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "openlibrary.import-au-demarrage=false")
@AutoConfigureTestGrpcTransport
class CatalogueGrpcServiceTest {

    @Autowired
    GrpcChannelFactory canaux;

    @Autowired
    LivreRepository repository;

    CatalogueGrpc.CatalogueBlockingStub stub;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        stub = CatalogueGrpc.newBlockingStub(canaux.createChannel("test"));
    }

    private long livre(int exemplaires) {
        return repository.save(new Livre("/works/OL" + System.nanoTime() + "W", null, "Dune", "Frank Herbert",
                1965, null, exemplaires)).getId();
    }

    @Test
    void getLivre_renvoieDisponibilite() {
        long id = livre(3);

        LivreInfo info = stub.getLivre(LivreId.newBuilder().setId(id).build());

        assertThat(info.getTitre()).isEqualTo("Dune");
        assertThat(info.getExemplairesDisponibles()).isEqualTo(3);
    }

    @Test
    void reserver_decremente() {
        long id = livre(3);

        LivreInfo info = stub.reserverExemplaire(LivreId.newBuilder().setId(id).build());

        assertThat(info.getExemplairesDisponibles()).isEqualTo(2);
        assertThat(repository.findById(id).orElseThrow().getExemplairesDisponibles()).isEqualTo(2);
    }

    @Test
    void reserver_sansStock_failedPrecondition() {
        long id = livre(0);

        assertThatThrownBy(() -> stub.reserverExemplaire(LivreId.newBuilder().setId(id).build()))
                .isInstanceOf(StatusRuntimeException.class)
                .extracting(e -> ((StatusRuntimeException) e).getStatus().getCode())
                .isEqualTo(Status.Code.FAILED_PRECONDITION);
    }

    @Test
    void getLivre_inconnu_notFound() {
        assertThatThrownBy(() -> stub.getLivre(LivreId.newBuilder().setId(999999).build()))
                .isInstanceOf(StatusRuntimeException.class)
                .extracting(e -> ((StatusRuntimeException) e).getStatus().getCode())
                .isEqualTo(Status.Code.NOT_FOUND);
    }

    @Test
    void liberer_incremente_sansDepasserLeTotal() {
        long id = livre(2);
        stub.reserverExemplaire(LivreId.newBuilder().setId(id).build());

        assertThat(stub.libererExemplaire(LivreId.newBuilder().setId(id).build()).getExemplairesDisponibles()).isEqualTo(2);
        assertThat(stub.libererExemplaire(LivreId.newBuilder().setId(id).build()).getExemplairesDisponibles()).isEqualTo(2);
    }

    @Test
    void reservations_concurrentes_unSeulSucces() throws Exception {
        long id = livre(1);
        ExecutorService pool = Executors.newFixedThreadPool(10);
        List<Callable<Boolean>> taches = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            taches.add(() -> {
                try {
                    stub.reserverExemplaire(LivreId.newBuilder().setId(id).build());
                    return true;
                } catch (StatusRuntimeException e) {
                    return false;
                }
            });
        }

        long succes = 0;
        for (Future<Boolean> f : pool.invokeAll(taches)) {
            if (f.get()) {
                succes++;
            }
        }
        pool.shutdown();

        assertThat(succes).isEqualTo(1);
        assertThat(repository.findById(id).orElseThrow().getExemplairesDisponibles()).isZero();
    }
}
