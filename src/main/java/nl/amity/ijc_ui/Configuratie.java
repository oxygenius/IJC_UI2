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

import java.math.BigDecimal;
import nl.amity.ijc_ui.data.external.api.APIs;
import nl.amity.ijc_ui.data.external.api.APIConfig;
import nl.amity.ijc_ui.data.external.api.APIConfigs;


/**
 * Configuratie van het indelingsprogramma. Doel is dat hier geen code in staat
 * maar alleen data. Hiermee kan deze geexporteerd worden naar een json bestand
 * waarna code editing overbodig is geworden om het programma te configureren.
 * 
 * @author Leo.vanderMeulen
 *
 */

public class Configuratie {
	
	public Configuratie() {
		
	}
	
	/**
	 * Salt
	 */
	public char[] salt = "dpwo98dpomwfviuoigjwjor8owunjsbgiojr;thm,ted".toCharArray();

	
	/**
	 * Aantal periodes in een seizoen
	 */
	public int perioden = 4;
	/**
	 * Aantal rondes per periode. Alle perioden in een seizoen hebben hetzelfde
	 * aantal rondes
	 */
	public int rondes = 8;

	/**
	 * Aantal groepen waarin wordt gespeeld
	 */
	public int aantalGroepen = 7;

	/**
	 * Geef hier voor iedere groep de naam op, lopende van de laagst groep tot
	 * de hoogste groep
	 */
	public String[] groepsnamen = { "Pionnengroep", "Paardengroep", "Lopergroep", "Torengroep", "Damegroep",
			"Koningsgroep", "Keizergroep" };

	/**
	 * Groovy functie die bepaalt hoeveel series er gespeeld moeten. Input is
	 * X=groepnummer, Y=periodenummer, Z=rondenummer Gebruik: int
	 * groep,periode,ronde; int series = Eval.xyz(groep, periode, ronde,
	 * Configuratie.grAantalSeries);
	 */
	public String grAantalSeries = "if (x == 6) { if ((y == 1) && (z == 1)) { return 2 } else { return 1 } }"
			+ " else if ((y == 1) && (z == 1)) { return 3 } else { return 2 } ";

	/**
	 * Retour aantal series
	 * 
	 * @param groep
	 * @param periode
	 * @param ronde
	 * @return aantal series
	 */
	public int bepaalAantalSeries(int groep, int periode, int ronde) {
		return (Integer) groovy.util.Eval.xyz(groep, periode, ronde, grAantalSeries);
	}

	/**
	 * Groovy functie die bepaalt hoeveel doorschuivers er zijn. Input is
	 * X=periodenummer, Y=rondenummer Gebruik: int groep,periode,ronde; int
	 * doorschuivers = Eval.xy(periode, ronde,
	 * Configuratie.grAantalDoorschuivers);
	 */
	public String grAantalDoorschuivers = "if (z >= 4) { if (z < 8) { if (x == 0) { return 2 } else { return 4 } } else { return 1 } } else { return 0 }";

	/**
	 * Retourneer aantal doorschuivers
	 * 
	 * @param periode
	 * @param ronde
	 * @return aantal doorschuivers
	 */
	public int bepaalAantalDoorschuivers(int groep, int periode, int ronde) {
		int res = (Integer) groovy.util.Eval.xyz(groep, periode, ronde, grAantalDoorschuivers);
		return res;
	}

	/**
	 * Retourneer aantal doorschuivers in de volgende ronde
	 * 
	 * @param p Huidige periode
	 * @param r Huidige ronde
	 * @return aantal doorschuivers
	 */
	public int bepaalAantalDoorschuiversVolgendeRonde(int groep, int p, int r) {
		int ronde = r;
		int periode = p;
		ronde += 1;
        if (ronde > rondes) {
        	ronde = 1;
        	periode++;
        	if (periode > perioden) periode = 1;
        }

		return (Integer) groovy.util.Eval.xyz(groep, periode, ronde, grAantalDoorschuivers);
	}

	/**
	 * Groovy functie die bepaalt of op rating wordt gesorteerd. Input is
	 * X=groepnummer, Y=periodenummer, Z=rondenummer Gebruik: int groep; int
	 * periode; int ronde; boolean sort = Eval.xyz(groep, periode, ronde,
	 * Configuratie.grSorteerOpRating);
	 */
	public String grSorteerOpRating = "if ((x == 6) && (z > 1) && (z < 7)) { true } else { false }";

	/**
	 * Bepaal of er gesorteerd moet worden op rating
	 * 
	 * @param groep
	 * @param periode
	 * @param ronde
	 * @return true, als er voor indelen gesorteerd moet worden op rating
	 */
	public boolean sorteerOpRating(int groep, int periode, int ronde) {
		return (Boolean) groovy.util.Eval.xyz(groep, periode, ronde, grSorteerOpRating);
	}

	/**
	 * Standaard wordt er gecontroleerd of de doorschuiver in de laatste ronde
	 * gegarandeerd kampioen is en niet te achterhalen door nummer 2. Door deze
	 * op false te true te zetten, wordt deze controle niet uitgevoerd
	 */
	public boolean laasteRondeDoorschuivenAltijd = false;

	/**
	 * Zet op true als de eerste ronde van iedere periode anders ingedeeld moet
	 * worden waarbij de bovenste helft tegen de onderste helft speelt. Door dit
	 * te doen, wordt er snel een scheiding gemaakt tussen de goede en minder
	 * goede spelers in een groep. Hierna wordt dus sneller tegen spelers van
	 * het eigen niveau gespeeld. 
	 */
	public boolean specialeIndelingEersteRonde = true;

	/**
	 * Bepaald hoe groot het verschil standaard mag zijn tussen twee
	 * tegenstanders in het klassement. Hiermee krijgt spelen tegen eigen niveau
	 * een hogere prioriteit dan spelen tegen een nieuwe tegenstander
	 */
	public int indelingMaximumVerschil = 3;

	/**
	 * Standaard rating voor nieuwe speler. Bij het toevoegen van een nieuwe
	 * speler is dit de standaard rating, afhankelijk van de groep waarin hij
	 * begint.
	 */
	public int[] startRating = { 100, 150, 200, 300, 500, 800, 1400 };

	/**
	 * standaard punten per groep bij aanvang periode
	 */
	public int[] startPunten = { 0, 10, 20, 30, 40, 50, 60 };

	/**
	 * Geef aan of er een bestand gegenereerd moet worden dat door de KNSB
	 * gebruikt kan worden voor verwerking resultaten in de KNSB rating
	 */
	public boolean exportKNSBRating = true;

	/**
	 * Geef aan of er een bestand gegenereerd moet worden dat door de OSBO
	 * gebruikt kan worden voor verwerking resultaten in de KNSB rating
	 */
	public boolean exportOSBORating = true;

	/**
	 * Geef aan of het simpele tekst bestand geexporteerd moet worden, kan o.a.
	 * worden gebruikt voor publicatie op websites.
	 */
	public boolean exportTextShort = true;

	/**
	 * Geef aan of het complexe tekstbestand geexporteerd moet worden. DIt
	 * formaat is compatible met oudere indelingsoftware.
	 */
	public boolean exportTextLong = true;

	/**
	 * Geef aan of in het lange bestandsformaat eventuele doorschuivers
	 */
	public boolean exportDoorschuivers = true;

	/**
	 * Geef header doorschuivers
	 */
	public String exportDoorschuiversStart = "De volgende spelers spelen deze week mee in deze groep:";

	/**
	 * Geef footer doorschuivers
	 */

	public String exportDoorschuiversStop = "De aanwezigheid van speler en de poging om alle speelgroepen een even aantal speler te geven bepaalt uiteindelijk wie doorschuift!";

	/**
	 * Geef aan of de KEI stand geexporteerd moet worden.
	 */
	public boolean exportKEIlijst = true;
	
	/**
	 * Geef aan of de intekenlijsten gemaakt moeten worden
	 */
	public boolean exportIntekenlijst = true;

	/**
	 * Geef aan of er speciale status bestanden opgeslagen moeten worden,
	 * bijvoorbeeld bij exporteren wedstrijden en een expliciete save in het
	 * menu.
	 */
	public boolean saveAdditionalStates = true;

	/**
	 * Waar beginnen met zoeken naar een trio? param x = groepsgrootte
	 */
	public String grBeginTrio = "if (x > 0) { x / 2 } else { 0 }";

	/**
	 * Bepaal beginpunt voor zoeken naar trio
	 * 
	 * @param groepsgrootte
	 * @return index voor beginpunt trio
	 */
	public int getBeginpuntTrio(int groepsgrootte) throws Exception {
			BigDecimal bd = (BigDecimal) groovy.util.Eval.x(groepsgrootte, grBeginTrio);
			return bd.intValue();
	}

	/**
	 * Bestandsnaam voor configuratie bestand prefix .json wordt automatisch
	 * toegevoegd
	 */
	public String configuratieBestand = "configuratie";

	/**
	 * Bestandsnaam voor status bestand prefix .json )en evt datum postfix)
	 * wordt automatisch toegevoegd
	 */
	public String statusBestand = "status";

	/**
	 * Applicatie titel
	 */
	public String appTitle = "Indeling Interne Jeugd Competitie";

	/**
	 * Naam Vereniging
	 */
	public String verenigingNaam = "<Schaakverenigingsnaam>";
	
	/**
	 * Naam competitie
	 */
	public String competitieNaam = "<Competitienaam>";
	
	/**
	 * Locatie
	 */
	public String competitieLocatie = "<Locatie>";
	
	/**
	 * Contactpersoon - Naam
	 */
	public String contactPersoonNaam = "Wedstrijdleiding Jeugd";

	/**
	 * Contactpersoon - Email
	 */
	public String contactPersoonEmail = "ijc@<vereniging>.nl";

	/**
	 * Fuzzy indeling wordt gebruikt indien waarde 'true'.
	 * Anders old skool algoritme, aangestuurd door configuratie
	 */
	public boolean fuzzyIndeling = true;

	/**
	 * Fuzzy oneven wordt gebruikt indien waarde 'true'.
	 * Anders wordt bij oneven spelers een ouderwetse trio gespeeld.
	 */
	public boolean fuzzyOneven = true;

	/**
	 * Fuzzy ranglijstpunten wordt gebruikt indien waarde 'true'.
	 * Gebruik in fuzzyroutine puntenranglijst ipv ranglijst.
	 * Dit geldt alleen voor ronde waarin géén doorschuiven is.
	 */
	public boolean fuzzyRanglijstpunten = true;

	/**
	 * Wegingsfactor voor fuzzy algoritme. Waarde tussen 0.0 en 1.0
	 * Deze parameter bepaald hoe zwaar het meeweegt dat er tegen een
	 * nieuwe tegenstander wordt gespeeld
	 */
	public double fuzzyWegingAndereTegenstander = 0.99;

	/**
	 * Wegingsfactor voor fuzzy algoritme. Waarde tussen 0.0 en 1.0
	 * Deze parameter bepaald hoe zwaar het meeweegt dat er tegen een
	 * tegenstander wordt gespeeld die vlakbij op de ranglijst staat
	 */
	public double fuzzyWegingAfstandRanglijst = 0.98;

	/**
	 * Wegingsfactor voor fuzzy algoritme. Waarde tussen 0.0 en 1.0
	 * Deze parameter bepaald hoe zwaar het meeweegt dat er tegen een
	 * tegenstander wordt gespeeld die met meer/minder punten op de ranglijst staat.
	 */
	public double fuzzyWegingAfstandRanglijstpunten = 0.98;

	/**
	 * Wegingsfactor voor fuzzy algoritme. Waarde tussen 0.0 en 1.0
	 * Deze parameter bepaald hoe zwaar het meeweegt dat er tegen een
	 * tegenstander met tegenovergestelde zwart/wit voorkeur. Hoe hoger,
	 * hoe meer geprobeerd wordt de totale voorkeur van de groep neutraal
	 * te krijgen
	 */
	public double fuzzyWegingZwartWitVerdeling = 0.93;

	/**
	 * Wegingsfactor voor fuzzy algoritme. Waarde tussen 0.0 en 1.0
	 * Deze parameter bepaald hoe zwaar het meeweegt dat er tegen een
	 * tegenstander wordt gespeeld die uit een hogere groep komt
	 */
	public double fuzzyWegingDoorschuiverEigenGroep = 0.98;

	/**
	 * plone52URL is the URL for a Plone 5.2 RESTAPI connection
	 */
	//public String plone52URL = "";

	/**
	 * plone52Path is the Path in to place new content for a Plone 5.2 RESTAPI connection
	 */
	//public String plone52Path = "";

	/**
	 * plone52UserName is the username for a Plone 5.2 RESTAPI connection
	 */
	//public String plone52UserName = "";

	/**
	 * plone52Password is the password for a Plone 5.2 RESTAPI connection
	 */
	//public String plone52Password = "";
	
	/**
	 * externalAPIs is an array of type API
	 * used for pushing information to external sites/systems trough an API
	 */

	public APIs externalAPIs;
	
	public APIConfigs externalAPIConfigs = new APIConfigs();
	
	public void Update() {
		 externalAPIs = new APIs();
	}
}
