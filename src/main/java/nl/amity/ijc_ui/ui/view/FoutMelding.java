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
 * - ...
 */
package nl.amity.ijc_ui.ui.view;

import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

/**
 * Genereer popup met foutmelding of mededeling. Deze moet geaccepteerd worden voordat
 * de applicatie verder kan.
 * Gebruik:
 *         FoutMelding.melding("Melding");
 *         FoutMelding.fout(this, "Volgende ronde is mislukt.", ex);
 *         FoutMelding.info(this, "Verwerk eerst de uitslagen.");
 * @author Lars.Dam
 *
 */
public class FoutMelding {

	private final static Logger logger = Logger.getLogger(FoutMelding.class.getName());

	public static void melding(String infoMessage)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "Foutmelding", JOptionPane.ERROR_MESSAGE);
    }

	/**
	 * Toon een begrijpelijke foutmelding. De technische details (stacktrace) gaan naar het logbestand.
	 */
	public static void fout(Component parent, String tekst, Throwable oorzaak) {
		logger.log(Level.WARNING, tekst, oorzaak);
		String bericht = tekst;
		if (oorzaak != null) {
			bericht += "\n\nDetails staan in het logbestand IJC_UI.log.";
		}
		JOptionPane.showMessageDialog(parent, bericht, "Foutmelding", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Toon een mededeling, bijvoorbeeld waarom een actie nu niet kan.
	 */
	public static void info(Component parent, String tekst) {
		JOptionPane.showMessageDialog(parent, tekst, "Mededeling", JOptionPane.INFORMATION_MESSAGE);
	}
}
