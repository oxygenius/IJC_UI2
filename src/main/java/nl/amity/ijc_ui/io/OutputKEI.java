/**
 * Copyright (C) 2016 Leo van der Meulen
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 3.0
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * See: http://www.gnu.org/licenses/gpl-3.0.html
 *
 * Problemen in deze code:
 */
package nl.amity.ijc_ui.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.amity.ijc_ui.data.groepen.Groep;
import nl.amity.ijc_ui.data.groepen.Groepen;
import nl.amity.ijc_ui.data.groepen.Speler;
import nl.amity.ijc_ui.ui.util.Utils;

/**
 * Schrijf het bestand met KEI stand. Alleen spelers die daadwerkelijk punten
 * hebben behaald, worden getoond.
 *
 * @author Leo.vanderMeulen
 *
 */
public class OutputKEI implements GroepenExportInterface {

	private final static Logger logger = Logger.getLogger(OutputKNSB.class.getName());

	/**
	 * Exporteer de KEI stand naar bestand Rp-rKEIpuntenS.txt Alleen spelers met
	 * behaalde punten worden getoond
	 *
	 * @param groepen
	 *            bevat de stand die geexporteerd moet worden
	 */
	public boolean export(Groepen groepen) {
		try {
			String bestandsnaam = "R" + groepen.getPeriode() + "-" + groepen.getRonde() + "KEIpuntenS.txt";
			logger.log(Level.INFO, "Sla uitslag op in bestand " + bestandsnaam);

			String dirName = "R" + groepen.getPeriode() + "-" + groepen.getRonde();
			new File(dirName).mkdirs();

			FileWriter writer = new FileWriter(dirName + File.separator + bestandsnaam);
			writer.write(getHeader(groepen.getPeriode(), groepen.getRonde()));

			// Vind spelers met KEI punten
			ArrayList<Speler> keispelers = new ArrayList<>();
			for (Groep groep : groepen.getGroepen(Groepen.Sortering.NIVEAU_ASC)) {
				for (Speler speler : groep.getSpelers()) {
					if ((speler.getKeikansen() > 0)) {
						keispelers.add(speler);
					}
				}
			}

			// Sorteer deze eerst op punten, dan op kansen
			Collections.sort(keispelers, new Comparator<Speler>() {
				@Override
				public int compare(Speler o1, Speler o2) {
					// return o2.getRating() - (o1.getRating());
					return (o2.getKeipunten() * 100 + o2.getKeikansen())
							- (o1.getKeipunten() * 100 + o1.getKeikansen());
				}
			});

			// Exporteer de gesorteerde lijst
			int pos = 1;
			for (Speler s : keispelers) {
				String res = Integer.toString(pos++);
				if (res.length() < 2)
					res = " " + res;
				res += ". " + s.getNaam();
				while (res.length() < 34) {
					res += " ";
				}
				String p = Integer.toString(s.getKeipunten());
				if (p.length() < 2)
					p = " " + p;
				res += p + "/";
				p = Integer.toString(s.getKeikansen());
				if (p.length() < 2)
					p = " " + p;
				res += p;
				writer.write(res + System.lineSeparator());

			}
			writer.close();
			return true;
		} catch (IOException ex) {
			logger.log(Level.WARNING, "Export mislukt : " + ex.getMessage());
            Utils.stacktrace(ex);

			return false;
		}
	}

	private String getHeader(int periode, int ronde) {
		String res = "";
		res += "Klassement KEI-punten: ronde: " + ronde + "  periode: " + periode + System.lineSeparator();
		res += "\nPos Naam               KEIpunten/kansen" + System.lineSeparator();
		res += "---------------------------------------" + System.lineSeparator();
		return res;
	}
}
