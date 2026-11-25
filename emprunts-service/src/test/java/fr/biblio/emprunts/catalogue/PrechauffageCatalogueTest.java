package fr.biblio.emprunts.catalogue;

import io.grpc.Status;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PrechauffageCatalogueTest {

    @Test
    void demarrage_prechauffeLeCanalGrpc() {
        CatalogueClient catalogue = mock(CatalogueClient.class);

        new PrechauffageCatalogue(catalogue).run(null);

        verify(catalogue).prechauffer();
    }

    @Test
    void demarrage_catalogueIndisponible_neBloquePas() {
        CatalogueClient catalogue = mock(CatalogueClient.class);
        doThrow(Status.UNAVAILABLE.asRuntimeException()).when(catalogue).prechauffer();

        assertThatCode(() -> new PrechauffageCatalogue(catalogue).run(null)).doesNotThrowAnyException();
    }
}
