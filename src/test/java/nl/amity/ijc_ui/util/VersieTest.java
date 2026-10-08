package nl.amity.ijc_ui.util;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Controles op het versienummer en de changelog.
 *
 * Het versienummer staat alleen in pom.xml; Maven vult het in version.properties.
 * Bij elke nieuwe versie hoort een sectie in CHANGELOG.md (zie README, "Een nieuwe versie uitbrengen").
 */
class VersieTest {

	@Test
	@DisplayName("Versienummer wordt door Maven uit pom.xml gevuld")
	void versieIsGevuld() {
		String versie = Versie.get();
		assertNotEquals("onbekend", versie, "version.properties is niet door Maven gevuld");
		assertTrue(versie.matches("\\d+(\\.\\d+)+(-SNAPSHOT)?"), "Onverwacht versienummer: " + versie);
	}

	@Test
	@DisplayName("CHANGELOG.md heeft een sectie voor de huidige versie")
	void changelogHeeftSectieVoorVersie() throws IOException {
		String versie = Versie.get().replace("-SNAPSHOT", "");
		String changelog = Files.readString(Path.of("CHANGELOG.md"), StandardCharsets.UTF_8);
		Pattern sectie = Pattern.compile("^## \\[" + Pattern.quote(versie) + "\\]", Pattern.MULTILINE);
		assertTrue(sectie.matcher(changelog).find(),
				"CHANGELOG.md mist de sectie '## [" + versie + "]'. Hernoem 'Nog niet uitgebracht' naar de nieuwe versie.");
	}
}
