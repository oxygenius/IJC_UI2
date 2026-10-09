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
package nl.amity.ijc_ui.ui.control;

import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;
import javax.swing.JFrame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import nl.amity.ijc_ui.Configuratie;
import nl.amity.ijc_ui.data.external.api.APIConfig;
import nl.amity.ijc_ui.data.external.api.Plone52;
import nl.amity.ijc_ui.data.groepen.Groep;
import nl.amity.ijc_ui.data.groepen.Groepen;
//import nl.amity.ijc_ui.data.groepen.Groepen.Sortering;
import nl.amity.ijc_ui.data.groepen.Speler;
import nl.amity.ijc_ui.data.wedstrijden.Groepswedstrijden;
import nl.amity.ijc_ui.data.wedstrijden.Serie;
import nl.amity.ijc_ui.data.wedstrijden.Wedstrijd;
import nl.amity.ijc_ui.data.wedstrijden.Wedstrijden;
import nl.amity.ijc_ui.io.GroepenReader;
import nl.amity.ijc_ui.io.ImportSpelers;
import nl.amity.ijc_ui.io.OutputExcel;
import nl.amity.ijc_ui.io.OutputIntekenlijst;
import nl.amity.ijc_ui.io.OutputKEI;
import nl.amity.ijc_ui.io.OutputKNSB;
import nl.amity.ijc_ui.io.OutputNeuralData;
import nl.amity.ijc_ui.io.OutputOSBO;
import nl.amity.ijc_ui.io.OutputSpeelschema;
import nl.amity.ijc_ui.io.OutputStanden;
import nl.amity.ijc_ui.io.OutputUitslagen;
import nl.amity.ijc_ui.neural.Voorspeller;
import nl.amity.ijc_ui.ui.util.Utils;
import nl.amity.ijc_ui.ui.view.Bevesting;
import nl.amity.ijc_ui.ui.view.ExternDialog;
import nl.amity.ijc_ui.ui.view.LesTekstDialoog;
import nl.amity.ijc_ui.ui.view.UitslagDialoog;
import nl.amity.ijc_ui.util.LoggingConfig;

/**
 * Main controller class voor afhandeling van de groepen en wedstrijden
 */
public class IJCController {

    private static volatile IJCController instance = null;

    private final static Logger logger = Logger.getLogger(IJCController.class.getName());

    private static final String defaultInputfile = "uitslag.json";

    private Status status;
    private Configuratie c;
	private KeyStore ks = null;
	// TO DO
    // In first time startup. Create a unique keystore password
	private char[] keyStorePassword = "m2fhwuiyegnfwgofijeghuiwhpfijeuovy4iojhkl43ngkls".toCharArray();
	private String ksfilename = "keystore.ks";

    private String laatsteExport;

    private String txtLes;
    private String pw = "";
    
    protected IJCController() throws GeneralSecurityException, CertificateException, IOException {
    	try {
    		ks = KeyStore.getInstance(KeyStore.getDefaultType());
    	}
    	catch (KeyStoreException kse) {
			// TODO Auto-generated catch block
			logger.log(Level.WARNING, kse.getMessage(), kse);
    	}
    	try (InputStream data = new FileInputStream(ksfilename)) {
    		this.ks.load(data, keyStorePassword);
    	}
    	catch (Exception e) {
			// TODO Auto-generated catch block
			logger.log(Level.WARNING, e.getMessage(), e);
			try {
				ks.load(null);
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				logger.log(Level.WARNING, e2.getMessage(), e2);
			}
    	}

    	status = new Status();
//    	status.groepen = null;
//    	status.wedstrijden = null;
//    	status.wedstrijdgroepen = null;
//    	status.externGespeeld = null;
    	c = new Configuratie();
    }

    public static IJCController getInstance() {
        if (instance == null) {
        	try {
            instance = new IJCController();
        	}
        	catch (GeneralSecurityException | IOException e)
        	{
    			// TODO Auto-generated catch block
    			logger.log(Level.WARNING, e.getMessage(), e);
        	}
        }
        return instance;
    }

    /**
     * Alias voor getInstance, korter voor beter leesbare code
     * @return
     */
    public static IJCController getI() {
    	return getInstance();
    }
    
    /** 
     * getSalt 
     * @return 
     */
    public char[] getSalt() {
    	return c.salt;
    }
    
    /** 
     * setPassword in KeyStore
     */
    public boolean setPassword(String alias, byte[] password, char[] master) throws GeneralSecurityException, DestroyFailedException {
    	SecretKey wrapper = new SecretKeySpec(password, "DSA");
    	KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(wrapper);
    	KeyStore.PasswordProtection pp = new KeyStore.PasswordProtection(master);
    	try {
    		this.ks.setEntry(alias, entry, pp);
    		pp.destroy();
    		return true;
    	}
    	catch (Exception e) {
			// TODO Auto-generated catch block
			logger.log(Level.WARNING, e.getMessage(), e);
    		pp.destroy();
			return false;
    	}
    	finally {
    	}
    }

    /** 
     * checkPassword in KeyStore
     */
    public boolean checkPassword(String alias, char[] master, char[] passwd) throws GeneralSecurityException, DestroyFailedException {
    	if (Arrays.equals(this.getPassword(alias, master), new String(passwd).getBytes())) {
			logger.log(Level.FINE, "oldPassword is OK");
    		return true;
    	} else {
			logger.log(Level.WARNING, "oldPassword is Wrong");
    		return false;
    	}
    }

    	
    /** 
     * getPassword from KeyStore
     */
    public byte[] getPassword(String alias, char[] master) throws GeneralSecurityException, DestroyFailedException {
    	KeyStore.PasswordProtection pp = new KeyStore.PasswordProtection(master);
    	try {
    		KeyStore.SecretKeyEntry e = (KeyStore.SecretKeyEntry) this.ks.getEntry(alias, pp);
    		try {
    			return e.getSecretKey().getEncoded();
    		}
    		catch (NullPointerException npe) {
    			return new byte[0];
    		}
    	}
    	finally {
    		pp.destroy();
    	}
    }

    /**
     * Snelle methode om tot de configuratie te komen
     * @return
     */
    public static Configuratie c() {
    	return getInstance().c;
    }

    public boolean isAutomatisch() {
        return status.automatisch;
    }

    public void setAutomatisch(boolean automatisch) {
        this.status.automatisch = automatisch;
    }

    public void leesGroepen() {
        synchronized (this) {
            leesGroepen(defaultInputfile);
        }
    }

    public Status getStatus() {
    	return status;
    }

    /**
     * Lees groepen bestand in
     * @param bestandsnaam
     */
	public void leesGroepen(String bestandsnaam) {
		synchronized (this) {
			status.groepen = null;
			try {
				if (bestandsnaam.endsWith(".txt")) {
					logger.log(Level.INFO, "Lees groepen in TXT formaat uit " + bestandsnaam);
					status.groepen = new GroepenReader().leesGroepen(bestandsnaam);
				} else if (bestandsnaam.endsWith(".json")) {
					logger.log(Level.INFO, "Lees groepen in JSON uit bestand " + bestandsnaam);
					status.groepen = new GroepenReader().leesGroepenJSON(bestandsnaam);
					// check for old groepen in status.
					for (int i=c.aantalGroepen; i<11 ; i++) {
						status.groepen.removeGroep(status.groepen.getGroepByNiveau(i));
					}
				} else {
					return;
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "Geen statusbestand gelezen");
				status.groepen = null;
			}
			if (status.groepen == null) {
				status.groepen = new Groepen();
				for (int i = 0; i < c.aantalGroepen; ++i)
					status.groepen.addGroep(new Groep(i));
				status.groepen.setPeriode(1);
				status.groepen.setRonde(1);
			}

			status.wedstrijdgroepen = new Groepen();
			status.wedstrijden = new Wedstrijden();
			status.resultaatVerwerkt = new Groepen();
			status.externGespeeld = new ArrayList<>();
			if (status.groepen.getRonde() == 1)
				resetAanwezigheidspunt();
			setAutomatisch(true);

			maakGroepsindeling();
			maakWedstrijden();
		}
	}

	/**
	 * Zet voor alle spelers het aanwezigheidspunt op onwaar.
	 */
	private void resetAanwezigheidspunt() {
    	logger.log(Level.FINE, "Eerste ronde van een periode; reset aanwezigheidspunt");
//		for (Groep groep : status.groepen.getGroepen(Groepen.Sortering.NIVEAU_ASC)) {
		for (Groep groep : status.groepen.getGroepen()) {
			for (Speler s : groep.getSpelers()) {
				s.setAanwezig(false);
			}
		}

	}

	/**
	 * Lees het status export bestand
	 * @return true, als bestand gevonden en ingelezen
	 */
	public boolean leesStatusBestand() {
		synchronized (this) {
        	logger.log(Level.FINE, "Lees status");
        	leesStatus();
        	leesConfiguratie();
        	checkStatus();
			if ((status == null) || (status.groepen == null)) {
				status = new Status();
				status.groepen = null;
				status.wedstrijdgroepen = null;
				status.wedstrijden = null;
				status.resultaatVerwerkt = null;
	        	logger.log(Level.WARNING, "Status bestand niet correct ingelezen");
				return false;
			}
			if (status.groepen.getAantalGroepen() != c.aantalGroepen) {
		    	logger.log(Level.WARNING, "Fix Groepen!");
				fix_groepen(status.groepen, c.aantalGroepen)		;
			}
			if (status.wedstrijdgroepen!=null) {
				if (status.wedstrijdgroepen.getAantalGroepen() != c.aantalGroepen) {
					logger.log(Level.WARNING, "Fix Wedstrijdgroepen!");
					fix_groepen(status.wedstrijdgroepen, c.aantalGroepen);
				}
			}
			status.groepen.sorteerGroepen(true);
		}
    	logger.log(Level.FINE, "Statusbestand ingelezen");
    	logger.log(Level.CONFIG, "aantal entries APIConfig = " + c.externalAPIConfigs.apiconfigs.size());
		return true;
	}
	
	public void fix_groepen(Groepen s_groepen, int c_groepenaantal) {
		if (s_groepen.getAantalGroepen() < c_groepenaantal) {
			logger.log(Level.WARNING, "More Groups is Config then in Status!");
//			for (Groep g : s_groepen.getGroepen(Groepen.Sortering.NIVEAU_ASC))
			for (Groep g : s_groepen.getGroepen())
				g.setNaam(c.groepsnamen[g.getNiveau()]);
			// Niveaus lopen van 0 t/m c_groepenaantal-1 (was <=: maakte één groep te veel aan)
			for (int i=s_groepen.getAantalGroepen(); i<c_groepenaantal;i++) {
				s_groepen.addGroep(new Groep(i));						
			}
		}
		if (s_groepen.getAantalGroepen() > c_groepenaantal) {
			logger.log(Level.WARNING, "More Groups (" + s_groepen.getAantalGroepen() + ") in Status then in Config (" + c_groepenaantal + ")!");
			//Waarschuwing en bevestiging voor verwijderen groepen uit Status.
			//Zorg dat de te verwijderen groep leeg wordt gemaakt!
			Groepen g_del = new Groepen();
//			for (Groep g : s_groepen.getGroepen(Groepen.Sortering.NIVEAU_ASC)) {
			for (Groep g : s_groepen.getGroepen()) {
				if (g.getNiveau() >= c_groepenaantal) {
					logger.log(Level.INFO, "Players from Groups (" + g.getNaam() + ") will be moved to lower group (" + s_groepen.getGroepByNiveau(g.getNiveau()-1) + ")");
					IJCController.terugschuiven(g,s_groepen.getGroepByNiveau(g.getNiveau()-1));
					g_del.addGroep(g);
				}
			}
			logger.log(Level.INFO, "All players moved!");
			try {
//				for (Groep g : g_del.getGroepen(Groepen.Sortering.NIVEAU_ASC)) {
				for (Groep g : g_del.getGroepen()) {
					s_groepen.removeGroep(g);
				}
				logger.log(Level.INFO, "One or more Groups deleted!");
			} catch (Exception e1) {
				logger.log(Level.SEVERE, "Problem in removing groups!");
			}
			c.aantalGroepen = s_groepen.getAantalGroepen();
		}
	}

    /**
     * Groepen zoals ingelezen met aanwezigheid bijgewerkt.
     *
     * @return
     */
    public Groepen getGroepen() {
        return status.groepen;
    }

    public Groep getGroepByID(int id) {
        return status.groepen.getGroepByNiveau(id);
    }

    public int getAantalGroepen() {
        return status.groepen.getAantalGroepen();
    }

    public Groepen getWedstrijdgroepen() {
        return status.wedstrijdgroepen;
    }

    public Groep getWedstrijdGroepByID(int id) {
        return status.wedstrijdgroepen.getGroepByNiveau(id);
    }

    public Groep getResultaatGroepByID(int id) {
    	if (status.resultaatVerwerkt != null) {
    		return status.resultaatVerwerkt.getGroepByNiveau(id);
    	} else {
    		return null;
    	}
    }

    public int getAantalWedstrijdGroepen() {
        return status.wedstrijdgroepen.getAantalGroepen();
    }

    public Wedstrijden getWedstrijden() {
        return status.wedstrijden;
    }

    public void setWedstrijden(Wedstrijden wedstrijden) {
    	status.wedstrijden = wedstrijden;
    }

    /**
     * Zet de groepen van aanwezige spelers om naar de wedstrijdgroepen. In de
     * wedstrijdgroepen zitten alleen de aanwezige spelers aangevuld met de
     * eventuele doorschuivers.
     * Deze wedstrijdgroepen worden vervoglens gebruikt om de wedstrijden in te delen
     */
    public void maakGroepsindeling() {
        synchronized (this) {
        	logger.log(Level.INFO, "Maak groepsindeling");
        	status.wedstrijdgroepen = getIndeler().maakGroepsindeling(status.groepen);
            if (status.automatisch) {
                maakWedstrijden();
            }
        }
    }

    /**
     * Maak de groepsindeling voor een wedstrijdgroep voor alleen de geselecteerde
     * groep. Indelingen andere groepen blijven ongewijzigd
     *
     */
    public void maakGroepsindeling(int groepID) {
    	//synchronized (this) {
        	logger.log(Level.INFO, "Maak groepsindeling voor groep " + groepID);
    		status.wedstrijdgroepen = getIndeler().maakGroepsindeling(status.groepen, status.wedstrijdgroepen, groepID);
		//}
    }
    /**
     * Bepaal de te spelen wedstrijden op basis van de wedstrijdgroepen.
     */
    public void maakWedstrijden() {
        synchronized (this) {
        	logger.log(Level.INFO, "Maak wedstrijden voor alle groepen");
        	status.wedstrijden = getIndeler().maakWedstrijdschema(status.wedstrijdgroepen);
        	status.wedstrijden.setSpeeldatum(Calendar.getInstance().getTime());
            printWedstrijden();
        }
    }

    /**
     * Bepaal de te spelen wedstrijden op basis van de wedstrijdgroepen.
     */
    public void maakWedstrijden(int groepID) {
        synchronized (this) {
        	logger.log(Level.INFO, "Maak wedstrijden voor groep " + groepID);
        	status.wedstrijden = getIndeler().updateWedstrijdschema(status.wedstrijden, status.wedstrijdgroepen, groepID);
        	status.wedstrijden.setSpeeldatum(Calendar.getInstance().getTime());
            printWedstrijden(groepID);
        }
    }

    /**
     * Print wedstrijden op het scherm, opgedeeld per serie
     */
    public void printWedstrijden() {
        logger.log(Level.FINE, "Wedstrijden Periode " + status.wedstrijden.getPeriode() + " Ronde " + status.wedstrijden.getRonde());
        for (Groepswedstrijden gw : status.wedstrijden.getGroepswedstrijden()) {
        	printGroepsWedstrijden(gw);
        }
    }

    /**
     * Print wedstrijden voor ��n groep op het scherm, opgedeeld per serie
     */
	public void printWedstrijden(int groep) {
		logger.log(Level.INFO, "Print wedstrijden");
		logger.log(Level.FINE, "Wedstrijden Periode " + status.wedstrijden.getPeriode() + " Ronde " + status.wedstrijden.getRonde());
		Groepswedstrijden gw = status.wedstrijden.getGroepswedstrijdenNiveau(groep);
		printGroepsWedstrijden(gw);
	}

	/**
	 * Print wedstrijden voor gespecificeerde Groepswedstrijden
	 * @param gw
	 */
	public void printGroepsWedstrijden(Groepswedstrijden gw) {
		logger.log(Level.FINE, " " + Groep.geefNaam(gw.getNiveau()));
		int i = 1;
		int distance = 0;
		for (Serie serie : gw.getSeries()) {
			logger.log(Level.FINE, " Serie " + i);
			for (Wedstrijd w : serie.getWedstrijden()) {
				logger.log(Level.FINE, " " + w.toString());
				distance += w.getDistance();
			}
			++i;
		}
		if (!gw.getTriowedstrijden().isEmpty()) {
			logger.log(Level.FINE, " Trio");
			for (Wedstrijd w : gw.getTriowedstrijden()) {
				logger.log(Level.FINE, " " + w.toString());
				distance += w.getDistance();
			}

		}
		logger.log(Level.FINE, " Totale afstand : " + distance);
	}

    /**
     * Meld een speler aan/afwezig
     * @param groep Betreffende groep
     * @param index Betreffende speler
     * @param waarde true, als aanwezig melden
     */
    public void setSpelerAanwezigheid(Groep groep, int index, final boolean waarde) {
        synchronized (this) {
            if (groep != null) {
                Speler s = groep.getSpelers().get(index);
                if (s != null) {
                	logger.log(Level.INFO, "Speler " + index + " in groep " + groep.getNaam() + " is " + (waarde ? "niet aanwezig" : "aanwezig"));
                    s.setAanwezig(waarde);
                    if (status.automatisch) {
                        maakGroepsindeling();
                    }
                }
            }
        }

    }

    /**
     * Voeg een speler toe aan een groep
     * @param groepID Groep waaraan toe te voegen
     * @param sp Gedefinieerde speler die toegevoegd moet worden
     * @param locatie Locatie in de tabel waar toe te voegen
     */
    public void addSpeler(int groepID, Speler sp, int locatie) {
    	logger.log(Level.INFO, "Voeg speler " + sp.getInitialen() + " toe aan groep " + groepID + ", locatie " + locatie);
        Groep gr = status.groepen.getGroepByNiveau(groepID);
        if (locatie > 0) {
        	gr.addSpeler(sp, locatie);
        } else {
        	gr.addSpeler(sp);
        }
        if (status.automatisch) {
            maakGroepsindeling();
        }
    }

    public void verwijderSpeler(int groepID, Speler sp, int locatie) {
    	logger.log(Level.INFO, "Verwijder speler " + sp.getInitialen() + " uit groep " + groepID + ", locatie " + locatie);
        Groep gr = status.groepen.getGroepByNiveau(groepID);
        gr.removeSpeler(sp, locatie);
        if (status.automatisch) {
            maakGroepsindeling();
        }
    }

    public void verwijderWedstrijdSpeler(int groepID, Speler sp, int locatie) {
    	logger.log(Level.INFO, "Verwijder speler " + sp.getInitialen() + " uit wedstrijdgroep " + groepID + ", locatie " + locatie);
        Groep gr = status.wedstrijdgroepen.getGroepByNiveau(groepID);
        gr.removeSpeler(sp, locatie);
        gr.renumber();
        if (status.automatisch) {
            maakGroepsindeling();
        }
    }
    /**
     * Verwerk uitslagen tot een nieuwe stand en sla deze op
     * in de verschillende bestanden
     */
    public void verwerkUitslagen() {
    	logger.log(Level.INFO, "Verwerk uitslagen");
    	Uitslagverwerker uv = new Uitslagverwerker();
    	status.resultaatVerwerkt =  uv.verwerkUitslag(status.groepen, status.wedstrijden, status.externGespeeld);
    	status.resultaatVerwerkt.sorteerGroepen(true);
    	logger.log(Level.FINER, () -> status.resultaatVerwerkt.toPrintableString());
    	logger.log(Level.INFO, "en sla uitslagen en status op");
    	new OutputStanden().export(status.resultaatVerwerkt);
    	new OutputUitslagen().export(status.wedstrijden);
    	if (c.exportKNSBRating) new OutputKNSB().export(status.wedstrijden);
    	if (c.exportOSBORating) new OutputOSBO().export(status.wedstrijden);
    	if (c.exportKEIlijst) new OutputKEI().export(status.resultaatVerwerkt);
    	if (c.exportIntekenlijst) new OutputIntekenlijst().export(status.resultaatVerwerkt);

    	new OutputNeuralData().export(status.wedstrijden);

    	saveState(true, "uitslag");
    }

	/**
     * Save state of the application to disk
	 * @param unique if true, a unique file is created with timestamp in filename
	 * @param postfix post fix of filename, before extension. Only used in combination with unique = true
	 */
    public void saveState(boolean unique, String postfix) {
    	// First save keystore
    	try (FileOutputStream keyStoreOutputStream = new FileOutputStream(ksfilename)) {
    		this.ks.store(keyStoreOutputStream, this.keyStorePassword);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			logger.log(Level.WARNING, e1.getMessage(), e1);
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			logger.log(Level.WARNING, e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			logger.log(Level.WARNING, e.getMessage(), e);
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			logger.log(Level.WARNING, e.getMessage(), e);
		}
    	
    	// Second save status
    	try {
			String bestandsnaam = c.statusBestand + ".json";
			logger.log(Level.INFO, "Sla status op in bestand " + bestandsnaam);
			Gson gson = new Gson();
			String jsonString = gson.toJson(status);
			// write converted json data to a file
			FileWriter writer = new FileWriter(bestandsnaam);
			writer.write(jsonString);
			writer.close();

			if (c.saveAdditionalStates && unique) {
				String s = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
				bestandsnaam = c.statusBestand + s + "-" + postfix + ".json";
				logger.log(Level.INFO, "Sla status op in bestand " + bestandsnaam);
				// write converted json data to a file
				String dirName = "R" + status.wedstrijdgroepen.getPeriode() + "-" + status.wedstrijdgroepen.getRonde();
				new File(dirName).mkdirs();
				writer = new FileWriter(dirName + File.separator + bestandsnaam);
				writer.write(jsonString);
				writer.close();
			}
			bestandsnaam = c.configuratieBestand + ".json";
			logger.log(Level.CONFIG, "Sla configuratie op in bestand " + bestandsnaam);
			// write converted json data to a file
			writer = new FileWriter(bestandsnaam);
			jsonString = gson.toJson(c);
			writer.write(jsonString);
			writer.close();

		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public boolean checkStatus() {
		// Check for wrong KNSBnumbers; this is vital!!!
		logger.log(Level.FINE, "Checking for wrong KNSBnumbers");
		try {
//			for (Groep g: status.groepen.getGroepen(Groepen.Sortering.NIVEAU_ASC)) {
			for (Groep g: status.groepen.getGroepen()) {
				logger.log(Level.FINER, () -> "Checking groep " + g.getNaam(g.getNiveau()) + "(" + g.getNiveau() + ") ");
				for (Speler s: g.getSpelers()) {
					logger.log(Level.FINER, () -> "Checking speler " + s.getNaam());
					s.setKNSBnummer(s.getKNSBnummer());
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception in checking Status. Error: " + e.getMessage());
			return false;
			}
		return true;
	}    
    
	public boolean leesStatus() {
		boolean leesstatus;
		leesstatus = leesStatus(c.statusBestand + ".json");
    	if (!leesstatus) logger.log(Level.WARNING, "Inlezen statusbestand mislukt. Nieuw status bestand aanmaken!");		
		return leesstatus;
	}

	public boolean leesStatus(String bestandsnaam) {
		Gson gson = new Gson();
		// Only use setDateFormat when status is broken and Datetime cannot be determined.
		// Normally not needed.
		//Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy HH:mm:ss a").create();
		//
		BufferedReader br;
		try {
	    	logger.log(Level.INFO, "Lees status uit bestand (inclusief path) " + bestandsnaam);
			br = new BufferedReader(new FileReader(bestandsnaam));
		} catch (Exception e) {
			// Could not read status
			// e.printStackTrace();
			logger.log(Level.WARNING, "Exception in BufferedReader for leesStatus in " + bestandsnaam +  ". Error: " + e.getMessage());
			return false;
		}
		try {
			Status nieuw = gson.fromJson(br, Status.class);
			status = nieuw;	// assure exception is thrown when things go wrong
			// Check for wrong KNSBnumbers; this is vital!!!
			logger.log(Level.FINE, "Aantal groepen in status is "+ status.groepen.getAantalGroepen());
			return true;
		} catch (Exception e) {
			// Could not read status
			logger.log(Level.WARNING, e.getMessage(), e);
			logger.log(Level.WARNING, "Exception in parsing content of leesStatus " + bestandsnaam +  ". Error: " + e.getMessage());
			return leesStatus(bestandsnaam, "");
		}
	}

	public boolean leesStatus(String bestandsnaam, String datetimeformat) {
		if (datetimeformat=="") {
			datetimeformat = "MMM d, yyyy, HH:mm:ss a";
		}
		Gson gson = new GsonBuilder().setDateFormat(datetimeformat).create();
		BufferedReader br;
		try {
	    	logger.log(Level.INFO, "Lees status uit bestand (inclusief path) " + bestandsnaam);
			br = new BufferedReader(new FileReader(bestandsnaam));
		} catch (Exception e) {
			// Could not read status
			// e.printStackTrace();
			logger.log(Level.WARNING, "Exception in BufferedReader for leesStatus in " + bestandsnaam +  ". Error: " + e.getMessage());
			return false;
		}
		try {
			Status nieuw = gson.fromJson(br, Status.class);
			status = nieuw;	// assure exception is thrown when things go wrong
			return true;
		} catch (Exception e) {
			// Could not read status
			logger.log(Level.WARNING, e.getMessage(), e);
			logger.log(Level.WARNING, "Exception in parsing content of leesStatus " + bestandsnaam +  ". Error: " + e.getMessage());
			return false;
		}
	}	
	
	public void leesBestand(String bestandsnaam) {
		if (!leesStatus(bestandsnaam))
			leesGroepen(bestandsnaam);

	}


	public void leesConfiguratie() {
		try {
			String bestandsnaam = c.configuratieBestand + ".json";
	    	logger.log(Level.CONFIG, "Lees configuratie uit bestand " + bestandsnaam);
			Gson gson = new Gson();
			BufferedReader br = new BufferedReader(new FileReader(bestandsnaam));
			c = gson.fromJson(br, Configuratie.class);
			if (c == null) {
				c = new Configuratie();
			} else {
				c.Update();
			}
			LoggingConfig.setLevel(c.debugLevel);
		} catch (IOException e) {
			// Could not read status
		}
	}

    public void exportWedstrijdschema() {
    	logger.log(Level.INFO, "Creeer bestanden met wedstrijden");
    	new OutputExcel().export(status.wedstrijden);
    	new OutputSpeelschema().export(status.wedstrijden);
        //voorspelUitslagen();
    }

    public ArrayList<Speler> getExterneSpelers() {
    	return status.externGespeeld;
    }

    /**
     * Voeg externe speler toe
     * @param naam Naam of initialen
     * @return De toegeveogde speler
     */
    public Speler addExterneSpeler(String naam) {
    	if (status.externGespeeld == null) status.externGespeeld = new ArrayList<>();
    	if (naam != null && naam.length() == 2) {
    		Speler s = getSpelerOpInitialen(naam);
    		if (s != null) status.externGespeeld.add(s);
    		return s;
    	} else if (naam != null && naam.length() > 2) {
    		Speler s = getSpelerOpNaam(naam);
    		if (s != null) status.externGespeeld.add(s);
    		return s;
    	} else {
    		// ongeldige naam
    		return null;
    	}
    }

    /**
     * Wis lijst met externe spelers
     */
    public void wisExterneSpelers() {
    	status.externGespeeld = new ArrayList<>();
    }

    /**
     * Vind speler in alle groepen op naam
     * @param naam
     * @return
     */
    public Speler getSpelerOpNaam(String naam) {
    	if (status.groepen != null) {
//    		for (Groep groep : status.groepen.getGroepen(Groepen.Sortering.NIVEAU_ASC)) {
        	for (Groep groep : status.groepen.getGroepen()) {
    			for (Speler speler : groep.getSpelers()) {
    				if (speler.getNaam().equals(naam)) {
    					return speler;
    				}
    			}
    		}
    	}
    	return null;
    }

    /**
     * Vind speler in alle groepen op initialen
     * @param naam
     * @return
     */
    public Speler getSpelerOpInitialen(String naam) {
    	if (status.groepen != null) {
//    		for (Groep groep : status.groepen.getGroepen(Groepen.Sortering.NIVEAU_ASC)) {
        	for (Groep groep : status.groepen.getGroepen()) {
    			for (Speler speler : groep.getSpelers()) {
    				if (speler.getInitialen().equals(naam)) {
    					return speler;
    				}
    			}
    		}
    	}
    	return null;
    }

    /**
     * Ga over naar de volgende ronde.
     * Uitslag van deze ronde wordt de start van de volgende
     */
    public void volgendeRonde() {
    	if (status.resultaatVerwerkt != null) {
    		status.groepen = status.resultaatVerwerkt;
			status.wedstrijdgroepen = null;
			status.wedstrijden = null;
			status.resultaatVerwerkt = null;
			status.externGespeeld = null;

			int ronde = status.groepen.getRonde();
			int periode = status.groepen.getPeriode();
			ronde += 1;
	        if (ronde > c.rondes) {
	        	ronde = 1;
	        	periode++;
	        	if (periode > c.perioden) periode = 1;
	        	resetPunten();
	        }
	        // Altijd ronde en periode instellen
	        status.groepen.setRonde(ronde);
	        status.groepen.setPeriode(periode);
	        // Als nieuwe periode, aanwezigheidspunten resetten en sorteren op rating
			if (ronde == 1) {
				resetAanwezigheidspunt();
//				for (Groep groep : status.groepen.getGroepen(Groepen.Sortering.NIVEAU_ASC)) {
				for (Groep groep : status.groepen.getGroepen()) {
					// sorteer aflopend op rating
					groep.sorteerRating(false);
				}
			}
			// Iedereen aanwezig zetten
//			for (Groep groep : status.groepen.getGroepen(Groepen.Sortering.NIVEAU_ASC)) {
			for (Groep groep : status.groepen.getGroepen()) {
				for (Speler s : groep.getSpelers()) {
					s.setAanwezig(true);
				}
			}
			// Automatisch verder
			setAutomatisch(true);
			maakGroepsindeling();
    	}
    }

    /**
     * Alle spelers in betreffende groep aan/afwezig.
     * Groep wordt inverse status van max
     */
    public void setAlleSpelersAanwezigheid(int groepID) {
    	Groep groep = getGroepByID(groepID);
    	if (groep != null) {
    		int aan = 0;
    		int af = 0;
    		for (Speler s : groep.getSpelers()) {
    			if (s.isAanwezig()) aan++; else af++;
    		}
    		boolean newstatus = (aan >= af) ? false : true;
    		for (Speler s : groep.getSpelers()) {
    			s.setAanwezig(newstatus);
    		}
    	}
    }

    /**
     * Speler naar hogere groep verplaatsen
     * @param groepID Huidige groep
     * @param speler Speler
     * @param locatie huidige locatie in huidige groep
     */
	public void spelerNaarHogereGroep(int groepID, Speler speler, int locatie) {
		Groep huidigeGroep = status.wedstrijdgroepen.getGroepByNiveau(groepID);
		Groep hogereGroep = status.wedstrijdgroepen.getGroepByNiveau(groepID+1);
		if (huidigeGroep == null || hogereGroep == null) return;
		logger.log(Level.INFO, "Verplaats " + speler.getNaam() + " van " + huidigeGroep.getNaam() + " naar " + hogereGroep.getNaam());
		hogereGroep.addSpelerHoudNiveau(speler);
		hogereGroep.renumber();
		huidigeGroep.removeSpeler(speler, locatie);
		huidigeGroep.renumber();
	}

    /**
     * Speler naar lagere groep verplaatsen in wedstrijdgroepen
     * @param groepID Huidige groep
     * @param speler Speler
     * @param locatie huidige locatie in huidige groep
     */
	public void spelerNaarLagereGroep(int groepID, Speler speler, int locatie) {
		Groep huidigeGroep = status.wedstrijdgroepen.getGroepByNiveau(groepID);
		Groep lagereGroep = status.wedstrijdgroepen.getGroepByNiveau(groepID-1);
		if (huidigeGroep == null || lagereGroep == null) return;
		logger.log(Level.INFO, "Verplaats " + speler.getNaam() + " van " + huidigeGroep.getNaam() + " naar " + lagereGroep.getNaam());
		lagereGroep.addSpeler(speler, 0); //vooraan plaatsen
		lagereGroep.renumber();
		huidigeGroep.removeSpeler(speler, locatie);
		lagereGroep.renumber();
	}

	public String getLaatsteExport() {
		return laatsteExport;
	}

	public void setLaatsteExport(String laatsteExport) {
		this.laatsteExport = laatsteExport;
	}

	/**
	 * Sorteer in aanwezigheidsgroep op rating
	 * @param groepID
	 */
	public void sorteerGroepOpRating(int groepID, Boolean toggle) {
		Groep groep = status.groepen.getGroepByNiveau(groepID);
		if (groep != null) {
			groep.sorteerRating(toggle);
			groep.renumber();
		}
	}

	/**
	 * Sorteer in aanwezigheidsgroep op Punten
	 * @param groepID
	 * #param toggle toggle sorting order
	 */
	public void sorteerGroepOpPunten(int groepID, Boolean toggle) {
		Groep groep = status.groepen.getGroepByNiveau(groepID);
		if (groep != null) {
			groep.sorteerPunten(toggle,false);
			groep.renumber();
	        if (status.automatisch) {
	            maakGroepsindeling();
	        }
	    }
	}
	
	/** Sorteer in groepen op niveau
	 * @param 
	 */
	public void sorteeropNiveau() {
//		status.groepen.sorteerGroepen();
//		status.groepen.sorteerNiveau(Sortering.NIVEAU_DESC);
	}

	/**
	 * Reset de punten in alle aanwezigheidsgroepen
	 */
	public void resetPunten() {
		status.groepen.resetPunten();
	}

	/**
	 * Reset de KEI punten in alle aanwezigheidsgroepen
	 */
	public void resetKEIPunten() {
		status.groepen.resetKEIPunten();
	}

	/**
	 * Exporteer naar external API
	 */
	public void exporteerNaarExternalAPI() {
		logger.log(Level.INFO, "Exporteer naar external API gekozen");
		logger.log(Level.FINE, "Er zijn " + c.externalAPIs.size() + " in de actieve configuratie opgenomen.");
		logger.log(Level.FINE, "Huidige periode : " + this.getGroepen().getPeriode());
		logger.log(Level.FINE, "Huidige rondde : " + this.getGroepen().getRonde());
		int vorigeperiode = Utils.vorigePeriode(c.perioden, c.rondes, this.getGroepen().getPeriode(), this.getGroepen().getRonde());
		int vorigeronde = Utils.vorigeRonde(c.perioden, c.rondes, this.getGroepen().getPeriode(), this.getGroepen().getRonde());
		logger.log(Level.FINE, "Vorige periode : " + vorigeperiode);
		logger.log(Level.FINE, "Vorige rondde : " + vorigeronde);
		for (APIConfig config : c.externalAPIConfigs.apiconfigs){
			//try {
			try {
		        pw = new String(this.getPassword(config.getId().toString(),  c.salt));
			} catch (GeneralSecurityException e2) {
				// TODO Auto-generated catch block
				logger.log(Level.WARNING, e2.getMessage(), e2);
			} catch (DestroyFailedException e2) {
				// TODO Auto-generated catch block
				logger.log(Level.WARNING, e2.getMessage(), e2);
			}
        
					// Edit template tekst
					txtLes = config.getTemplate();
					// Edit template tekst
					String txtLes = config.getTemplate();
					logger.log(Level.FINEST, "Template = " + txtLes);
//					Window parentWindow = SwingUtilities.getWindowAncestor(this.getClass());
					LesTekstDialoog lt  = new LesTekstDialoog("Edit template voor Lestekst");
					lt.setLesTekst(txtLes);
					lt.setVisible(true);
					txtLes = lt.getLesTekst();
					Boolean cancel = lt.getCanceled();
					if (!cancel) {
						logger.log(Level.FINEST, "Aangepaste txtLes : " + txtLes);				
						c.externalAPIs.export(config.getURL(), config.getPagePath(), config.getUserName(), pw, config.getLoginPath(), txtLes, vorigeperiode, vorigeronde);
					} else {
						logger.log(Level.INFO, "Canceled. No exported!");				
					}
//					logger.log(Level.INFO, "Export API fictief done");
						}
	}

	
	
	
	public void getRequest() {
		for (APIConfig config : c.externalAPIConfigs.apiconfigs){
			try {
					c.externalAPIs.getRequest("https://schaakrating.nl");
			}
			finally {}
		}	
	}
	
	
	/**
	 * Verwijder gebruiker van API
	 */
	public void externalAPIDeleteUsers() {
		try {
			//c.externalAPIs.verwijderGebruikers(c.plone52URL, c.plone52UserName, new String(this.getPassword("Plone52Password", c.salt)));
		//} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//} catch (DestroyFailedException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		}
		finally {
			
		}
	}
	/**
	 * Schuif speler door
	 * @param groepID Groep ID van huidige groep
	 * @param spelerID speler ID
	 */
	public void doorschuiven(int groepID, int spelerID) {
		Groep huidigeGroep = status.groepen.getGroepByNiveau(groepID);
		Groep hogereGroep = status.groepen.getGroepByNiveau(groepID+1);
		if (huidigeGroep != null && hogereGroep != null) {
			Speler s = huidigeGroep.getSpelerByID(spelerID+1);
			huidigeGroep.removeSpeler(s, spelerID);
			Speler nieuweSpeler = new Speler(s);
			nieuweSpeler.setPunten(IJCController.c().startPunten[groepID+1]);
			hogereGroep.addSpeler(nieuweSpeler);
			huidigeGroep.renumber();
			hogereGroep.renumber();
			logger.log(Level.INFO, "Speler " + s.getNaam() + " doorgeschoven naar groep " + Groep.geefNaam(groepID+1));
		}
	}

	/**
	 * Schuif speler terug naar een groep lager
	 * @param groepID Groep ID van huidige groep
	 * @param spelerID speler ID
	 */
	public void terugschuiven(int groepID, int spelerID) {
		Groep huidigeGroep = status.groepen.getGroepByNiveau(groepID);
		Groep lagereGroep = status.groepen.getGroepByNiveau(groepID-1);
		if (huidigeGroep != null && lagereGroep != null) {
			Speler s = huidigeGroep.getSpelerByID(spelerID+1);
			huidigeGroep.removeSpeler(s, spelerID);
			Speler nieuweSpeler = new Speler(s);
			nieuweSpeler.setPunten(IJCController.c().startPunten[groepID-1]);
			lagereGroep.addSpeler(nieuweSpeler);
			huidigeGroep.renumber();
			lagereGroep.renumber();
			logger.log(Level.INFO, "Speler " + s.getNaam() + " teruggeschoven naar groep " + Groep.geefNaam(groepID+1));
		}
	}

	public static boolean terugschuiven(Groep huidigeGroep, Groep lagereGroep) {
		try {
			if (huidigeGroep != null && lagereGroep != null) {
				for (Speler s : huidigeGroep.getSpelers()) {
					huidigeGroep.removeSpeler(s);
					Speler nieuweSpeler = new Speler(s);
					nieuweSpeler.setPunten(IJCController.c().startPunten[lagereGroep.getNiveau()]);
					lagereGroep.addSpeler(nieuweSpeler);
					huidigeGroep.renumber();
					lagereGroep.renumber();
					logger.log(Level.INFO, "Speler " + s.getNaam() + " teruggeschoven naar groep " + lagereGroep.getNaam());
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Stumbled on problems with moving players form removed group to lower group");
			return false;
		}
		return true;
	}

	/**
	 * Geen een groepen indeler. Type is afhankelijk van de settings
	 * in Configuratie. Dynamisch wisselen tussen indelers is mogelijk.
	 * @return
	 */
	public GroepenIndelerInterface getIndeler() {
		return new GroepenIndelerFactory().getIndeler();
	}

	public void importeerSpelers(String absolutePath) {
		new ImportSpelers().importeerSpelers(absolutePath);
		status.groepen.hernummerGroepen();

	}

	public void start() {
        if (!leesStatusBestand()) leesGroepen();
        if (isAutomatisch()) maakGroepsindeling();
	}

	public void voorspelUitslagen() {
		logger.log(Level.INFO, "Voorspel uitslagen");
		Voorspeller v = new Voorspeller();
		v.initialiseer();
		String directory = "R" + status.wedstrijden.getPeriode() + "-" + status.wedstrijden.getRonde();
		String bestand =  "R" + status.wedstrijden.getPeriode() + "-" + status.wedstrijden.getRonde() + ".arff";
		new OutputNeuralData().export(status.wedstrijden, bestand);
		try {
			v.voorspel(directory + File.separator + bestand);
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public void wisZwartWitVoorkeur() {
//		for (Groep groep : status.groepen.getGroepen(Groepen.Sortering.NIVEAU_ASC)) {
		for (Groep groep : status.groepen.getGroepen()) {
			for (Speler speler : groep.getSpelers()) {
				speler.setWitvoorkeur(0);
			}
		}
	}

	public ArrayList<Speler> getNietKNSBLeden() {
		ArrayList<Speler> lijst = new ArrayList<>(); 
//		for (Groep groep : status.groepen.getGroepen(Groepen.Sortering.NIVEAU_ASC)) {
		for (Groep groep : status.groepen.getGroepen()) {
			for (Speler speler : groep.getSpelers()) {
				if (!speler.isKNSBLid()) {
					lijst.add(speler);
				}
			}
		}
		return lijst;
	}

	/**
	 * Exporteer het complete systeem naar een ZIP-bestand.
	 * Dit bevat status.json, configuratie.json, keystore.ks en alle rondedirectories.
	 * @param zipFilePath het pad naar het ZIP-bestand dat aangemaakt moet worden
	 */
	public void exportSystem(String zipFilePath) {
		logger.log(Level.INFO, "Exporteer systeem naar " + zipFilePath);
		
		// Save current version into configuratie before exporting
		c.appVersion = nl.amity.ijc_ui.util.Versie.get();
		
		// First save current state (including updated configuratie with version)
		saveState(false, null);
		
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
			// Add core files
			addFileToZip(zos, c.statusBestand + ".json");
			addFileToZip(zos, c.configuratieBestand + ".json");
			addFileToZip(zos, ksfilename);
			
			// Add all round directories (R*-*)
			File currentDir = new File(".");
			File[] roundDirs = currentDir.listFiles((dir, name) -> name.matches("R\\d+-\\d+"));
			if (roundDirs != null) {
				for (File dir : roundDirs) {
					if (dir.isDirectory()) {
						addDirectoryToZip(zos, dir, dir.getName());
					}
				}
			}
			
			logger.log(Level.INFO, "Systeem export voltooid: " + zipFilePath);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Fout bij exporteren van systeem", e);
			throw new RuntimeException("Export mislukt: " + e.getMessage(), e);
		}
	}

	private void addFileToZip(ZipOutputStream zos, String filePath) throws IOException {
		File file = new File(filePath);
		if (file.exists()) {
			logger.log(Level.FINE, "Voeg toe aan ZIP: " + filePath);
			zos.putNextEntry(new ZipEntry(file.getName()));
			try (FileInputStream fis = new FileInputStream(file)) {
				byte[] buffer = new byte[4096];
				int len;
				while ((len = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}
			}
			zos.closeEntry();
		} else {
			logger.log(Level.WARNING, "Bestand niet gevonden voor export: " + filePath);
		}
	}

	private void addDirectoryToZip(ZipOutputStream zos, File dir, String zipPath) throws IOException {
		File[] files = dir.listFiles();
		if (files == null) return;
		
		for (File file : files) {
			if (file.isFile()) {
				String entryName = zipPath + File.separator + file.getName();
				logger.log(Level.FINE, "Voeg toe aan ZIP: " + entryName);
				zos.putNextEntry(new ZipEntry(entryName));
				try (FileInputStream fis = new FileInputStream(file)) {
					byte[] buffer = new byte[4096];
					int len;
					while ((len = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
				}
				zos.closeEntry();
			}
		}
	}

	/**
	 * Importeer het complete systeem uit een ZIP-bestand.
	 * Valideert de versie, extraheert alle bestanden, en laadt de nieuwe data.
	 * @param zipFilePath het pad naar het ZIP-bestand
	 * @return true als de import succesvol was
	 * @throws RuntimeException als de versie niet compatibel is of het ZIP-bestand ongeldig is
	 */
	public boolean importSystem(String zipFilePath) {
		logger.log(Level.INFO, "Importeer systeem uit " + zipFilePath);
		
		File zipFile = new File(zipFilePath);
		if (!zipFile.exists()) {
			throw new RuntimeException("ZIP-bestand niet gevonden: " + zipFilePath);
		}
		
		// First, extract configuratie.json to check version compatibility
		String importedVersion = null;
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().equals(c.configuratieBestand + ".json")) {
					// Read the configuratie.json content to check version
					byte[] buffer = new byte[(int) entry.getSize()];
					int bytesRead = zis.read(buffer, 0, buffer.length);
					String configContent = new String(buffer, 0, bytesRead);
					
					// Parse version from configuratie.json
					Gson gson = new Gson();
					Configuratie importedConfig = gson.fromJson(configContent, Configuratie.class);
					if (importedConfig != null && importedConfig.appVersion != null) {
						importedVersion = importedConfig.appVersion;
					}
					break;
				}
				zis.closeEntry();
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Fout bij lezen van ZIP-bestand", e);
			throw new RuntimeException("Kan ZIP-bestand niet lezen: " + e.getMessage(), e);
		}
		
		// Check version compatibility
		String currentVersion = nl.amity.ijc_ui.util.Versie.get();
		if (importedVersion != null && !importedVersion.equals(currentVersion)) {
			logger.log(Level.WARNING, "Versie mismatch: geïmporteerd=" + importedVersion + ", huidig=" + currentVersion);
			throw new RuntimeException(
				"Versie incompatibiliteit gedetecteerd!\n" +
				"Geïmporteerde versie: " + importedVersion + "\n" +
				"Huidige versie: " + currentVersion + "\n\n" +
				"Importeer alleen exportbestanden van dezelfde versie."
			);
		}
		
		// Extract all files
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				String entryName = entry.getName();
				logger.log(Level.FINE, "Extraheer: " + entryName);
				
				// Skip directory entries
				if (entry.isDirectory()) {
					new File(entryName).mkdirs();
					zis.closeEntry();
					continue;
				}
				
				// Determine output path
				File outputFile = new File(entryName);
				
				// Create parent directories if needed
				File parentDir = outputFile.getParentFile();
				if (parentDir != null && !parentDir.exists()) {
					parentDir.mkdirs();
				}
				
				// Extract file
				try (FileOutputStream fos = new FileOutputStream(outputFile)) {
					byte[] buffer = new byte[4096];
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
				}
				zis.closeEntry();
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Fout bij extractie van ZIP-bestand", e);
			throw new RuntimeException("Kan ZIP-bestand niet extraheren: " + e.getMessage(), e);
		}
		
		logger.log(Level.INFO, "Bestanden geëxtraheerd, laad nu de nieuwe data");
		
		// Reload the system with imported data
		// Reset configuration
		c = new Configuratie();
		leesConfiguratie();
		
		// Reset status
		status = new Status();
		if (!leesStatus()) {
			logger.log(Level.WARNING, "Kon statusbestand niet laden na import");
			throw new RuntimeException("Kan statusbestand niet laden na import");
		}
		
		// Reload keystore
		try (InputStream data = new FileInputStream(ksfilename)) {
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(data, keyStorePassword);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Fout bij laden van keystore na import", e);
			try {
				ks.load(null);
			} catch (Exception e2) {
				logger.log(Level.WARNING, "Fout bij laden van lege keystore", e2);
			}
		}
		
		// Fix groups if needed
		checkStatus();
		if (status.groepen != null && status.groepen.getAantalGroepen() != c.aantalGroepen) {
			fix_groepen(status.groepen, c.aantalGroepen);
		}
		if (status.wedstrijdgroepen != null && status.wedstrijdgroepen.getAantalGroepen() != c.aantalGroepen) {
			fix_groepen(status.wedstrijdgroepen, c.aantalGroepen);
		}
		if (status.groepen != null) {
			status.groepen.sorteerGroepen(true);
		}
		
		logger.log(Level.INFO, "Import voltooid");
		return true;
	}
}
