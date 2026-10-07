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
import nl.amity.ijc_ui.data.groepen.Speler;
import nl.amity.ijc_ui.data.wedstrijden.Groepswedstrijden;
import nl.amity.ijc_ui.data.wedstrijden.Serie;
import nl.amity.ijc_ui.data.wedstrijden.Wedstrijd;
import nl.amity.ijc_ui.data.wedstrijden.Wedstrijden;

/**
 * Tests voor de fuzzy indeling.
 *
 * De golden tests gebruiken geanonimiseerde status bestanden van echt gespeelde
 * rondes (zie src/test/tools/anonymize_status.py). Uit de 'wedstrijdgroepen' wordt
 * opnieuw een indeling gemaakt die gelijk moet zijn aan de opgeslagen 'wedstrijden'.
 */
class GroepenIndelerFuzzyTest {

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
		c.fuzzyIndeling = true;
		c.fuzzyOneven = true;
		c.fuzzyRanglijstpunten = true;
		c.fuzzyWegingAndereTegenstander = 1.0;
		c.fuzzyWegingAfstandRanglijst = 1.0;
		c.fuzzyWegingAfstandRanglijstpunten = 0.98;
		c.fuzzyWegingZwartWitVerdeling = 1.0;
		c.fuzzyWegingDoorschuiverEigenGroep = 1.0;
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
		c.fuzzyIndeling = origineel.fuzzyIndeling;
		c.fuzzyOneven = origineel.fuzzyOneven;
		c.fuzzyRanglijstpunten = origineel.fuzzyRanglijstpunten;
		c.fuzzyWegingAndereTegenstander = origineel.fuzzyWegingAndereTegenstander;
		c.fuzzyWegingAfstandRanglijst = origineel.fuzzyWegingAfstandRanglijst;
		c.fuzzyWegingAfstandRanglijstpunten = origineel.fuzzyWegingAfstandRanglijstpunten;
		c.fuzzyWegingZwartWitVerdeling = origineel.fuzzyWegingZwartWitVerdeling;
		c.fuzzyWegingDoorschuiverEigenGroep = origineel.fuzzyWegingDoorschuiverEigenGroep;
	}

	// ---------------------------------------------------------------------
	// Golden tests op basis van gespeelde rondes
	// ---------------------------------------------------------------------

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "P1R4", "P1R5", "P1R6" })
	@DisplayName("Indeling is gelijk aan de indeling van de gespeelde ronde")
	void indelingGelijkAanGespeeldeRonde(String fixture) throws Exception {
		Status status = leesFixture(fixture);

		Wedstrijden gemaakt = new GroepenIndelerFuzzy().maakWedstrijdschema(status.wedstrijdgroepen);

		for (Groepswedstrijden verwacht : status.wedstrijden.getGroepswedstrijden()) {
			Groepswedstrijden werkelijk = gemaakt.getGroepswedstrijdenNiveau(verwacht.getNiveau());
			assertNotNull(werkelijk, "Geen wedstrijden gemaakt voor groep " + verwacht.getNiveau());
			assertEquals(beschrijf(verwacht), beschrijf(werkelijk),
					fixture + ": afwijkende indeling voor groep " + verwacht.getNiveau());
		}
		assertEquals(status.wedstrijden.getGroepswedstrijden().size(), gemaakt.getGroepswedstrijden().size());
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "P1R4", "P1R5", "P1R6" })
	@DisplayName("Iedere speler wordt ingedeeld en speelt niet tegen zichzelf")
	void iedereSpelerIngedeeld(String fixture) throws Exception {
		Status status = leesFixture(fixture);

		for (Groep groep : status.wedstrijdgroepen.getGroepen()) {
			if (groep.getAantalSpelers() < 2) continue;
			Groepswedstrijden gws = new GroepenIndelerFuzzy().maakWedstrijdenVoorGroep(
					status.wedstrijdgroepen.getPeriode(), status.wedstrijdgroepen.getRonde(), groep);
			controleerIndeling(groep, gws, fixture + " groep " + groep.getNiveau());
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

		new GroepenIndelerFuzzy().maakWedstrijdschema(status.wedstrijdgroepen);

		for (Groep groep : status.wedstrijdgroepen.getGroepen()) {
			assertEquals(voor.get(groep.getNiveau()), toestand(groep), "Groep " + groep.getNiveau() + " gewijzigd");
		}
	}

	// ---------------------------------------------------------------------
	// Regels van de indeling met zelf opgebouwde groepen
	// ---------------------------------------------------------------------

	@ParameterizedTest(name = "{0} spelers")
	@ValueSource(ints = { 2, 4, 6, 8, 10, 14 })
	@DisplayName("Even aantal spelers: iedere speler speelt een keer per serie")
	void evenAantalSpelers(int aantal) {
		Groep groep = maakGroep(1, aantal);
		// Groep 1 speelt in ronde 2 twee series, geen doorschuivers
		Groepswedstrijden gws = new GroepenIndelerFuzzy().maakWedstrijdenVoorGroep(1, 2, groep);

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
	@ValueSource(ints = { 3, 5, 7, 9, 13 })
	@DisplayName("Oneven aantal spelers (fuzzyOneven): iedere speler speelt twee keer")
	void onevenAantalSpelers(int aantal) {
		Groep groep = maakGroep(1, aantal);
		Groepswedstrijden gws = new GroepenIndelerFuzzy().maakWedstrijdenVoorGroep(1, 2, groep);

		// De drie series worden samengevoegd tot een serie
		assertEquals(1, gws.getSeries().size());
		assertEquals(aantal, gws.getWedstrijden().size());
		Map<Integer, Integer> partijen = telPartijen(gws);
		for (Speler s : groep.getSpelers()) {
			assertEquals(2, partijen.getOrDefault(s.getId(), 0), "Aantal partijen voor " + s.getNaam());
		}
		controleerIndeling(groep, gws, aantal + " spelers");
	}

	@Test
	@DisplayName("Vijf spelers zonder fuzzyOneven: vijf partijen, iedere speler twee keer")
	void vijfSpelersZonderFuzzyOneven() {
		IJCController.c().fuzzyOneven = false;
		Groep groep = maakGroep(1, 5);
		Groepswedstrijden gws = new GroepenIndelerFuzzy().maakWedstrijdenVoorGroep(1, 2, groep);

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
		zetTegenstanders(groep, 1, "-- ", "-- ", "-- ", "AB+");
		zetTegenstanders(groep, 2, "-- ", "-- ", "-- ", "AA-");
		zetTegenstanders(groep, 3, "-- ", "-- ", "-- ", "AD=");
		zetTegenstanders(groep, 4, "-- ", "-- ", "-- ", "AC=");
		// Groep 2 speelt in ronde 2 een serie
		Groepswedstrijden gws = new GroepenIndelerFuzzy().maakWedstrijdenVoorGroep(1, 2, groep);

		assertEquals(2, gws.getWedstrijden().size());
		for (Wedstrijd w : gws.getWedstrijden()) {
			Set<Integer> paar = Set.of(w.getWit().getId(), w.getZwart().getId());
			assertFalse(paar.equals(Set.of(1, 2)) || paar.equals(Set.of(3, 4)), "Herhaling van vorige ronde: " + w);
		}
	}

	@Test
	@DisplayName("Speler met witvoorkeur krijgt wit tegen speler met zwartvoorkeur")
	void kleurverdeling() {
		Groep groep = maakGroep(2, 2);
		groep.getSpelerByID(1).setWitvoorkeur(-1);
		groep.getSpelerByID(2).setWitvoorkeur(1);
		Groepswedstrijden gws = new GroepenIndelerFuzzy().maakWedstrijdenVoorGroep(1, 2, groep);

		assertEquals(1, gws.getWedstrijden().size());
		Wedstrijd w = gws.getWedstrijden().get(0);
		assertEquals(2, w.getWit().getId(), "Speler met witvoorkeur moet wit hebben: " + w);
	}

	// ---------------------------------------------------------------------
	// Hulpfuncties
	// ---------------------------------------------------------------------

	private static Status leesFixture(String naam) throws Exception {
		try (Reader r = new InputStreamReader(
				GroepenIndelerFuzzyTest.class.getResourceAsStream("/fixtures/fuzzy/" + naam + ".json"),
				StandardCharsets.UTF_8)) {
			return new Gson().fromJson(r, Status.class);
		}
	}

	/**
	 * Algemene eisen aan iedere indeling: geen partij tegen zichzelf, alleen spelers
	 * uit de groep, iedere speler speelt minstens een keer en geen paar speelt twee
	 * keer tegen elkaar (behalve bij twee spelers, dan in ieder geval met andere kleur).
	 */
	private static void controleerIndeling(Groep groep, Groepswedstrijden gws, String context) {
		Set<Integer> ids = new HashSet<>();
		for (Speler s : groep.getSpelers()) ids.add(s.getId());

		Set<Set<Integer>> paren = new HashSet<>();
		Set<List<Integer>> kleuren = new HashSet<>();
		for (Wedstrijd w : alleWedstrijden(gws)) {
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

	private static List<Wedstrijd> alleWedstrijden(Groepswedstrijden gws) {
		List<Wedstrijd> result = new ArrayList<>(gws.getWedstrijden());
		if (gws.getTriowedstrijden() != null) result.addAll(gws.getTriowedstrijden());
		return result;
	}

	private static Map<Integer, Integer> telPartijen(Groepswedstrijden gws) {
		Map<Integer, Integer> partijen = new HashMap<>();
		for (Wedstrijd w : alleWedstrijden(gws)) {
			partijen.merge(w.getWit().getId(), 1, Integer::sum);
			partijen.merge(w.getZwart().getId(), 1, Integer::sum);
		}
		return partijen;
	}

	/** Leesbare weergave per serie (wit-zwart op initialen) voor vergelijking */
	private static String beschrijf(Groepswedstrijden gws) {
		StringBuilder sb = new StringBuilder();
		for (Serie serie : gws.getSeries()) {
			sb.append("serie:");
			for (Wedstrijd w : serie.getWedstrijden()) {
				sb.append(' ').append(w.getWit().getInitialen()).append('-').append(w.getZwart().getInitialen());
			}
			sb.append('\n');
		}
		if (gws.getTriowedstrijden() != null && !gws.getTriowedstrijden().isEmpty()) {
			sb.append("trio:");
			for (Wedstrijd w : gws.getTriowedstrijden()) {
				sb.append(' ').append(w.getWit().getInitialen()).append('-').append(w.getZwart().getInitialen());
			}
			sb.append('\n');
		}
		return sb.toString();
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
	 * Maak een groep met spelers met id 1..aantal, initialen AA, AB, ... en
	 * aflopende punten zodat de ranglijst gelijk is aan de volgorde.
	 */
	private static Groep maakGroep(int niveau, int aantal) {
		Groep groep = new Groep(niveau);
		for (int i = 1; i <= aantal; i++) {
			String initialen = "A" + (char) ('A' + i - 1);
			Speler s = new Speler(i, String.format("Speler %02d", i), initialen, 0, niveau, 1000 - i,
					new String[] { "-- ", "-- ", "-- ", "-- " }, 100 - i, false, true, 0, 0, 0,
					"-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- ");
			groep.addSpeler(s);
		}
		return groep;
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
		k.fuzzyIndeling = c.fuzzyIndeling;
		k.fuzzyOneven = c.fuzzyOneven;
		k.fuzzyRanglijstpunten = c.fuzzyRanglijstpunten;
		k.fuzzyWegingAndereTegenstander = c.fuzzyWegingAndereTegenstander;
		k.fuzzyWegingAfstandRanglijst = c.fuzzyWegingAfstandRanglijst;
		k.fuzzyWegingAfstandRanglijstpunten = c.fuzzyWegingAfstandRanglijstpunten;
		k.fuzzyWegingZwartWitVerdeling = c.fuzzyWegingZwartWitVerdeling;
		k.fuzzyWegingDoorschuiverEigenGroep = c.fuzzyWegingDoorschuiverEigenGroep;
		return k;
	}
}
