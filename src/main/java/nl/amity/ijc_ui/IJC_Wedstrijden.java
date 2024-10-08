/**
 * Copyright (C) 2016-2024 Leo van der Meulen/Lars Dam
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
package nl.amity.ijc_ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import nl.amity.ijc_ui.ui.control.IJCController;
import nl.amity.ijc_ui.ui.view.Hoofdscherm;

/**
 * Main class of the application.
 * Lees de groep samenstellingen uit een uitslagbestand en maak
 * de eerste versie van de goepsindelingen (hierbij wordt een aangepaste
 * groepen structuur gemaakt, rekening houdende met doorschuivers).
 * Hierna wordt de user interface opgestart.
 * Indien er een statusbestand wordt gevonden, wordt deze gelezen in
 * plaats van het uitslagbestand.
 *
 * @author Leo van der Meulen
 */
public class IJC_Wedstrijden {

    private final static Logger logger = Logger.getLogger(IJC_Wedstrijden.class.getName());

    /**
     * Main start method for the application.
     * Reads existing status or opens the default uitslag file.
     *
     * Hierna neemt Hoofdscherm en IJCController als duo het
     * over voor de aansturing van de functionaliteit.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	logger.log(Level.INFO, "Opstarten controller");
        IJCController.getInstance().start();

        logger.log(Level.INFO, "Opstarten user interface");
        new Hoofdscherm().setVisible(true);

    }

}
