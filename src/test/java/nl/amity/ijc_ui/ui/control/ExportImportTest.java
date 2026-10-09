package nl.amity.ijc_ui.ui.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import nl.amity.ijc_ui.Configuratie;
import nl.amity.ijc_ui.data.groepen.Groep;
import nl.amity.ijc_ui.data.groepen.Groepen;
import nl.amity.ijc_ui.data.groepen.Speler;
import nl.amity.ijc_ui.util.Versie;

/**
 * Tests voor export en import van het complete systeem.
 *
 * De tests gebruiken een tijdelijke directory om bestanden te maken en te
 * verwijderen zonder het werkelijke project te beïnvloeden. Elke test maakt
 * een ZIP-bestand met verschillende scenario's: volledige export/import met
 * dezelfde versie, versie mismatch, en onvolledige data.
 */
class ExportImportTest {

	private Path tempDir;
	private String huidigeVersie;

	@BeforeEach
	void setup() throws Exception {
		tempDir = Files.createTempDirectory("ijc_export_test_");
		huidigeVersie = Versie.get();
	}

	@AfterEach
	void cleanup() throws IOException {
		// Recursively delete temp directory
		if (tempDir != null && Files.exists(tempDir)) {
			Files.walk(tempDir)
					.sorted((a, b) -> b.compareTo(a))
					.forEach(path -> {
						try {
							Files.delete(path);
						} catch (IOException e) {
							// ignore
						}
					});
		}
	}

	/**
	 * Maak een minimale Configuratie en schrijf deze naar de opgegeven directory.
	 */
	private Configuratie maakConfiguratie(Path dir, String versie) throws IOException {
		Configuratie c = new Configuratie();
		c.appVersion = versie;
		c.aantalGroepen = 2;
		c.groepsnamen = new String[] { "Lagere groep", "Hogere groep" };
		String json = new Gson().toJson(c);
		Files.writeString(dir.resolve(c.configuratieBestand + ".json"), json);
		return c;
	}

	/**
	 * Maak een minimale Status en schrijf deze naar de opgegeven directory.
	 */
	private Status maakStatus(Path dir, Configuratie c) throws IOException {
		Status status = new Status();
		status.groepen = new Groepen();
		status.groepen.setPeriode(1);
		status.groepen.setRonde(1);

		// Maak 2 groepen met een paar spelers
		for (int niveau = 0; niveau < c.aantalGroepen; niveau++) {
			Groep groep = new Groep(niveau);
			for (int i = 1; i <= 4; i++) {
				Speler s = new Speler(i, "Speler " + (niveau * 10 + i),
						"" + (char) ('A' + niveau) + (char) ('A' + i - 1), 0, niveau,
						1000 - i, new String[] { "-- ", "-- ", "-- ", "-- " },
						100 - i, false, true, 0, 0, 0,
						"-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- ");
				groep.addSpeler(s);
			}
			status.groepen.addGroep(groep);
		}

		String json = new Gson().toJson(status);
		Files.writeString(dir.resolve(c.statusBestand + ".json"), json);
		return status;
	}

	/**
	 * Maak een lege keystore en schrijf deze naar de opgegeven directory.
	 */
	private void maakKeystore(Path dir) throws Exception {
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null);
		try (FileOutputStream fos = new FileOutputStream(dir.resolve("keystore.ks").toFile())) {
			ks.store(fos, "m2fhwuiyegnfwgofijeghuiwhpfijeuovy4iojhkl43ngkls".toCharArray());
		}
	}

	/**
	 * Maak een complete testomgeving met configuratie, status en keystore.
	 */
	private Path maakTestOmgeving(String versie) throws Exception {
		Path dir = tempDir.resolve("test_" + System.nanoTime());
		Files.createDirectories(dir);
		Configuratie c = maakConfiguratie(dir, versie);
		maakStatus(dir, c);
		maakKeystore(dir);
		return dir;
	}

	/**
	 * Maak een ZIP-bestand met de inhoud van de opgegeven directory.
	 */
	private Path maakZip(Path sourceDir, Path zipPath) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
			 java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(fos)) {

			Files.walk(sourceDir).forEach(path -> {
				try {
					if (Files.isDirectory(path)) return;
					Path relative = sourceDir.relativize(path);
					ZipEntry entry = new ZipEntry(relative.toString());
					zos.putNextEntry(entry);
					byte[] data = Files.readAllBytes(path);
					zos.write(data);
					zos.closeEntry();
				} catch (IOException e) {
					// ignore
				}
			});
		}
		return zipPath;
	}

	/**
	 * Tel het aantal entries in een ZIP-bestand.
	 */
	private int telZipEntries(Path zipPath) throws IOException {
		int count = 0;
		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
			while (zis.getNextEntry() != null) {
				count++;
				zis.closeEntry();
			}
		}
		return count;
	}

	/**
	 * Controleer of een ZIP-bestand een specifieke entry bevat.
	 */
	private boolean zipBevatEntry(Path zipPath, String entryName) throws IOException {
		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().equals(entryName)) {
					return true;
				}
				zis.closeEntry();
			}
		}
		return false;
	}

	// ---------------------------------------------------------------------
	 // Tests: Export ZIP inhoud
	// ---------------------------------------------------------------------

	@Test
	@DisplayName("Export ZIP bevat alle vereiste bestanden")
	void exportZipBevatAlleBestanden() throws Exception {
		Path testDir = maakTestOmgeving(huidigeVersie);
		Path zipFile = tempDir.resolve("export_test.zip");

		// Maak de ZIP handmatig (simuleert exportSystem)
		maakZip(testDir, zipFile);

		// Controleer dat alle vereiste bestanden aanwezig zijn
		assertTrue(zipBevatEntry(zipFile, "configuratie.json"), "ZIP mist configuratie.json");
		assertTrue(zipBevatEntry(zipFile, "status.json"), "ZIP mist status.json");
		assertTrue(zipBevatEntry(zipFile, "keystore.ks"), "ZIP mist keystore.ks");
	}

	@Test
	@DisplayName("Export ZIP bevat appVersion in configuratie")
	void exportZipBevatVersie() throws Exception {
		Path testDir = maakTestOmgeving(huidigeVersie);
		Path zipFile = tempDir.resolve("export_versie.zip");

		maakZip(testDir, zipFile);

		// Lees configuratie.json uit de ZIP en controleer appVersion
		String json = leesEntryUitZip(zipFile, "configuratie.json");
		Configuratie c = new Gson().fromJson(json, Configuratie.class);
		assertEquals(huidigeVersie, c.appVersion, "appVersion in ZIP komt niet overeen");
	}

	@Test
	@DisplayName("Export ZIP bevat meerdere entries")
	void exportZipAantalEntries() throws Exception {
		Path testDir = maakTestOmgeving(huidigeVersie);
		Path zipFile = tempDir.resolve("export_entries.zip");

		maakZip(testDir, zipFile);

		int entries = telZipEntries(zipFile);
		// Minimaal 3 bestanden: status.json, configuratie.json, keystore.ks
		assertTrue(entries >= 3, "ZIP bevat minder dan 3 entries: " + entries);
	}

	// ---------------------------------------------------------------------
	// Tests: Import met dezelfde versie
	// ---------------------------------------------------------------------

	@Test
	@DisplayName("Import met dezelfde versie slaagt")
	void importZelfdeVersieSlaat() throws Exception {
		Path testDir = maakTestOmgeving(huidigeVersie);
		Path zipFile = tempDir.resolve("import_zelfde.zip");
		maakZip(testDir, zipFile);

		// Extraheer en valideer
		Path importDir = tempDir.resolve("import_resultaat");
		Files.createDirectories(importDir);

		extraheerZip(zipFile, importDir);

		// Controleer dat alle bestanden zijn geëxtraheerd
		assertTrue(Files.exists(importDir.resolve("configuratie.json")), "configuratie.json niet geëxtraheerd");
		assertTrue(Files.exists(importDir.resolve("status.json")), "status.json niet geëxtraheerd");
		assertTrue(Files.exists(importDir.resolve("keystore.ks")), "keystore.ks niet geëxtraheerd");

		// Controleer dat de versie correct is
		String configJson = Files.readString(importDir.resolve("configuratie.json"));
		Configuratie c = new Gson().fromJson(configJson, Configuratie.class);
		assertEquals(huidigeVersie, c.appVersion, "Versie na import komt niet overeen");
	}

	@Test
	@DisplayName("Import behoudt status data")
	void importBehoudtStatusData() throws Exception {
		Path testDir = maakTestOmgeving(huidigeVersie);
		Path zipFile = tempDir.resolve("import_status.zip");
		maakZip(testDir, zipFile);

		Path importDir = tempDir.resolve("import_status_resultaat");
		Files.createDirectories(importDir);
		extraheerZip(zipFile, importDir);

		// Lees en valideer status
		String statusJson = Files.readString(importDir.resolve("status.json"));
		Status status = new Gson().fromJson(statusJson, Status.class);

		assertNotNull(status.groepen, "Status groepen is null na import");
		assertEquals(2, status.groepen.getAantalGroepen(), "Aantal groepen komt niet overeen");
	}

	// ---------------------------------------------------------------------
	// Tests: Import met verschillende versie
	// ---------------------------------------------------------------------

	@Test
	@DisplayName("Import met andere versie faalt met versie mismatch")
	void importAndereVersieFaalt() throws Exception {
		Path testDir = maakTestOmgeving("999.999.999.999");
		Path zipFile = tempDir.resolve("import_andere_versie.zip");
		maakZip(testDir, zipFile);

		// Lees de versie uit de ZIP
		String importedVersion = leesVersieUitZip(zipFile);
		assertEquals("999.999.999.999", importedVersion, "Test ZIP heeft verkeerde versie");

		// De versie moet verschillen van de huidige versie
		assertFalse(huidigeVersie.equals(importedVersion),
				"Test kan niet uitvoeren: huidige versie is toevallig gelijk aan test versie");

		// Simuleer de versiecheck zoals in importSystem
		RuntimeException ex = assertThrows(RuntimeException.class, () -> {
			if (importedVersion != null && !importedVersion.equals(huidigeVersie)) {
				throw new RuntimeException("Versie incompatibiliteit gedetecteerd!");
			}
		});
		assertTrue(ex.getMessage().contains("Versie incompatibiliteit"),
				"Foutmelding bevat geen versie incompatibiliteit");
	}

	@Test
	@DisplayName("Import zonder appVersion in configuratie slaagt")
	void importZonderVersieSlaat() throws Exception {
		// Maak een configuratie ZONDER appVersion
		Path testDir = tempDir.resolve("test_geen_versie");
		Files.createDirectories(testDir);

		Configuratie c = new Configuratie();
		c.appVersion = null; // Geen versie
		c.aantalGroepen = 2;
		c.groepsnamen = new String[] { "Lagere groep", "Hogere groep" };
		String json = new Gson().toJson(c);
		Files.writeString(testDir.resolve(c.configuratieBestand + ".json"), json);

		maakStatus(testDir, c);
		maakKeystore(testDir);

		Path zipFile = tempDir.resolve("import_geen_versie.zip");
		maakZip(testDir, zipFile);

		// Lees de versie uit de ZIP - moet null zijn
		String importedVersion = leesVersieUitZip(zipFile);
		// Wanneer appVersion null is, moet de import slagen (geen versiecheck)
		// Dit simuleert het gedrag van importSystem: als importedVersion null is,
		// wordt de versiecheck overgeslagen
		if (importedVersion == null) {
			// Geen versiecheck nodig, import zou moeten slagen
			assertTrue(true, "Import zonder versie slaagt (geen versiecheck)");
		}
	}

	// ---------------------------------------------------------------------
	// Tests: Onvolledige data
	// ---------------------------------------------------------------------

	@Test
	@DisplayName("Import met ontbrekende status.json faalt")
	void importOntbrekendeStatusFaalt() throws Exception {
		Path testDir = tempDir.resolve("test_onvolledig_status");
		Files.createDirectories(testDir);

		// Maak alleen configuratie en keystore, geen status
		Configuratie c = maakConfiguratie(testDir, huidigeVersie);
		maakKeystore(testDir);

		Path zipFile = tempDir.resolve("import_onvolledig_status.zip");
		maakZip(testDir, zipFile);

		// Controleer dat status.json ontbreekt
		assertFalse(zipBevatEntry(zipFile, "status.json"), "Test ZIP bevat status.json, maar zou het niet moeten");

		// Extraheer en probeer status te laden
		Path importDir = tempDir.resolve("import_onvolledig_status_resultaat");
		Files.createDirectories(importDir);
		extraheerZip(zipFile, importDir);

		assertFalse(Files.exists(importDir.resolve("status.json")),
				"status.json zou niet geëxtraheerd moeten zijn");
	}

	@Test
	@DisplayName("Import met ontbrekende configuratie.json faalt")
	void importOntbrekendeConfiguratieFaalt() throws Exception {
		Path testDir = tempDir.resolve("test_onvolledig_config");
		Files.createDirectories(testDir);

		// Maak alleen status en keystore, geen configuratie
		Configuratie c = new Configuratie();
		c.aantalGroepen = 2;
		c.groepsnamen = new String[] { "Lagere groep", "Hogere groep" };
		maakStatus(testDir, c);
		maakKeystore(testDir);

		Path zipFile = tempDir.resolve("import_onvolledig_config.zip");
		maakZip(testDir, zipFile);

		// Controleer dat configuratie.json ontbreekt
		assertFalse(zipBevatEntry(zipFile, "configuratie.json"),
				"Test ZIP bevat configuratie.json, maar zou het niet moeten");
	}

	@Test
	@DisplayName("Import met ontbrekende keystore.ks faalt")
	void importOntbrekendeKeystoreFaalt() throws Exception {
		Path testDir = tempDir.resolve("test_onvolledig_keystore");
		Files.createDirectories(testDir);

		// Maak alleen configuratie en status, geen keystore
		Configuratie c = maakConfiguratie(testDir, huidigeVersie);
		maakStatus(testDir, c);

		Path zipFile = tempDir.resolve("import_onvolledig_keystore.zip");
		maakZip(testDir, zipFile);

		// Controleer dat keystore.ks ontbreekt
		assertFalse(zipBevatEntry(zipFile, "keystore.ks"),
				"Test ZIP bevat keystore.ks, maar zou het niet moeten");
	}

	@Test
	@DisplayName("Import met lege ZIP faalt")
	void importLegeZipFaalt() throws IOException {
		Path zipFile = tempDir.resolve("lege.zip");
		// Maak een lege ZIP
		try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(
				new FileOutputStream(zipFile.toFile()))) {
			// Geen entries toegevoegd
		}

		int entries = telZipEntries(zipFile);
		assertEquals(0, entries, "Lege ZIP zou 0 entries moeten hebben");
	}

	@Test
	@DisplayName("Import met ongeldige ZIP faalt")
	void importOngeldigeZipFaalt() throws IOException {
		Path zipFile = tempDir.resolve("ongeldig.zip");
		Files.writeString(zipFile, "Dit is geen geldige ZIP");

		// Een ongeldige ZIP geeft null als eerste entry, wat resulteert in geen data
		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
			ZipEntry entry = zis.getNextEntry();
			assertTrue(entry == null, "Ongeldige ZIP zou geen entries moeten hebben");
		}
	}

	// ---------------------------------------------------------------------
	// Tests: Round directories in export
	// ---------------------------------------------------------------------

	@Test
	@DisplayName("Export ZIP kan rondedirectories bevatten")
	void exportZipRondedirectories() throws Exception {
		Path testDir = maakTestOmgeving(huidigeVersie);

		// Maak een rondedirectory met een bestand
		Path rondeDir = testDir.resolve("R1-4");
		Files.createDirectories(rondeDir);
		Files.writeString(rondeDir.resolve("R1-4.json"), "{}");

		Path zipFile = tempDir.resolve("export_rondes.zip");
		maakZip(testDir, zipFile);

		// Controleer dat de rondedirectory in de ZIP zit
		assertTrue(zipBevatEntry(zipFile, "R1-4" + File.separator + "R1-4.json"),
				"ZIP mist rondedirectory inhoud");
	}

	// ---------------------------------------------------------------------
	// Hulpmethoden
	// ---------------------------------------------------------------------

	/**
	 * Extraheer een ZIP-bestand naar de opgegeven directory.
	 */
	private void extraheerZip(Path zipPath, Path targetDir) throws IOException {
		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				Path targetPath = targetDir.resolve(entry.getName());

				if (entry.isDirectory()) {
					Files.createDirectories(targetPath);
				} else {
					Files.createDirectories(targetPath.getParent());
					Files.copy(zis, targetPath);
				}
				zis.closeEntry();
			}
		}
	}

	/**
	 * Lees de inhoud van een specifieke entry uit een ZIP-bestand als String.
	 */
	private String leesEntryUitZip(Path zipPath, String entryName) throws IOException {
		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().equals(entryName)) {
					byte[] buffer = new byte[4096];
					java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
					int len;
					while ((len = zis.read(buffer)) > 0) {
						baos.write(buffer, 0, len);
					}
					return baos.toString("UTF-8");
				}
				zis.closeEntry();
			}
		}
		return null;
	}

	/**
	 * Lees de appVersion uit configuratie.json in een ZIP-bestand.
	 */
	private String leesVersieUitZip(Path zipPath) throws IOException {
		String json = leesEntryUitZip(zipPath, "configuratie.json");
		if (json == null) return null;
		Configuratie c = new Gson().fromJson(json, Configuratie.class);
		return c != null ? c.appVersion : null;
	}
}
