package nl.amity.ijc_ui.ui.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.gson.Gson;

import nl.amity.ijc_ui.Configuratie;
import nl.amity.ijc_ui.data.groepen.Groep;
import nl.amity.ijc_ui.data.groepen.Groepen;
import nl.amity.ijc_ui.data.groepen.Speler;
import nl.amity.ijc_ui.data.wedstrijden.Groepswedstrijden;
import nl.amity.ijc_ui.data.wedstrijden.Serie;
import nl.amity.ijc_ui.data.wedstrijden.Wedstrijd;
import nl.amity.ijc_ui.data.wedstrijden.Wedstrijden;

/**
 * Tests voor de klassieke (niet fuzzy) indeling.
 *
 * De gespeelde rondes in de fixtures zijn met de fuzzy indeling gemaakt, dus de
 * opgeslagen wedstrijden zijn geen golden test voor deze indeler. De
 * 'wedstrijdgroepen' worden alleen als invoer gebruikt en de gemaakte indeling
 * wordt op algemene regels gecontroleerd.
 */
class GroepenIndelerTest {

	private Configuratie origineel;

	@BeforeEach
	void configureerClubinstellingen() {
		origineel = kopie(IJCController.c());
		Configuratie c = IJCController.c();
		// Instellingen zoals gebruikt bij het maken van de fixtures (configuratie.json)
		c.perioden = 4;
		c.rondes = 8;
		c.aantalGroepen = 4;
		c.groepsnamen = new String[] { "Pionnengroep", "Paardengroep", "Lopergroep", "Koninggroep", "", "", "", "", "", "" };
		c.grAantalSeries = "if ((y ==1) && (z==1)) {if ((x ==0) || (x==1)) { return 2 } else { return 2 }} else if ((x ==0) || (x==1)) { return 2 } else { return 1 }";
		c.grAantalDoorschuivers = "if (z >= 4) { if (z < 8) { return 3 } else {  return 0 } } else { return 0 }";
		c.grSorteerOpRating = "if ((x == 6) && (z > 1) && (z < 7)) { true } else { false }";
		c.grBeginTrio = "if (x > 0) { x / 2 } else { 0 }";
		c.fuzzyIndeling = false;
		c.specialeIndelingEersteRonde = true;
		c.indelingMaximumVerschil = 3;
		c.laasteRondeDoorschuivenAltijd = false;
	}

	@AfterEach
	void herstelConfiguratie() {
		Configuratie c = IJCController.c();
		c.perioden = origineel.perioden;
		c.rondes = origineel.rondes;
		c.aantalGroepen = origineel.aantalGroepen;
		c.groepsnamen = origineel.groepsnamen;
		c.grAantalSeries = origineel.grAantalSeries;
		c.grAantalDoorschuivers = origineel.grAantalDoorschuivers;
		c.grSorteerOpRating = origineel.grSorteerOpRating;
		c.grBeginTrio = origineel.grBeginTrio;
		c.fuzzyIndeling = origineel.fuzzyIndeling;
		c.specialeIndelingEersteRonde = origineel.specialeIndelingEersteRonde;
		c.indelingMaximumVerschil = origineel.indelingMaximumVerschil;
		c.laasteRondeDoorschuivenAltijd = origineel.laasteRondeDoorschuivenAltijd;
	}

	// ---------------------------------------------------------------------
	// Tests op basis van gespeelde rondes
	// ---------------------------------------------------------------------

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "P1R4", "P1R5", "P1R6" })
	@DisplayName("Iedere speler wordt ingedeeld en speelt niet tegen zichzelf")
	void iedereSpelerIngedeeld(String fixture) throws Exception {
		Status status = leesFixture(fixture);

		for (Groep groep : status.wedstrijdgroepen.getGroepen()) {
			if (groep.getAantalSpelers() < 2) continue;
			Groepswedstrijden gws = new GroepenIndeler().maakWedstrijdenVoorGroep(
					status.wedstrijdgroepen.getPeriode(), status.wedstrijdgroepen.getRonde(), groep);
			controleerIndeling(groep, gws, fixture + " groep " + groep.getNiveau());
		}
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "P1R4", "P1R5", "P1R6" })
	@DisplayName("Wedstrijdschema bevat een indeling voor iedere groep")
	void wedstrijdschemaVoorIedereGroep(String fixture) throws Exception {
		Status status = leesFixture(fixture);

		Wedstrijden gemaakt = new GroepenIndeler().maakWedstrijdschema(status.wedstrijdgroepen);

		assertEquals(status.wedstrijdgroepen.getPeriode(), gemaakt.getPeriode());
		assertEquals(status.wedstrijdgroepen.getRonde(), gemaakt.getRonde());
		for (Groep groep : status.wedstrijdgroepen.getGroepen()) {
			if (groep.getAantalSpelers() < 2) continue;
			assertNotNull(gemaakt.getGroepswedstrijdenNiveau(groep.getNiveau()),
					fixture + ": geen wedstrijden voor groep " + groep.getNiveau());
		}
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "P1R4", "P1R5", "P1R6" })
	@DisplayName("Indelen wijzigt de wedstrijdgroep niet")
	void wedstrijdgroepOngewijzigd(String fixture) throws Exception {
		Status status = leesFixture(fixture);
		Map<Integer, String> voor = new HashMap<>();
		for (Groep groep : status.wedstrijdgroepen.getGroepen()) {
			voor.put(groep.getNiveau(), toestand(groep));
		}

		new GroepenIndeler().maakWedstrijdschema(status.wedstrijdgroepen);

		for (Groep groep : status.wedstrijdgroepen.getGroepen()) {
			assertEquals(voor.get(groep.getNiveau()), toestand(groep), "Groep " + groep.getNiveau() + " gewijzigd");
		}
	}

	// ---------------------------------------------------------------------
	// Regels van de indeling met zelf opgebouwde groepen
	// ---------------------------------------------------------------------

	@ParameterizedTest(name = "{0} spelers")
	@ValueSource(ints = { 6, 8, 10, 14 })
	@DisplayName("Even aantal spelers: iedere speler speelt een keer per serie")
	void evenAantalSpelers(int aantal) {
		controleerTweeSeries(aantal);
	}

	@ParameterizedTest(name = "{0} spelers")
	@ValueSource(ints = { 2, 4 })
	@DisplayName("Kleine groep: iedere speler speelt een keer per serie")
	void kleineGroepKrijgtAlleSeries(int aantal) {
		controleerTweeSeries(aantal);
	}

	private static void controleerTweeSeries(int aantal) {
		Groep groep = maakGroep(1, aantal);
		// Groep 1 speelt in ronde 2 twee series, geen doorschuivers
		Groepswedstrijden gws = new GroepenIndeler().maakWedstrijdenVoorGroep(1, 2, groep);

		assertEquals(2, gws.getSeries().size());
		for (Serie serie : gws.getSeries()) {
			assertEquals(aantal / 2, serie.getWedstrijden().size());
			Set<Integer> spelers = new HashSet<>();
			for (Wedstrijd w : serie.getWedstrijden()) {
				assertTrue(spelers.add(w.getWit().getId()), "Speler dubbel in serie: " + w);
				assertTrue(spelers.add(w.getZwart().getId()), "Speler dubbel in serie: " + w);
			}
			assertEquals(aantal, spelers.size());
		}
		controleerIndeling(groep, gws, aantal + " spelers");
	}

	@ParameterizedTest(name = "{0} spelers")
	@ValueSource(ints = { 3, 7, 9, 13 })
	@DisplayName("Oneven aantal spelers bij twee series: volledig trio, iedere speler speelt twee keer")
	void onevenAantalSpelersTweeSeries(int aantal) {
		Groep groep = maakGroep(1, aantal);
		Groepswedstrijden gws = new GroepenIndeler().maakWedstrijdenVoorGroep(1, 2, groep);

		assertEquals(3, gws.getTriowedstrijden().size());
		Map<Integer, Integer> partijen = telPartijen(gws);
		for (Speler s : groep.getSpelers()) {
			assertEquals(2, partijen.getOrDefault(s.getId(), 0), "Aantal partijen voor " + s.getNaam());
		}
		controleerIndeling(groep, gws, aantal + " spelers");
	}

	@ParameterizedTest(name = "{0} spelers")
	@ValueSource(ints = { 3, 5, 7, 9 })
	@DisplayName("Oneven aantal spelers bij een serie: half trio, iedere speler speelt een keer")
	void onevenAantalSpelersEenSerie(int aantal) {
		Groep groep = maakGroep(2, aantal);
		// Groep 2 speelt in ronde 2 een serie
		Groepswedstrijden gws = new GroepenIndeler().maakWedstrijdenVoorGroep(1, 2, groep);

		List<Wedstrijd> trio = gws.getTriowedstrijden();
		assertEquals(2, trio.size());
		long eersteBeschikbaar = trio.stream()
				.filter(w -> isEersteBeschikbaar(w.getWit()) || isEersteBeschikbaar(w.getZwart())).count();
		assertEquals(1, eersteBeschikbaar, "Precies een trio partij tegen 'Eerste beschikbaar'");
		assertEquals((aantal - 3) / 2 + 2, gws.getWedstrijden().size());
		Map<Integer, Integer> partijen = telPartijen(gws);
		for (Speler s : groep.getSpelers()) {
			assertEquals(1, partijen.getOrDefault(s.getId(), 0), "Aantal partijen voor " + s.getNaam());
		}
		controleerIndeling(groep, gws, aantal + " spelers");
	}

	@Test
	@DisplayName("Vijf spelers bij twee series: vijf partijen, iedere speler twee keer")
	void vijfSpelersTweeSeries() {
		Groep groep = maakGroep(1, 5);
		Groepswedstrijden gws = new GroepenIndeler().maakWedstrijdenVoorGroep(1, 2, groep);

		// Spelers worden verdubbeld en er wordt een serie gepland
		assertEquals(1, gws.getSeries().size());
		assertEquals(5, gws.getWedstrijden().size());
		Map<Integer, Integer> partijen = telPartijen(gws);
		for (Speler s : groep.getSpelers()) {
			assertEquals(2, partijen.getOrDefault(s.getId(), 0), "Aantal partijen voor " + s.getNaam());
		}
		controleerIndeling(groep, gws, "5 spelers");
	}

	@Test
	@DisplayName("Niet opnieuw tegen de tegenstander van vorige ronde")
	void nietTegenVorigeTegenstander() {
		Groep groep = maakGroep(2, 4);
		// Vorige ronde: 1-2 en 3-4
		zetTegenstanders(groep, 1, "-- ", "-- ", "-- ", "CB+");
		zetTegenstanders(groep, 2, "-- ", "-- ", "-- ", "CA-");
		zetTegenstanders(groep, 3, "-- ", "-- ", "-- ", "CD=");
		zetTegenstanders(groep, 4, "-- ", "-- ", "-- ", "CC=");
		Groepswedstrijden gws = new GroepenIndeler().maakWedstrijdenVoorGroep(1, 2, groep);

		assertEquals(2, gws.getWedstrijden().size());
		for (Wedstrijd w : gws.getWedstrijden()) {
			Set<Integer> paar = Set.of(w.getWit().getId(), w.getZwart().getId());
			assertFalse(paar.equals(Set.of(1, 2)) || paar.equals(Set.of(3, 4)), "Herhaling van vorige ronde: " + w);
		}
	}

	@Test
	@DisplayName("Speler met witvoorkeur krijgt wit tegen speler met zwartvoorkeur")
	void kleurverdeling() {
		Groep groep = maakGroep(2, 6);
		groep.getSpelerByID(1).setWitvoorkeur(-1);
		groep.getSpelerByID(2).setWitvoorkeur(1);
		// Groep 2 speelt in ronde 2 een serie; met minimaal verschil 1 spelen 1 en 2 tegen elkaar
		Groepswedstrijden gws = new GroepenIndeler().maakWedstrijdenVoorGroep(1, 2, groep);

		assertEquals(3, gws.getWedstrijden().size());
		Wedstrijd w = gws.getSeries().get(0).getWedstrijden().get(0);
		assertEquals(Set.of(1, 2), Set.of(w.getWit().getId(), w.getZwart().getId()), "Onverwacht paar: " + w);
		assertEquals(2, w.getWit().getId(), "Speler met witvoorkeur moet wit hebben: " + w);
	}

	// ---------------------------------------------------------------------
	// Groepsindeling en doorschuiven
	// ---------------------------------------------------------------------

	@Test
	@DisplayName("Zonder doorschuiven worden alleen afwezige spelers verwijderd")
	void groepsindelingZonderDoorschuiven() {
		Groepen aanwezigheid = maakGroepen(2, 6, 4);
		aanwezigheid.getGroepByNiveau(0).getSpelerByID(3).setAanwezig(false);

		Groepen gemaakt = new GroepenIndeler().maakGroepsindeling(aanwezigheid);

		assertEquals("AA0 AB0 AD0 AE0 AF0", samenstelling(gemaakt.getGroepByNiveau(0)));
		assertEquals("BA1 BB1 BC1 BD1", samenstelling(gemaakt.getGroepByNiveau(1)));
		assertEquals(List.of(1, 2, 3, 4, 5), ids(gemaakt.getGroepByNiveau(0)), "Spelers niet hernummerd");
	}

	@Test
	@DisplayName("Derde doorschuiver schuift door als de hogere groep anders oneven blijft")
	void derdeDoorschuiverBijOnevenGroep() {
		Groepen aanwezigheid = maakGroepen(4, 6, 5);

		Groepen gemaakt = new GroepenIndeler().maakGroepsindeling(aanwezigheid);

		assertEquals("AD0 AE0 AF0", samenstelling(gemaakt.getGroepByNiveau(0)));
		assertEquals("BA1 BB1 BC1 BD1 BE1 AA0 AB0 AC0", samenstelling(gemaakt.getGroepByNiveau(1)));
		assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), ids(gemaakt.getGroepByNiveau(1)), "Spelers niet hernummerd");
	}

	@Test
	@DisplayName("Derde doorschuiver blijft als de hogere groep al even is")
	void geenDerdeDoorschuiverBijEvenGroep() {
		Groepen aanwezigheid = maakGroepen(4, 6, 4);

		Groepen gemaakt = new GroepenIndeler().maakGroepsindeling(aanwezigheid);

		assertEquals("AC0 AD0 AE0 AF0", samenstelling(gemaakt.getGroepByNiveau(0)));
		assertEquals("BA1 BB1 BC1 BD1 AA0 AB0", samenstelling(gemaakt.getGroepByNiveau(1)));
	}

	@Test
	@DisplayName("Afwezige speler schuift niet door")
	void afwezigeSpelerSchuiftNietDoor() {
		Groepen aanwezigheid = maakGroepen(4, 6, 5);
		aanwezigheid.getGroepByNiveau(0).getSpelerByID(2).setAanwezig(false);

		Groepen gemaakt = new GroepenIndeler().maakGroepsindeling(aanwezigheid);

		// Plaats 2 wordt niet opgevuld; plaats 3 schuift niet door omdat de hogere groep al even is
		assertEquals("AC0 AD0 AE0 AF0", samenstelling(gemaakt.getGroepByNiveau(0)));
		assertEquals("BA1 BB1 BC1 BD1 BE1 AA0", samenstelling(gemaakt.getGroepByNiveau(1)));
	}

	@Test
	@DisplayName("Doorgeschoven spelers worden tegen een speler uit de hogere groep ingedeeld")
	void doorschuiverTegenEigenGroepVermeden() {
		Groepen aanwezigheid = maakGroepen(4, 6, 4);
		Groepen wedstrijdgroepen = new GroepenIndeler().maakGroepsindeling(aanwezigheid);
		Groep hoger = wedstrijdgroepen.getGroepByNiveau(1);

		// Groep 1 speelt in ronde 4 twee series
		Groepswedstrijden gws = new GroepenIndeler().maakWedstrijdenVoorGroep(1, 4, hoger);

		controleerIndeling(hoger, gws, "doorschuivers");
		for (Wedstrijd w : gws.getSeries().get(0).getWedstrijden()) {
			assertFalse(w.getWit().getGroep() == 0 && w.getZwart().getGroep() == 0,
					"Doorschuivers tegen elkaar in eerste serie: " + w);
		}
	}

	// ---------------------------------------------------------------------
	// Hulpfuncties
	// ---------------------------------------------------------------------

	private static Status leesFixture(String naam) throws Exception {
		try (Reader r = new InputStreamReader(
				GroepenIndelerTest.class.getResourceAsStream("/fixtures/fuzzy/" + naam + ".json"),
				StandardCharsets.UTF_8)) {
			return new Gson().fromJson(r, Status.class);
		}
	}

	/**
	 * Algemene eisen aan iedere indeling: geen partij tegen zichzelf, alleen spelers
	 * uit de groep (of 'Eerste beschikbaar' in een half trio), iedere speler speelt
	 * minstens een keer en geen paar speelt twee keer tegen elkaar (behalve bij twee
	 * spelers, dan in ieder geval met andere kleur).
	 */
	private static void controleerIndeling(Groep groep, Groepswedstrijden gws, String context) {
		Set<Integer> ids = new HashSet<>();
		for (Speler s : groep.getSpelers()) ids.add(s.getId());

		Set<Set<Integer>> paren = new HashSet<>();
		Set<List<Integer>> kleuren = new HashSet<>();
		for (Wedstrijd w : alleWedstrijden(gws)) {
			if (isEersteBeschikbaar(w.getWit()) || isEersteBeschikbaar(w.getZwart())) continue;
			int wit = w.getWit().getId();
			int zwart = w.getZwart().getId();
			assertTrue(wit != zwart, context + ": speler tegen zichzelf: " + w);
			assertTrue(ids.contains(wit) && ids.contains(zwart), context + ": onbekende speler: " + w);
			if (groep.getAantalSpelers() > 2) {
				assertTrue(paren.add(Set.of(wit, zwart)), context + ": paar speelt twee keer: " + w);
			} else {
				assertTrue(kleuren.add(List.of(wit, zwart)), context + ": twee keer dezelfde kleur: " + w);
			}
		}
		Map<Integer, Integer> partijen = telPartijen(gws);
		for (Speler s : groep.getSpelers()) {
			assertTrue(partijen.getOrDefault(s.getId(), 0) > 0, context + ": niet ingedeeld: " + s.getNaam());
		}
	}

	private static boolean isEersteBeschikbaar(Speler s) {
		return "Eerste beschikbaar".equals(s.getNaam());
	}

	private static List<Wedstrijd> alleWedstrijden(Groepswedstrijden gws) {
		// getWedstrijden bevat zowel de series als de triowedstrijden
		return gws.getWedstrijden();
	}

	private static Map<Integer, Integer> telPartijen(Groepswedstrijden gws) {
		Map<Integer, Integer> partijen = new HashMap<>();
		for (Wedstrijd w : alleWedstrijden(gws)) {
			// 'Eerste beschikbaar' is geen speler uit de groep en telt niet mee
			if (!isEersteBeschikbaar(w.getWit())) partijen.merge(w.getWit().getId(), 1, Integer::sum);
			if (!isEersteBeschikbaar(w.getZwart())) partijen.merge(w.getZwart().getId(), 1, Integer::sum);
		}
		return partijen;
	}

	/** Leesbare weergave van een wedstrijdgroep: initialen plus oorspronkelijke groep */
	private static String samenstelling(Groep groep) {
		StringBuilder sb = new StringBuilder();
		for (Speler s : groep.getSpelers()) {
			if (sb.length() > 0) sb.append(' ');
			sb.append(s.getInitialen()).append(s.getGroep());
		}
		return sb.toString();
	}

	private static List<Integer> ids(Groep groep) {
		List<Integer> result = new ArrayList<>();
		for (Speler s : groep.getSpelers()) result.add(s.getId());
		return result;
	}

	private static String toestand(Groep groep) {
		StringBuilder sb = new StringBuilder();
		for (Speler s : groep.getSpelers()) {
			sb.append(s.getId()).append(s.getInitialen()).append(s.getWitvoorkeur())
					.append(s.getTegenstandersString()).append(s.getGroep()).append(';');
		}
		return sb.toString();
	}

	/**
	 * Maak een groep met spelers met id 1..aantal, initialen per groep uniek
	 * (groep 0: AA, AB, ...; groep 1: BA, BB, ...) en aflopende punten zodat de
	 * ranglijst gelijk is aan de volgorde.
	 */
	private static Groep maakGroep(int niveau, int aantal) {
		Groep groep = new Groep(niveau);
		for (int i = 1; i <= aantal; i++) {
			String initialen = "" + (char) ('A' + niveau) + (char) ('A' + i - 1);
			Speler s = new Speler(i, String.format("Speler %d-%02d", niveau, i), initialen, 0, niveau, 1000 - i,
					new String[] { "-- ", "-- ", "-- ", "-- " }, 100 - i, false, true, 0, 0, 0,
					"-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- ");
			groep.addSpeler(s);
		}
		return groep;
	}

	/** Maak aanwezigheidsgroepen voor periode 1 en de gegeven ronde, groepsgroottes van laag naar hoog */
	private static Groepen maakGroepen(int ronde, int... aantallen) {
		Groepen groepen = new Groepen();
		groepen.setPeriode(1);
		groepen.setRonde(ronde);
		for (int niveau = 0; niveau < aantallen.length; niveau++) {
			groepen.addGroep(maakGroep(niveau, aantallen[niveau]));
		}
		return groepen;
	}

	private static void zetTegenstanders(Groep groep, int id, String... tegenstanders) {
		groep.getSpelerByID(id).setTegenstanders(tegenstanders);
	}

	private static Configuratie kopie(Configuratie c) {
		Configuratie k = new Configuratie();
		k.perioden = c.perioden;
		k.rondes = c.rondes;
		k.aantalGroepen = c.aantalGroepen;
		k.groepsnamen = c.groepsnamen;
		k.grAantalSeries = c.grAantalSeries;
		k.grAantalDoorschuivers = c.grAantalDoorschuivers;
		k.grSorteerOpRating = c.grSorteerOpRating;
		k.grBeginTrio = c.grBeginTrio;
		k.fuzzyIndeling = c.fuzzyIndeling;
		k.specialeIndelingEersteRonde = c.specialeIndelingEersteRonde;
		k.indelingMaximumVerschil = c.indelingMaximumVerschil;
		k.laasteRondeDoorschuivenAltijd = c.laasteRondeDoorschuivenAltijd;
		return k;
	}
}
