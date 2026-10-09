package nl.amity.ijc_ui.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controleert of er een nieuwere versie van IJC_UI beschikbaar is op GitHub.
 *
 * De methode haalt de nieuwste release op via de GitHub Releases API en
 * vergelijkt het versienummer met de lokale versie.
 */
public final class VersieChecker {

	private static final Logger logger = Logger.getLogger(VersieChecker.class.getName());
	private static final String GITHUB_RELEASES_URL = "https://api.github.com/repos/oxygenius/IJC_UI2/releases/latest";

	private VersieChecker() {
	}

	/**
	 * @return het versienummer van de nieuwste GitHub-release, of null bij fout
	 */
	public static String haalNieuwsteVersie() {
		try {
			URL url = URI.create(GITHUB_RELEASES_URL).toURL();
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
			conn.setRequestProperty("User-Agent", "IJC_UI2");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(10000);

			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				logger.log(Level.WARNING, "GitHub API retourneerde status {0}", responseCode);
				return null;
			}

			try (InputStream in = conn.getInputStream();
				 InputStreamReader isr = new InputStreamReader(in);
				 BufferedReader reader = new BufferedReader(isr)) {

				String line;
				while ((line = reader.readLine()) != null) {
					// Zoek de "tag_name" veld in de JSON-response
					// Voorbeeld: "tag_name":"v2.0.2.2"
					int tagIndex = line.indexOf("\"tag_name\"");
					if (tagIndex >= 0) {
						// Zoek de dubbele aanhalingsteken na de dubbele punt na "tag_name"
						int colon = line.indexOf(':', tagIndex + "tag_name".length() + 2);
						if (colon >= 0) {
							// Zoek de begin- en einde-aanhalingsteken van de waarde
							int startQuote = line.indexOf('"', colon + 1);
							if (startQuote >= 0) {
								int endQuote = line.indexOf('"', startQuote + 1);
								if (endQuote > startQuote) {
									String value = line.substring(startQuote + 1, endQuote);
									// Verwijder het "v" voorvoegsel
									if (value.startsWith("v") || value.startsWith("V")) {
										value = value.substring(1);
									}
									return value;
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "Fout bij ophalen nieuwste versie van GitHub: " + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Vergelijkt twee versienummers.
	 *
	 * @param current de huidige versie
	 * @param latest  de nieuwste beschikbare versie
	 * @return negatief als current < latest, 0 als gelijk, positief als current > latest
	 */
	public static int compareVersions(String current, String latest) {
		if (current == null || latest == null) {
			return 0;
		}
		String[] currentParts = current.split("\\.");
		String[] latestParts = latest.split("\\.");
		int len = Math.max(currentParts.length, latestParts.length);
		for (int i = 0; i < len; i++) {
			int c = (i < currentParts.length) ? parsePart(currentParts[i]) : 0;
			int l = (i < latestParts.length) ? parsePart(latestParts[i]) : 0;
			if (c != l) {
				return c - l;
			}
		}
		return 0;
	}

	private static int parsePart(String part) {
		try {
			return Integer.parseInt(part);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
