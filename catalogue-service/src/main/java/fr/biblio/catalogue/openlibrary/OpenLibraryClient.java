package fr.biblio.catalogue.openlibrary;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class OpenLibraryClient {

    private static final String CHAMPS = "key,title,author_name,first_publish_year,isbn,cover_i";

    private final RestClient restClient;

    public OpenLibraryClient(RestClient.Builder builder, OpenLibraryProperties proprietes) {
        this.restClient = builder.baseUrl(proprietes.baseUrl()).build();
    }

    public List<DocOpenLibrary> rechercher(String q, int limite) {
        ReponseRecherche reponse = restClient.get()
                .uri(u -> u.path("/search.json")
                        .queryParam("q", q)
                        .queryParam("limit", limite)
                        .queryParam("fields", CHAMPS)
                        .build())
                .retrieve()
                .body(ReponseRecherche.class);
        return reponse == null || reponse.docs() == null ? List.of() : reponse.docs();
    }

    public record ReponseRecherche(List<DocOpenLibrary> docs) {
    }

    public record DocOpenLibrary(
            String key,
            String title,
            @JsonProperty("author_name") List<String> auteurs,
            @JsonProperty("first_publish_year") Integer annee,
            List<String> isbn,
            @JsonProperty("cover_i") Integer couverture) {
    }
}
