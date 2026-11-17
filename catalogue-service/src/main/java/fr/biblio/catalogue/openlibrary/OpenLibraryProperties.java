package fr.biblio.catalogue.openlibrary;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("openlibrary")
public record OpenLibraryProperties(
        String baseUrl,
        List<String> requetesInitiales,
        int limite,
        int exemplairesParDefaut,
        boolean importAuDemarrage) {
}
