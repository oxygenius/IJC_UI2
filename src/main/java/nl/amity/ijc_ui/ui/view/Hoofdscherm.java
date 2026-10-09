/**
 * Copyright (C) 2016-2018 Lars Dam, Leo van der Meulen
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
 *
 */
package nl.amity.ijc_ui.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.JDialog;

import nl.amity.ijc_ui.Configuratie;
//import nl.amity.ijc_ui.SpelerDBImport;
import nl.amity.ijc_ui.data.external.api.APIConfig;
import nl.amity.ijc_ui.data.groepen.Groep;
import nl.amity.ijc_ui.data.groepen.Groepen;
import nl.amity.ijc_ui.data.groepen.Speler;
import nl.amity.ijc_ui.ui.control.IJCController;
import nl.amity.ijc_ui.ui.control.Status;
import nl.amity.ijc_ui.ui.model.SpelersModel;
import nl.amity.ijc_ui.ui.model.WedstrijdModel;
import nl.amity.ijc_ui.ui.model.WedstrijdSpelersModel;
import nl.amity.ijc_ui.ui.util.Utils;
import nl.amity.ijc_ui.util.Versie;
import nl.amity.ijc_ui.util.VersieChecker;

/**
 * Structuur van de GUI: JFrame Hoofdscherm (this) met hoofdPanel (BorderLayout):
 * - Noord (BorderLayout.NORTH): bovenbalk met de knoppenbalk (knoppen in een
 *   BoxLayout) en het ronde-label, in een FlowLayout.
 * - Midden (BorderLayout.CENTER): JTabbedPane tabs, per groep tabs[i]:
 *   JPanel panels[i] met GridLayout(1, 3) — drie even brede kolommen
 *   (aanwezigheid, spelers, wedstrijden). Elke kolom heeft twee kopregels
 *   (JTextField, niet bewerkbaar/focusbaar) en een JScrollPane met een JTable
 *   die de volledige hoogte vult (setFillsViewportHeight).
 * Tabellenkolommen: smalle kolommen (codes, rating, punten) hebben een vaste
 * breedte; alleen de naamkolommen groeien mee (flexibleColumSize) tot een maximum
 * zodat namen en getallen bij elkaar blijven. Het venster kan niet kleiner worden
 * dan MINIMALE_GROOTTE (1150x670) en schaalt mee in alle richtingen.
 *
 * @author Leo van der Meulen
 * @author Lars Dam
 */
public class Hoofdscherm extends JFrame {

	//Colors and fonts
	private static final Color light_green = new Color(200, 255, 200);
	private static final Color light_red = new Color(255, 200, 200);
	private static final Font courierFont = new Font("Courier New", Font.PLAIN, 11);
	private static final Color indigo = new Color(75,0,130);
	private static final Color purple = new Color(128,0,128);
	private static final Color violetred = new Color(199,21,133);
	private static final Color deeppink = new Color(255,20,147);

	private static final long serialVersionUID = -2154845989579570030L;
	private final static Logger logger = Logger.getLogger(Hoofdscherm.class.getName());

	private String appVersion = Versie.get();
	/** Kleinste venstergrootte waarbij alle kolommen en knoppen leesbaar zijn. */
	private static final Dimension MINIMALE_GROOTTE = new Dimension(1150, 670);
	private JPanel hoofdPanel;
	private JTabbedPane tabs;
	private JPanel[] panels;
	private JLabel rondeLabel;
	private JButton automatischButton;
	private JButton wedstrijdgroepButton;
	private JButton speelschemaButton;
	private JButton bewerkspeelschemaButton;
	private JButton exportButton;
	private JButton uitslagButton;
	private JButton externenButton;
	private JButton updatestandButton;
	private JTextField[] jTFZWbalansvoor;
	private JTextField[] jTFZWbalansna;
	private JScrollPane[] leftScrollPane;
	private JScrollPane[] centerLeftScrollPane;
	private JScrollPane[] centerRightScrollPane;
	private JTable[] aanwezigheidsTabel;
	private JTable[] wedstrijdspelersTabel;
	private JTable[] wedstrijdenTabel;
	private int aantal;

	private IJCController controller;

	/**
	 * Creates new form MainWindow
	 */
	public Hoofdscherm() {
		initComponents();
        initSizes();
	}

	private void initComponents() {

		controller = IJCController.getInstance();
		aantal = Groep.getAantalGroepen();
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle(IJCController.c().verenigingNaam + " - " + IJCController.c().appTitle + " - versie " + this.appVersion);
		logger.log(Level.CONFIG, "Java version: " + System.getProperty("java.runtime.version"));
		logger.log(Level.INFO, IJCController.c().verenigingNaam + " - " + IJCController.c().appTitle + " - versie " + this.appVersion);
		// Knoppenbalk boven, tabbladen vullen de rest en groeien mee met het venster
		hoofdPanel = new javax.swing.JPanel(new BorderLayout());
		addButtons();
		addMenubar();

		tabs = new JTabbedPane();
		tabs.setTabPlacement(JTabbedPane.TOP);

		panels = new JPanel[aantal];
		leftScrollPane = new JScrollPane[aantal];
		jTFZWbalansvoor = new JTextField[aantal];
		jTFZWbalansna = new JTextField[aantal];
		centerLeftScrollPane = new JScrollPane[aantal];
		centerRightScrollPane = new JScrollPane[aantal];
		aanwezigheidsTabel = new JTable[aantal];
		wedstrijdspelersTabel = new JTable[aantal];
		wedstrijdenTabel = new JTable[aantal];

		/*
		 * for (int i = 0; i < aantal; ++i) {
		 * panels[i] = makePanel();
		 * fillGroupPanel(panels[i], i);
		 * 
		 * tabs.addTab(Groep.geefNaam(i) + " (" + Groep, null, panels[i],
		 * "Gegevens van  " + Groep.geefNaam(i)); }
		 */
		int i =0;
//		Groepen groepen = controller.sorteeropNiveau();
		// getGroepen(...) sorteert de groepen en spelers (bijwerking); de inhoud van tab i is
		// altijd groep i (fillGroupPanel en getGroepByID werken op niveau), dus titel en
		// tooltip moeten ook bij groep i horen en niet bij de i-de groep in deze lijst.
		for (Groep g : controller.getStatus().groepen.getGroepen(Groepen.Sortering.NIVEAU_DESC)) {
//		for (Groep g : controller.getStatus().groepen.getGroepen()) {
			panels[i] = makePanel();
		    fillGroupPanel(panels[i], i);
		    tabs.addTab(Groep.geefNaam(i), null, panels[i], "Gegevens van " + Groep.geefNaam(i) + " (" + i + ")");
		    //tabs.addTab(g.getNaam(), null, panels[i]);
		    logger.log(Level.FINEST, () -> g.getNaam() + " (" + g.getNiveau() + ")");
		    i++;
		}
		logger.log(Level.FINEST, "i = " + i);
		
		
		hoofdPanel.add(tabs, BorderLayout.CENTER);
		hoofdPanel.setBorder(new EmptyBorder(0, 8, 8, 8));
		this.add(hoofdPanel);

		updateUpdateStandButton();

		pack();

		this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent event){
                    controller.saveState(false, null);
                }
        });

	}

	private void addButtons() {
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));

		// Button voor automatisch doorvoeren wijzigingen ja/nee
		automatischButton = new JButton("Auto");
		automatischButton.addActionListener(e -> actieAutomatisch());
		buttonPane.add(automatischButton);

		// Button voor bepalen wedstrijdgroepen
		wedstrijdgroepButton = new JButton("1a. Maak wedstrijdgroep");
		wedstrijdgroepButton.addActionListener(e -> actieMaakWedstrijdgroep());
		buttonPane.add(wedstrijdgroepButton);

		// Button voor maken speelschema
		speelschemaButton = new JButton("1b. Maak speelschema");
		speelschemaButton.addActionListener(e -> actieMaakSpeelschema());
		buttonPane.add(speelschemaButton);

		// Button voor bewerken speelschema
		bewerkspeelschemaButton = new JButton("1c. Bewerk speelschema");
		bewerkspeelschemaButton.addActionListener(e -> actieBewerkSchema());
		buttonPane.add(bewerkspeelschemaButton);

		buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
		buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
		buttonPane.add(new JSeparator(SwingConstants.VERTICAL));

		exportButton = new JButton("2. Export");
		exportButton.addActionListener(e -> actieExport());
		buttonPane.add(exportButton);

		buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
		buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
		buttonPane.add(new JSeparator(SwingConstants.VERTICAL));

		uitslagButton = new JButton("3a. Uitslagen");
		uitslagButton.addActionListener(e -> actieVoerUitslagenIn());
		buttonPane.add(uitslagButton);

		externenButton = new JButton("3b. Extern");
		externenButton.addActionListener(e -> actieExterneSpelers());
		buttonPane.add(externenButton);

		buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
		buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
		buttonPane.add(new JSeparator(SwingConstants.VERTICAL));

		updatestandButton = new JButton("4. Update stand");
		updatestandButton.addActionListener(e -> actieUpdateStand());
		buttonPane.add(updatestandButton);

		updateUpdateStandButton();

		buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonPane.setBackground(Color.white);

		rondeLabel = new JLabel();
		updateRondeLabel();
		updateAutomatisch(controller.isAutomatisch());
		JPanel bovenbalk = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
		bovenbalk.add(buttonPane);
		bovenbalk.add(rondeLabel);
		hoofdPanel.add(bovenbalk, BorderLayout.NORTH);
	}

	/**
	 * Bepaal de kleur van de Update Stand button.
	 * Deze is groen als alle wedstrijden een uitslag hebben.
	 * Geef tevens de groepen een * in hun tabnaam die alle uitslagen ingevoerd hebben.
	 */
	public void updateUpdateStandButton() {
		if (controller.getWedstrijden() != null)
		updatestandButton.setBackground((controller.getWedstrijden() != null) && (controller.getWedstrijden().isUitslagBekend())?light_green:hoofdPanel.getBackground());
		if (tabs != null) {
			for (int i = 0; i < tabs.getTabCount(); i++) {
				if (IJCController.getI().getWedstrijden()!= null) {
					if (!(IJCController.getI().getWedstrijden().getGroepswedstrijdenNiveau(i) == null)) {
						if (IJCController.getI().getWedstrijden().getGroepswedstrijdenNiveau(i).isUitslagBekend()) {
							//tabs.setTitleAt(i, Groep.geefNaam(i) + " ("+i+") *");
							tabs.setTitleAt(i, Groep.geefNaam(i) + "*");
							//tabs.setToolTipText("Gegevens van " + Groep.geefNaam(i)+ " (" + i + ")");
						} else {
							//tabs.setTitleAt(i, Groep.geefNaam(i) + " ("+i+")");
							tabs.setTitleAt(i, Groep.geefNaam(i));
							//tabs.setToolTipText("Gegevens van " + Groep.geefNaam(i)+ " (" + i + ")");
						}
					} else {
						//.setTitleAt(i, Groep.geefNaam(i) + " ("+i+") *");
						tabs.setTitleAt(i, Groep.geefNaam(i) + "*");
						//tabs.setToolTipText("Gegevens van " + Groep.geefNaam(i)+ " (" + i + ")");
					}
				}
			}
		}
		hoofdPanel.repaint();
	}

	/**
	 * Update het label met ronde en periode informatie
	 */
	public void updateRondeLabel() {
		String rondeText = "<html>Periode: " + controller.getGroepen().getPeriode() + "<BR>";
		rondeText += "Ronde: " + controller.getGroepen().getRonde() + "</HTML>";
		rondeLabel.setText(rondeText);
	}

	/**
	 * Update textfield met ZW balans voor spelen ronde
	 */
	public void updateZWbalansvoor(int index) {
		try {
			String bal = String.format(Locale.US, "%.0f", controller.getWedstrijdGroepByID(index).getZWbalansvoor());
			String ZWbalansText = "ZW Balans voor deze ronde is " + bal;
			jTFZWbalansvoor[index].setText(ZWbalansText);
			this.repaint();
		}
		catch (NullPointerException npex) {
			logger.log(Level.WARNING, "Nullpointer Exception in update ZWbalansvoor.");
		}
		catch (Exception ex) {
			logger.log(Level.WARNING, "Other Exception in update ZWbalansvoor.");			
		}
	}

	/**
	 * Update textfield met ZW balans voor spelen ronde
	 */
	public void updateZWbalansvoor() {
		for (int index=0;index<aantal;index++) {
			updateZWbalansvoor(index);
		}
	}

	public void updateZWbalansna(int index) {
		try {
		String bal = String.format(Locale.US, "%.0f", controller.getWedstrijdGroepByID(index).getZWbalansna());
		String ZWbalansText = "ZW Balans na deze ronde is " + bal + "";
		jTFZWbalansna[index].setText(ZWbalansText);
		}
		catch (NullPointerException npex) {
			logger.log(Level.WARNING, "Nullpointer Exception in update ZWbalansna.");
		}
		catch (Exception ex) {
			logger.log(Level.WARNING, "Other Exception in update ZWbalansna.");			
		}
	}

	public void updateZWbalansna() {
		for (int index=0;index<aantal;index++) {
			updateZWbalansna(index);
		}
	}

		public void updateAutomatisch(boolean newState) {
		controller.setAutomatisch(newState);
		if (controller.isAutomatisch()) {
			automatischButton.setBackground(Color.GREEN);
			bewerkspeelschemaButton.setBackground(light_green);
			speelschemaButton.setBackground(light_green);
			wedstrijdgroepButton.setBackground(light_green);

		} else {
			automatischButton.setBackground(Color.RED);
			bewerkspeelschemaButton.setBackground(light_red);
			speelschemaButton.setBackground(light_red);
			wedstrijdgroepButton.setBackground(light_red);
		}
	}

	private void addMenubar() {
		// Menu bar met 1 niveau
		JMenuBar menubar = new JMenuBar();
		JMenu filemenu = new JMenu("Bestand");
		// File menu
		JMenuItem item = new JMenuItem("Openen...");
		item.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		Hoofdscherm hs = this;
		item.addActionListener(e -> {
			// Create a file chooser
			final JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			// In response to a button click:
			int returnVal = fc.showOpenDialog(hs);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				logger.log(Level.INFO, "Opening: " + file.getAbsolutePath() + ".");
				controller.leesBestand(file.getAbsolutePath());
				//updateAutomatisch(true);
				//controller.maakGroepsindeling();
				updateRondeLabel();
				updateUpdateStandButton();
				updateAutomatisch(controller.isAutomatisch());
				hs.repaint();
			}
		});
		filemenu.add(item);
		item = new JMenuItem("Opslaan");
		item.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		item.addActionListener(e -> controller.saveState(true, "save"));
		filemenu.add(item);
		item = new JMenuItem("Exporteer systeem...");
		item.addActionListener(e -> actieExportSysteem());
		filemenu.add(item);
		item = new JMenuItem("Importeer systeem...");
		item.addActionListener(e -> actieImportSysteem());
		filemenu.add(item);
		filemenu.addSeparator();
		item = new JMenuItem("Instellingen...");
		item.addActionListener(e -> actieInstellingen());
		item.setAccelerator(KeyStroke.getKeyStroke('I', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		filemenu.add(item);
		filemenu.addSeparator();
		item = new JMenuItem("Afsluiten");
		item.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		item.addActionListener(e -> {
			controller.saveState(false, null);
			System.exit(EXIT_ON_CLOSE);
		});
		filemenu.add(item);
		menubar.add(filemenu);

		JMenu spelermenu = new JMenu("Speler");

		item = new JMenuItem("Nieuwe speler");
		item.setAccelerator(KeyStroke.getKeyStroke('N', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		item.addActionListener(e -> {
			actieNieuweSpeler(null, null);
			hoofdPanel.repaint();
		});
		spelermenu.add(item);
		menubar.add(spelermenu);

		item = new JMenuItem("Importeer spelers");
		item.addActionListener(e -> {
			// Create a file chooser
			final JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			// In response to a button click:
			if (fc.showOpenDialog(hs) == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				logger.log(Level.INFO, "Opening: " + file.getAbsolutePath() + ".");
				controller.importeerSpelers(file.getAbsolutePath());
				hs.repaint();
			}
		});
		spelermenu.add(item);

		item = new JMenuItem("Speler geschiedenis");
		item.addActionListener(e -> new SpelersScherm().setVisible(true));
		spelermenu.add(item);

		spelermenu.addSeparator();
		item = new JMenuItem("Wis Zwart/Wit voorkeur");
		item.addActionListener(e -> {
			if (Bevesting.bevestig(hs, "Weet u zeker dat de zwart/wit-voorkeur van alle spelers gewist moet worden?")) {
				controller.wisZwartWitVoorkeur();
				hoofdPanel.repaint();
			}
		});
		spelermenu.add(item);

		menubar.add(spelermenu);

		JMenu indelingMenu = new JMenu("Indeling");
		item = new JMenuItem("Automatisch aan/uit");
		item.addActionListener(e -> actieAutomatisch());
		indelingMenu.add(item);
		item = new JMenuItem("Maak wedstrijdgroep");
		item.addActionListener(e -> actieMaakWedstrijdgroep());

		indelingMenu.add(item);
		item = new JMenuItem("Maak speelschema");
		item.addActionListener(e -> actieMaakSpeelschema());
		indelingMenu.add(item);
		item = new JMenuItem("Bewerk speelschema");
		item.addActionListener(e -> {
			updateAutomatisch(false);
			// ResultaatDialoog
			actieBewerkSchema();
		});

		indelingMenu.add(item);
		indelingMenu.addSeparator();
		item = new JMenuItem("Export");
		item.addActionListener(e -> actieExport());
		indelingMenu.add(item);
		indelingMenu.addSeparator();
		item = new JMenuItem("Vul uitslagen in");
		item.addActionListener(e -> actieVoerUitslagenIn());
		indelingMenu.add(item);
		item = new JMenuItem("Externe spelers");
		item.addActionListener(e -> actieExterneSpelers());
		indelingMenu.add(item);
		item = new JMenuItem("Maak nieuwe stand");
		item.addActionListener(e -> actieUpdateStand());
		indelingMenu.add(item);
		indelingMenu.addSeparator();
		item = new JMenuItem("Volgende ronde");
		item.addActionListener(e -> actieVolgendeRonde());
		indelingMenu.add(item);
		menubar.add(indelingMenu);

		JMenu overigmenu = new JMenu("Overig");

		item = new JMenuItem("Reset punten");
		item.addActionListener(e -> {
			if (Bevesting.bevestig(hs, "Weet u zeker dat de punten van alle spelers op de startwaarde gezet moeten worden?\nDit kan niet ongedaan gemaakt worden.")) {
				controller.resetPunten();
				hoofdPanel.repaint();
			}
		});
		overigmenu.add(item);

		item = new JMenuItem("Reset KEI punten");
		item.addActionListener(e -> {
			if (Bevesting.bevestig(hs, "Weet u zeker dat de KEI-punten van alle spelers op nul gezet moeten worden?\nDit kan niet ongedaan gemaakt worden.")) {
				controller.resetKEIPunten();
				hoofdPanel.repaint();
			}
		});
		overigmenu.add(item);

		item = new JMenuItem("Exporteer actieve API's");
		item.addActionListener(e -> {
			for (APIConfig config : IJCController.c().externalAPIConfigs.apiconfigs){
				// Edit template tekst
///				String txtLes = config.getTemplate();
///				logger.log(Level.INFO, "Template = " + txtLes);
//				Window parentWindow = SwingUtilities.getWindowAncestor(this.getClass());
///				LesTekstDialoog lt  = new LesTekstDialoog("Edit template voor Lestekst");
///				lt.setLesTekst(txtLes);
///				lt.setVisible(true);
///				txtLes = lt.getLesTekst();
///				logger.log(Level.INFO, "Aangepaste txtLes : " + txtLes);				
			}				
			controller.exporteerNaarExternalAPI();
			hoofdPanel.repaint();
		});
		overigmenu.add(item);

		// Ontwikkelaarsfunctie: alleen zichtbaar als het logniveau op FINE of gedetailleerder staat
		// (Instellingen > Debug). "Admin - Delete users API" is verwijderd: de functie was uitgeschakeld.
		final JMenuItem testRequestItem = new JMenuItem("Test request");
		testRequestItem.addActionListener(e -> {
			controller.getRequest();
			hoofdPanel.repaint();
		});
		overigmenu.add(testRequestItem);
		overigmenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				testRequestItem.setVisible(isOntwikkelaarsmodus());
			}
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});

		menubar.add(overigmenu);

		JMenu helpmenu = new JMenu("Help");

		item = new JMenuItem("Over IJC_UI");
		item.addActionListener(e -> actieOver());
		helpmenu.add(item);

		item = new JMenuItem("Gebruiksaanwijzing");
		item.addActionListener(e -> openUrl("https://github.com/oxygenius/IJC_UI2"));
		helpmenu.add(item);

		item = new JMenuItem("Contact");
		item.addActionListener(e -> openUrl("https://github.com/oxygenius/IJC_UI2/issues"));
		helpmenu.add(item);

		helpmenu.addSeparator();
		item = new JMenuItem("Controleer op updates...");
		item.addActionListener(e -> actieControleerUpdates());
		helpmenu.add(item);

		menubar.add(helpmenu);

		this.setJMenuBar(menubar);
	}

	/**
	 * @return true als het ingestelde logniveau FINE of gedetailleerder is (Instellingen > Debug)
	 */
	private static boolean isOntwikkelaarsmodus() {
		try {
			return Level.parse(IJCController.c().debugLevel.toUpperCase()).intValue() <= Level.FINE.intValue();
		} catch (RuntimeException e) {
			return false;
		}
	}

	public JTable getAanwezigheidsTabel(int index) {
		if ((index >= 0) && (index < aantal)) {
			return aanwezigheidsTabel[index];
		} else {
			return null;
		}
	}

	public JTable getWedstrijdspelersTabel(int index) {
		if ((index >= 0) && (index < aantal)) {
			return wedstrijdspelersTabel[index];
		} else {
			return null;
		}
	}

	public JTable getWedstrijdenTabel(int index) {
		if ((index >= 0) && (index < aantal)) {
			return wedstrijdenTabel[index];
		} else {
			return null;
		}
	}

	public void initSizes() {
		logger.log(Level.FINEST, "Maak alle componenten van het juiste formaat");
		setMinimumSize(MINIMALE_GROOTTE);
		setSize(MINIMALE_GROOTTE);
		setLocationRelativeTo(null);
		int ii =0;
		try {
			for (int i = 0; i < aantal; ++i) {
				ii = i;
				aanwezigheidsTabel[i].setFillsViewportHeight(true);
				wedstrijdspelersTabel[i].setFillsViewportHeight(true);
				wedstrijdenTabel[i].setFillsViewportHeight(true);
				// Kolombreedtes: vast voor getallen en codes, meegroeiend voor namen
				fixedColumSize(aanwezigheidsTabel[i].getColumnModel().getColumn(0), 38);
				fixedColumSize(aanwezigheidsTabel[i].getColumnModel().getColumn(1), 22);
				flexibleColumSize(aanwezigheidsTabel[i].getColumnModel().getColumn(2), 122, 220);
				fixedColumSize(aanwezigheidsTabel[i].getColumnModel().getColumn(3), 30);
				fixedColumSize(aanwezigheidsTabel[i].getColumnModel().getColumn(4), 40);
				fixedColumSize(aanwezigheidsTabel[i].getColumnModel().getColumn(5), 47);

				fixedColumSize(wedstrijdspelersTabel[i].getColumnModel().getColumn(0), 25);
				flexibleColumSize(wedstrijdspelersTabel[i].getColumnModel().getColumn(1), 125, 220);
				fixedColumSize(wedstrijdspelersTabel[i].getColumnModel().getColumn(2), 33);
				fixedColumSize(wedstrijdspelersTabel[i].getColumnModel().getColumn(3), 20);
				fixedColumSize(wedstrijdspelersTabel[i].getColumnModel().getColumn(4), 20);
				flexibleColumSize(wedstrijdspelersTabel[i].getColumnModel().getColumn(5), 90, 200);

				fixedColumSize(wedstrijdenTabel[i].getColumnModel().getColumn(0), 25);
				flexibleColumSize(wedstrijdenTabel[i].getColumnModel().getColumn(1), 115, 220);
				fixedColumSize(wedstrijdenTabel[i].getColumnModel().getColumn(2), 10);
				flexibleColumSize(wedstrijdenTabel[i].getColumnModel().getColumn(3), 115, 220);
				fixedColumSize(wedstrijdenTabel[i].getColumnModel().getColumn(4), 50);
			}
		} catch (NullPointerException npe) {
			logger.log(Level.WARNING, "Null Pointer Exception probably in one of the Tables. Error: " + npe.getMessage() + ". i = " + ii);
		}
	}

	/**
	 * Kolom die meegroeit als er ruimte is, maar niet verder dan maximaal: anders komt
	 * een naam bij een breed venster te ver van zijn getallen (rating, punten) te staan.
	 */
	private void flexibleColumSize(TableColumn c, int minimaal, int maximaal) {
		c.setMinWidth(minimaal);
		c.setPreferredWidth(minimaal);
		c.setMaxWidth(maximaal);
	}

	private void fixedColumSize(TableColumn c, int width) {
		Utils.fixedColumSize(c, width);
	}

	protected JPanel makePanel() {
		JPanel panel = new JPanel(false);
		// Drie even brede kolommen die meegroeien met het venster
		panel.setLayout(new GridLayout(1, 3));
		return panel;
	}

	/**
	 * Kopregel boven een tabel. Een JTextField (geen JLabel), omdat de ZW-balansregels
	 * later via setText worden bijgewerkt; niet bewerkbaar en niet focusbaar.
	 */
	private JTextField maakKopregel(String tekst) {
		JTextField kop = new JTextField(tekst);
		kop.setEditable(false);
		kop.setFocusable(false);
		kop.setOpaque(false);
		kop.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
		kop.setFont(kop.getFont().deriveFont(Font.BOLD, 13f));
		kop.setHorizontalAlignment(SwingConstants.CENTER);
		return kop;
	}

	/** ZW-balansregel met oorspronkelijke instellingen — geen vetgedrukte of vergrote lettertype. */
	private JTextField maakZWbalansregel(String tekst) {
		JTextField kop = new JTextField(tekst);
		kop.setEditable(false);
		kop.setFocusable(false);
		kop.setOpaque(false);
		kop.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		return kop;
	}

	/** Kolom met twee kopregels bovenaan en de tabel (in scrollpane) eronder over de volle hoogte. */
	private JPanel maakKolom(JTextField kop1, JTextField kop2, JScrollPane tabel) {
		JPanel koppen = new JPanel(new GridLayout(2, 1));
		koppen.add(kop1);
		koppen.add(kop2);
		JPanel kolom = new JPanel(new BorderLayout());
		kolom.add(koppen, BorderLayout.NORTH);
		kolom.add(tabel, BorderLayout.CENTER);
		kolom.setBorder(new EmptyBorder(5, 5, 5, 5));
		return kolom;
	}

	protected void fillGroupPanel(JPanel panel, final int index) {
		leftScrollPane[index] = new javax.swing.JScrollPane();
		leftScrollPane[index].setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		centerLeftScrollPane[index] = new javax.swing.JScrollPane();
		centerLeftScrollPane[index].setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		jTFZWbalansvoor[index] = new JTextField();
		centerRightScrollPane[index] = new javax.swing.JScrollPane();
		centerRightScrollPane[index].setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		String[] columnToolTips = {
			    "De-/Selecter alle spelers",
			    null,
			    null,
			    null,
			    "Sorteer",
			    "Sorteer"
		};
		
		aanwezigheidsTabel[index] = new JTable(new SpelersModel(index, panel)) {
			private static final long serialVersionUID = -8293073016982337108L;

			@Override
		    //Implement table header tool tips.
		    protected JTableHeader createDefaultTableHeader() {
		        return new JTableHeader(columnModel) {
		            
		        	private static final long serialVersionUID = -8293073016255337108L;
		        	public String getToolTipText(MouseEvent e) {
		                String tip = null;
		                java.awt.Point p = e.getPoint();
		                int index = columnModel.getColumnIndexAtX(p.x);
		                int realIndex = 
		                        columnModel.getColumn(index).getModelIndex();
		                return columnToolTips[realIndex];
		            }
		        };
			}	
		
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);
				// Tooltip
				if (c instanceof JComponent && (column >0)) {
					JComponent jc = (JComponent) c;
					SpelersModel model = (SpelersModel) getModel();
					jc.setToolTipText(model.getToolTip(row, column).toString());
				}

				// Alternate row color
				if (!isRowSelected(row)) {
					c.setBackground(row % 2 == 0 ? Color.WHITE : Utils.RIJKLEUR_ONEVEN);
				}
				return c;
			}
		};

		aanwezigheidsTabel[index].getTableHeader().addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
		        int col = aanwezigheidsTabel[index].columnAtPoint(e.getPoint());
		        String name = aanwezigheidsTabel[index].getColumnName(col);
		        logger.log(Level.FINEST, () -> "Column index selected " + col + " " + name);
		        int groepID = tabs.getSelectedIndex();
		        switch (col) {
		        case 0:
		        	logger.log(Level.FINEST, "Zet aanwezigheid alle spelers");
		        	controller.setAlleSpelersAanwezigheid(groepID);
		        	if (controller.isAutomatisch()) {
		        		controller.maakGroepsindeling();
		        	}
		        	updateZWbalansvoor();
		        	updateZWbalansna();
		        	repaint();
		        	break;
		        case 4:
		        	logger.log(Level.FINEST, "Sorteer op rating (toggle) in de groep");
		        	controller.sorteerGroepOpRating(groepID, true);
		        	if (controller.isAutomatisch()) {
			        	// Opnieuw indelen op basis van nieuwe volgorde
		        		controller.maakGroepsindeling();
		        		repaint();
		        	}
		        	break;
		        case 5:
		        	logger.log(Level.FINEST, "Sorteer op punten (toggle) in de groep");
		        	controller.sorteerGroepOpPunten(groepID, true);
		        	if (controller.isAutomatisch()) {
			        	// Opnieuw indelen op basis van nieuwe volgorde
		        		controller.maakGroepsindeling();
		        		repaint();
		        	}
		        	break;
		        	
		        }
		    }
		});

		aanwezigheidsTabel[index].addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				int r = aanwezigheidsTabel[index].rowAtPoint(e.getPoint());
				if (r >= 0 && r < aanwezigheidsTabel[index].getRowCount()) {
					aanwezigheidsTabel[index].setRowSelectionInterval(r, r);
				} else {
					aanwezigheidsTabel[index].clearSelection();
				}

				int rowindex = aanwezigheidsTabel[index].getSelectedRow();
				if (rowindex < 0) {
					return;
				}
				final int groepID = tabs.getSelectedIndex();
				final Speler s = controller.getGroepByID(groepID).getSpelerByID(rowindex + 1);
				final Speler s2 = controller.getGroepByID(groepID).getSpelerByID(rowindex + 2);
				if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
					JPopupMenu popup = new JPopupMenu();
					JMenuItem menuItem = new JMenuItem("Bewerk speler");
					menuItem.addActionListener(evt -> {
						BewerkSpelerDialoog rd = new BewerkSpelerDialoog(new JFrame(), "Bewerk Speler", s, true,
								s.getId());
						rd.addWindowListener(new WindowAdapter() {
							@Override
							public void windowClosed(WindowEvent e) {
								hoofdPanel.repaint();
								// do something...
							}

						});
						rd.setVisible(true);
					});
					popup.add(menuItem);

					menuItem = new JMenuItem("Voeg speler toe, na ...");
					menuItem.addActionListener(evt -> actieNieuweSpeler(s, s2));
					popup.add(menuItem);

					menuItem = new JMenuItem("Verwijder Speler");
					popup.add(menuItem);
					menuItem.addActionListener(evt -> {
						// JDialog.setDefaultLookAndFeelDecorated(true);
						String tekst = "Weet u zeker dat \"" + s.getNaam() + "\" verwijderd moet worden?";
						String[] options = { "Ja", "Nee" };
						int response = JOptionPane.showOptionDialog(null, tekst, "Bevestig", 0,
								JOptionPane.WARNING_MESSAGE, null, options, null);
						if (response == JOptionPane.YES_OPTION) {
							controller.verwijderSpeler(groepID, s, s.getId() - 1);
						}
						hoofdPanel.repaint();
					});

					popup.addSeparator();
					menuItem = new JMenuItem("Doorschuiven Speler");
					popup.add(menuItem);
					menuItem.addActionListener(evt -> {
						if (Bevesting.bevestig(hoofdPanel, "Weet u zeker dat \"" + s.getNaam() + "\" doorgeschoven moet worden naar een hogere groep?")) {
							controller.doorschuiven(groepID, s.getId()-1);
						}
						hoofdPanel.repaint();
					});

					menuItem = new JMenuItem("Terugschuiven Speler");
					popup.add(menuItem);
					menuItem.addActionListener(evt -> {
						if (Bevesting.bevestig(hoofdPanel, "Weet u zeker dat \"" + s.getNaam() + "\" teruggeschoven moet worden naar een lagere groep?")) {
							controller.terugschuiven(groepID, s.getId()-1);
						}
						hoofdPanel.repaint();
					});

					popup.show(e.getComponent(), e.getX(), e.getY());
				}
				updateZWbalansvoor();
				updateZWbalansna();
				hoofdPanel.repaint();
			}
		});

		wedstrijdspelersTabel[index] = new JTable(new WedstrijdSpelersModel(index, panel)) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);
				WedstrijdSpelersModel model = (WedstrijdSpelersModel) getModel();
				// Tooltip
				if (c instanceof JComponent) {
					JComponent jc = (JComponent) c;
					jc.setToolTipText(model.getToolTip(row, column).toString());
				}

				// Alternate row color
				if (!isRowSelected(row)) {
					c.setBackground(row % 2 == 0 ? Color.WHITE : Utils.RIJKLEUR_ONEVEN);
				}
				// Alternative font
				if (column > 1) {
					c.setFont(courierFont);
				}
				if (model.isDoorgeschoven(row)) {
					c.setFont(new Font(c.getFont().getName(), Font.ITALIC, c.getFont().getSize()));
					c.setForeground(Color.BLUE);
				} else {
					c.setForeground(Color.BLACK);
				}
				if (column == 5) {
					//c.setForeground(indigo);
				}
				return c;
			}
		};

		wedstrijdspelersTabel[index].addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				int r = wedstrijdspelersTabel[index].rowAtPoint(e.getPoint());
				if (r >= 0 && r < wedstrijdspelersTabel[index].getRowCount()) {
					wedstrijdspelersTabel[index].setRowSelectionInterval(r, r);
				} else {
					wedstrijdspelersTabel[index].clearSelection();
				}

				int rowindex = wedstrijdspelersTabel[index].getSelectedRow();
				if (rowindex < 0) {
					return;
				}
				final int groepID = tabs.getSelectedIndex();
				final Speler s = controller.getWedstrijdGroepByID(groepID).getSpelerByID(rowindex + 1);
				if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
					JPopupMenu popup = new JPopupMenu();
					JMenuItem menuItem = new JMenuItem("Verwijder Speler");
					popup.add(menuItem);
					menuItem.addActionListener(evt -> {
						// JDialog.setDefaultLookAndFeelDecorated(true);
						String tekst = "Weet u zeker dat \"" + s.getNaam() + "\" verwijderd moet worden?";
						String[] options = { "Ja", "Nee" };
						int response = JOptionPane.showOptionDialog(null, tekst, "Bevestig", 0,
								JOptionPane.WARNING_MESSAGE, null, options, null);
						if (response == JOptionPane.YES_OPTION) {
							controller.verwijderWedstrijdSpeler(groepID, s, s.getId() - 1);
						}
						updateZWbalansvoor(index);
						updateZWbalansna(index);
						hoofdPanel.repaint();
					});
					menuItem = new JMenuItem("Speler naar hogere groep");
					popup.add(menuItem);
					menuItem.addActionListener(evt -> {
						if (groepID + 1 >= aantal) {
							FoutMelding.info(hoofdPanel, "Speler is al in de hoogste groep.");
						} else {
							controller.spelerNaarHogereGroep(groepID, s, s.getId() - 1);
							updateZWbalansvoor(index);
							updateZWbalansna(index);
							hoofdPanel.repaint();
						}
					});
					menuItem = new JMenuItem("Speler naar lagere groep");
					popup.add(menuItem);
					menuItem.addActionListener(evt -> {
						if (groepID < 1) {
							FoutMelding.info(hoofdPanel, "Speler is al in de laagste groep.");
						} else {
							controller.spelerNaarLagereGroep(groepID, s, s.getId() - 1);
							updateZWbalansvoor(index);
							updateZWbalansna(index);
							hoofdPanel.repaint();
						}
					});
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		wedstrijdenTabel[index] = new JTable(new WedstrijdModel(index, panel)) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);
				WedstrijdModel model = (WedstrijdModel) getModel();
				// Alternate row color
				if (!isRowSelected(row)) {
					c.setBackground(row % 2 == 0 ? Color.WHITE : Utils.RIJKLEUR_ONEVEN);
				}
				// Alternatief font bij dubbele wedstrijden
				if (model.isDubbeleWedstrijd(row)) {
					c.setForeground(Color.RED);
				} else 
					if (model.isEerderGespeeld(row) < 99) {
					//c.setForeground(Color.BLUE);

					switch (model.isEerderGespeeld(row)) {
					case 1:
						c.setForeground(indigo);
						break;
					case 2:
						c.setForeground(purple);
						break;
					case 3:
						c.setForeground(violetred);
						break;
					case 4:
						c.setForeground(deeppink);
						break;
					}
				} else {
					c.setForeground(Color.BLACK);
				}
				JComponent jc = (JComponent) c;
				jc.setToolTipText(model.getToolTip(row, column).toString());
				return c;
			}
		};
		wedstrijdenTabel[index].getModel().addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent arg0) {
				updateUpdateStandButton();
				updateZWbalansvoor(index);
				updateZWbalansna(index);
				hoofdPanel.repaint();
			}
		});


		leftScrollPane[index].setViewportView(aanwezigheidsTabel[index]);
		centerLeftScrollPane[index].setViewportView(wedstrijdspelersTabel[index]);
		centerRightScrollPane[index].setViewportView(wedstrijdenTabel[index]);

		// Drie kolommen met elk twee kopregels en een tabel die de rest van de hoogte vult
		JTextField jTFaanwezigheid = maakKopregel("Aanwezigheid in de " + Groep.geefNaam(index));
		jTFZWbalansvoor[index] = maakZWbalansregel("ZW Balans");
		panel.add(maakKolom(jTFaanwezigheid, jTFZWbalansvoor[index], leftScrollPane[index]));
		updateZWbalansvoor(index);

		JTextField jTFwedstrijdgroep = maakKopregel("Spelers die spelen in de " + Groep.geefNaam(index));
		panel.add(maakKolom(jTFwedstrijdgroep, maakKopregel(""), centerLeftScrollPane[index]));

		JTextField jTFwedstrijden = maakKopregel("Indeling van wedstrijden in de " + Groep.geefNaam(index));
		jTFZWbalansna[index] = maakZWbalansregel("ZW Balans");
		panel.add(maakKolom(jTFwedstrijden, jTFZWbalansna[index], centerRightScrollPane[index]));
		updateZWbalansna(index);

		panel.setBorder(new EmptyBorder(1, 1, 1, 1));
		pack();

	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
	}

	public void actieExport() {
		updateAutomatisch(false);
		controller.exportWedstrijdschema();
		controller.saveState(true, "export");
		hoofdPanel.repaint();
	}

	/**
	 * Exporteer het complete systeem naar een ZIP-bestand
	 */
	public void actieExportSysteem() {
		final JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
		fc.setDialogTitle("Exporteer systeem");
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		fc.setSelectedFile(new File("IJC_UI2_export_" + timestamp + ".zip"));
		
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String filePath = file.getAbsolutePath();
			// Ensure .zip extension
			if (!filePath.toLowerCase().endsWith(".zip")) {
				filePath += ".zip";
			}
			try {
				controller.exportSystem(filePath);
				JOptionPane.showMessageDialog(this, "Systeem geëxporteerd naar:\n" + filePath, "Export voltooid", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Export mislukt:\n" + ex.getMessage(), "Fout", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Importeer het complete systeem uit een ZIP-bestand
	 */
	public void actieImportSysteem() {
		final JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
		fc.setDialogTitle("Importeer systeem");
		fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("ZIP-bestanden", "zip"));
		
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String filePath = file.getAbsolutePath();
			
			// Ask for confirmation before importing
			if (!Bevesting.bevestig(this, "Weet u zeker dat u het systeem wilt importeren?\n\n"
					+ "Alle huidige gegevens worden vervangen door de geïmporteerde gegevens.\n"
					+ "Dit kan niet ongedaan gemaakt worden.\n\n"
					+ "Bestand: " + file.getName())) {
				return;
			}
			
			try {
				controller.importSystem(filePath);
				// Refresh the UI after import
				updateRondeLabel();
				updateUpdateStandButton();
				updateAutomatisch(controller.isAutomatisch());
				hoofdPanel.repaint();
				JOptionPane.showMessageDialog(this, "Systeem geïmporteerd uit:\n" + filePath, "Import voltooid", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Import mislukt:\n" + ex.getMessage(), "Fout", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Dialoog voor het bewerken van het speelschema
	 */
	public void actieBewerkSchema() {
		updateAutomatisch(false);
		hoofdPanel.repaint();
		int groep = tabs.getSelectedIndex();
		WedstrijdschemaDialoog dialoog = new WedstrijdschemaDialoog(new JFrame(), "Wedstrijden", groep);
		dialoog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				hoofdPanel.repaint();
			}

		});
		dialoog.setVisible(true);
	}

	public void actieAutomatisch() {
		updateAutomatisch(!controller.isAutomatisch());
		if (controller.isAutomatisch()) controller.maakGroepsindeling();
		hoofdPanel.repaint();
	}

	public void actieNieuweSpeler(final Speler s, final Speler s2) {
		int groepID = tabs.getSelectedIndex();
		Speler nieuw = new Speler();
		nieuw.setGroep(groepID);
		if ((s != null) && (s2 != null)) {
			nieuw.setPunten((s.getPunten() + s2.getPunten()) / 2);
			nieuw.setRating((s.getRating() + s2.getRating()) / 2);
		} else {
			// Onderaan altijd standaard rating
			nieuw.setRating(IJCController.c().startRating[groepID]);
			nieuw.setPunten(IJCController.c().startPunten[groepID]);
		}
		int locatie = (s != null) ? s.getId() : 0;
		BewerkSpelerDialoog rd = new BewerkSpelerDialoog(new JFrame(), "Nieuwe speler", nieuw,
				false, locatie);
		rd.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				controller.getGroepByID(groepID).renumber();
				hoofdPanel.repaint();
			}

		});
		rd.setVisible(true);
	}

	public void actieMaakWedstrijdgroep() {
		controller.setAutomatisch(false);
		controller.maakGroepsindeling(tabs.getSelectedIndex());
		hoofdPanel.repaint();
	}

	public void actieMaakSpeelschema() {
		updateAutomatisch(false);
		controller.maakWedstrijden(tabs.getSelectedIndex());
		updateZWbalansvoor(tabs.getSelectedIndex());
		updateZWbalansna(tabs.getSelectedIndex());
		hoofdPanel.repaint();
	}

	public void actieVoerUitslagenIn() {
		hoofdPanel.repaint();
		updateAutomatisch(false);
		ResultaatDialoog rd = new ResultaatDialoog(new JFrame(),
				"Wedstrijdresultaten: 1=wit wint, 0=zwart wint, 2=remise (7/8/9 reglementaire uitslag)", tabs.getSelectedIndex());
		rd.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				updateUpdateStandButton();
				hoofdPanel.repaint();
			}

		});
		rd.setVisible(true);
	}

	public void actieExterneSpelers() {
		hoofdPanel.repaint();
		updateAutomatisch(false);
		ExternDialog ed = new ExternDialog(new JFrame(), "Externe spelers");
		ed.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				hoofdPanel.repaint();
			}
		});
		ed.setVisible(true);
	}

	public void actieUpdateStand() {
		controller.setAutomatisch(false);
		controller.verwerkUitslagen();
		hoofdPanel.repaint();
		new UitslagDialoog().createDialog();
	}

	public void actieVolgendeRonde() {
		Status s = controller.getStatus();
		if (s.resultaatVerwerkt == null) {
			FoutMelding.info(this, "De uitslagen van deze ronde zijn nog niet verwerkt.\n"
					+ "Voer eerst de uitslagen in (3a) en kies daarna \"4. Update stand\".");
			return;
		}
		Groepen g = controller.getGroepen();
		if (!Bevesting.bevestig(this, "Weet u zeker dat u naar de volgende ronde wilt gaan?\n"
				+ "Periode " + g.getPeriode() + ", ronde " + g.getRonde() + " wordt dan afgesloten.")) {
			return;
		}
//		try {
//			SpelerDBImport dbi = new SpelerDBImport();
//			dbi.importStatusObjectWithDBSession(s);
//		}
//		catch (Exception ex) {
//			logger.log(Level.INFO, "Exception: " +  ex.getMessage());
//			//Utils.stacktrace(ex);
//		}
		try {
			controller.volgendeRonde();
			updateAutomatisch(true);
			updateRondeLabel();
			updateZWbalansvoor();
			updateZWbalansna();
			updateUpdateStandButton();
		} catch (Exception ex) {
			FoutMelding.fout(this, "Naar de volgende ronde gaan is mislukt.", ex);
		}
		hoofdPanel.repaint();
	}

	public void actieInstellingen() {
		hoofdPanel.repaint();
		ConfigurationDialog dialoog = new ConfigurationDialog(new JFrame(), "Configuratie");
		dialoog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				//TODO config naar status doorzetten
				logger.log(Level.FINE, "Adjusting status to changes in config");
				Status s = controller.getStatus();
				Configuratie c = IJCController.c();
				int s_groepen= s.groepen.getAantalGroepen();
				int c_groepen= c.aantalGroepen;
				if (s_groepen != c_groepen) {
					controller.fix_groepen(s.groepen, c_groepen);
				}
				aantal = c.aantalGroepen;
				updateUpdateStandButton();
				hoofdPanel.repaint();
			}

		});
		dialoog.setVisible(true);
	}

	/** Toont het Over-dialoog met versie- en auteursinformatie. */
	private void actieOver() {
		String text = "IJC_UI2 – Indelingsprogramma voor interne jeugdcompetitie\n"
				+ "\n"
				+ "Versie: " + appVersion + "\n"
				+ "Copyright © 2016–2026 Leo van der Meulen en Lars Dam\n"
				+ "\n"
				+ "Dit programma valt onder de GNU General Public License versie 3.\n"
				+ "\n"
				+ "Broncode: https://github.com/oxygenius/IJC_UI2";
		JOptionPane.showMessageDialog(this, text, "Over IJC_UI", JOptionPane.INFORMATION_MESSAGE);
	}

	/** Opent de opgegeven URL in de standaardwebbrowser. */
	private void openUrl(String url) {
		try {
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Desktop.getDesktop().browse(new URI(url));
			} else {
				JOptionPane.showMessageDialog(this, "Kan de browser niet starten.\n"
						+ "Open de link handmatig:\n\n" + url, "Fout", JOptionPane.WARNING_MESSAGE);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Fout bij het openen van de browser:\n"
					+ e.getMessage() + "\n\nLink: " + url, "Fout", JOptionPane.ERROR_MESSAGE);
		}
	}

	/** Controleert of er een nieuwere versie beschikbaar is op GitHub. */
	private void actieControleerUpdates() {
		String currentVersion = appVersion;
		if (currentVersion == null || currentVersion.equals("onbekend")) {
			JOptionPane.showMessageDialog(this, "De huidige versie kan niet bepaald worden.",
					"Updates", JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Toon een wacht-dialoog tijdens het ophalen van de nieuwste versie
		JProgressBar progressBar = new JProgressBar(0, 100);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Controleren op updates..."));
		panel.add(progressBar);

		JDialog waitDialog = new JDialog(this, "Updates", true);
		waitDialog.add(panel);
		waitDialog.pack();
		waitDialog.setLocationRelativeTo(this);

		SwingWorker<String, ?> worker = new SwingWorker<>() {
			@Override
			protected String doInBackground() {
				return VersieChecker.haalNieuwsteVersie();
			}

			@Override
			protected void done() {
				SwingUtilities.invokeLater(() -> waitDialog.dispose());
				try {
					String latestVersion = get();
					if (latestVersion == null) {
						JOptionPane.showMessageDialog(Hoofdscherm.this,
								"Kan de nieuwste versie niet ophalen van GitHub.\n"
								+ "Controleer uw internetverbinding en probeer het opnieuw.\n\n"
								+ "Huidige versie: " + currentVersion,
								"Updates", JOptionPane.INFORMATION_MESSAGE);
						return;
					}

					int cmp = VersieChecker.compareVersions(currentVersion, latestVersion);
					if (cmp < 0) {
						int response = JOptionPane.showConfirmDialog(Hoofdscherm.this,
								"Er is een nieuwere versie beschikbaar:\n\n"
								+ "Huidige versie:  " + currentVersion + "\n"
								+ "Nieuwste versie: " + latestVersion + "\n\n"
								+ "Wil u de downloadpagina openen?",
								"Nieuwe versie beschikbaar",
								JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (response == JOptionPane.YES_OPTION) {
							openUrl("https://github.com/oxygenius/IJC_UI2/releases/latest");
						}
					} else if (cmp == 0) {
						JOptionPane.showMessageDialog(Hoofdscherm.this,
								"U heeft de nieuwste versie geïnstalleerd.\n\n"
								+ "Versie: " + currentVersion,
								"Updates", JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(Hoofdscherm.this,
								"U heeft een nieuwere versie dan de laatste release.\n\n"
								+ "Huidige versie: " + currentVersion,
								"Updates", JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(Hoofdscherm.this,
							"Fout bij het controleren op updates:\n" + e.getMessage(),
							"Updates", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
		waitDialog.setVisible(true);
	}
}
