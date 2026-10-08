package nl.amity.ijc_ui.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Versienummer van de applicatie.
 *
 * Het enige versienummer staat in pom.xml. Maven schrijft het bij het bouwen in
 * version.properties, zodat het hier zonder dubbele administratie gelezen kan worden.
 */
public final class Versie {

	private static final Logger logger = Logger.getLogger(Versie.class.getName());
	private static final String RESOURCE = "/version.properties";
	private static final String ONBEKEND = "onbekend";

	private static String versie;

	private Versie() {
	}

	/**
	 * @return versienummer uit pom.xml, of "onbekend" als het niet gelezen kan worden
	 */
	public static synchronized String get() {
		if (versie == null) {
			versie = lees();
		}
		return versie;
	}

	private static String lees() {
		try (InputStream in = Versie.class.getResourceAsStream(RESOURCE)) {
			if (in == null) {
				logger.log(Level.WARNING, RESOURCE + " niet gevonden; versie onbekend.");
				return ONBEKEND;
			}
			Properties p = new Properties();
			p.load(in);
			String v = p.getProperty("version", "").trim();
			// Niet gefilterd (bijvoorbeeld gestart zonder Maven-build): placeholder staat er nog
			if (v.isEmpty() || v.startsWith("${")) {
				logger.log(Level.WARNING, RESOURCE + " is niet door Maven gevuld; versie onbekend.");
				return ONBEKEND;
			}
			return v;
		} catch (IOException e) {
			logger.log(Level.WARNING, "Fout bij lezen van " + RESOURCE + ": " + e.getMessage(), e);
			return ONBEKEND;
		}
	}
}
