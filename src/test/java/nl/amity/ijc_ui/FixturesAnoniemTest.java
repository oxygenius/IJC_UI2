package nl.amity.ijc_ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Controleert dat alle testfixtures geanonimiseerd zijn.
 *
 * De repository is openbaar, dus fixtures mogen geen namen of KNSB-nummers van
 * (jeugd)leden bevatten. Maak fixtures altijd met src/test/tools/anonymize_status.py;
 * dat script geeft spelers de naam "Speler NN" en KNSB-nummer 0.
 */
class FixturesAnoniemTest {

	private static final Path FIXTURES = Path.of("src", "test", "resources", "fixtures");

	static Stream<Path> fixtures() throws IOException {
		List<Path> bestanden;
		try (Stream<Path> paden = Files.walk(FIXTURES)) {
			bestanden = paden.filter(p -> p.toString().endsWith(".json")).toList();
		}
		assertFalse(bestanden.isEmpty(), "Geen fixtures gevonden in " + FIXTURES);
		return bestanden.stream();
	}

	@ParameterizedTest
	@MethodSource("fixtures")
	@DisplayName("Fixture bevat alleen geanonimiseerde spelers")
	void fixtureIsGeanonimiseerd(Path fixture) throws IOException {
		JsonElement root;
		try (Reader reader = Files.newBufferedReader(fixture, StandardCharsets.UTF_8)) {
			root = JsonParser.parseReader(reader);
		}
		List<String> fouten = new ArrayList<>();
		controleer(root, fouten);
		assertTrue(fouten.isEmpty(), fixture + " is niet geanonimiseerd (gebruik anonymize_status.py):\n"
				+ String.join("\n", fouten.subList(0, Math.min(10, fouten.size()))));
	}

	/**
	 * Loopt recursief door de JSON en controleert elk spelerobject (een object met "naam").
	 * Bij een fout wordt alleen het soort fout gemeld, niet de naam zelf.
	 */
	private static void controleer(JsonElement e, List<String> fouten) {
		if (e.isJsonArray()) {
			e.getAsJsonArray().forEach(kind -> controleer(kind, fouten));
		} else if (e.isJsonObject()) {
			JsonObject o = e.getAsJsonObject();
			if (o.has("naam")) {
				String id = o.has("id") ? "speler id " + o.get("id").getAsString() : "speler zonder id";
				if (!o.get("naam").getAsString().matches("Speler \\d{2,}")) {
					fouten.add(id + ": naam is niet van de vorm 'Speler NN'");
				}
				if (o.has("KNSBnummer") && o.get("KNSBnummer").getAsLong() != 0) {
					fouten.add(id + ": KNSBnummer is niet 0");
				}
			}
			o.entrySet().forEach(kind -> controleer(kind.getValue(), fouten));
		}
	}
}
