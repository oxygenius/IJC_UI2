	/**
	 * Copyright (C) 202 Lars Dam
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

//import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Bevesting {

	/**
	 * Vraag bevestiging voor een actie die niet ongedaan gemaakt kan worden.
	 * De knoppen zijn "Ja" en "Nee"; "Nee" is de standaardknop, zodat Enter niets kapotmaakt.
	 *
	 * @param parent  venster waarboven de vraag verschijnt (mag null zijn)
	 * @param vraag   de vraag, bijvoorbeeld "Weet u zeker dat ...?"
	 * @return true als de gebruiker "Ja" kiest
	 */
	public static boolean bevestig(Component parent, String vraag) {
		String[] opties = { "Ja", "Nee" };
		int keuze = JOptionPane.showOptionDialog(parent, vraag, "Bevestiging", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE, null, opties, opties[1]);
		return keuze == 0;
	}

	public static int YesNoCancel(String infoMessage) {
		// ImageIcon icon = new ImageIcon("Bevestiging.png");
	    //JOptionPane.showConfirmDialog(null, infoMessage, "Bevestiging", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, icon);
	    return JOptionPane.showConfirmDialog(null, infoMessage, "Bevestiging", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
	}

	public static int YesNo(String infoMessage) {
		// ImageIcon icon = new ImageIcon("Bevestiging.png");
	    //JOptionPane.showConfirmDialog(null, infoMessage, "Bevestiging", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, icon);
	    return JOptionPane.showConfirmDialog(null, infoMessage, "Bevestiging", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null);
	}
}

