package nl.amity.ijc_ui.data.wedstrijden;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Controleert welke invoercodes voor een uitslag geaccepteerd worden
 * (uitslagkolom in het hoofdscherm en de dialoog "Uitslagen").
 */
class WedstrijdTest {

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 7, 8, 9 })
	@DisplayName("0/1/2 en reglementair 7/8/9 zijn geldige uitslagcodes")
	void geldigeCodes(int code) {
		assertTrue(Wedstrijd.isGeldigeUitslagcode(code));
	}

	@ParameterizedTest
	@ValueSource(ints = { -1, 3, 4, 5, 6, 10, 12 })
	@DisplayName("Andere codes worden geweigerd")
	void ongeldigeCodes(int code) {
		assertFalse(Wedstrijd.isGeldigeUitslagcode(code));
	}
}
