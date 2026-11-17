package fr.biblio.catalogue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "openlibrary.import-au-demarrage=false")
class CatalogueServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
