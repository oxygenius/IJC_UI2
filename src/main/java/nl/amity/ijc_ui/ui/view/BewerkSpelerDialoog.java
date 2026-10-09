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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import nl.amity.ijc_ui.data.groepen.Groep;
import nl.amity.ijc_ui.data.groepen.Speler;
import nl.amity.ijc_ui.ui.control.IJCController;

/**
 *
 * @author Leo van der Meulen
 */
public class BewerkSpelerDialoog extends JDialog {

	private static final long serialVersionUID = -5297394315846599903L;

	private final static Logger logger = Logger.getLogger(BewerkSpelerDialoog.class.getName());
	private static IJCController controller;
    private Speler speler;
    boolean bestaandeSpeler;
    final int locatie;

    public BewerkSpelerDialoog(Frame frame, String title, Speler sp, boolean bestaand, int loc) {
        super(frame, title);
        this.speler = sp;
        this.bestaandeSpeler = bestaand;
        this.locatie = loc;
    	logger.log(Level.INFO, "Bewerk speler " + sp.toPrintableString());
        controller = IJCController.getInstance();
        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().add(createPanel());
        pack();
        setSize(Math.max(getWidth(), 320), getHeight());
        setLocationRelativeTo(frame);
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(18, 2));
        //ID
        panel.add(new JLabel("ID"));
        final JTextField tfID = new JTextField(String.valueOf(speler.getId()));
        tfID.setEditable(false);
        panel.add(tfID);
        //Naam
        panel.add(new JLabel("Naam"));
        final JTextField tfNaam = new JTextField(speler.getNaam());
        panel.add(tfNaam);
        // Initialen
//        panel.add(new JLabel("Initialen"));
//        final JTextField tfInit = new JTextField(speler.getInitialen());
//        panel.add(tfInit);
        // Wit voorkeur
        panel.add(new JLabel("Witvoorkeur"));
        final JTextField tfWit = new JTextField(String.valueOf((int) speler.getWitvoorkeur()));
        panel.add(tfWit);
        // Groep
        panel.add(new JLabel("Groep"));
        final JTextField tfGroep = new JTextField(Groep.geefNaam(speler.getGroep()));
        tfGroep.setEditable(false);
        panel.add(tfGroep);
        // Rating
        panel.add(new JLabel("Rating"));
        final JTextField tfRating = new JTextField(String.valueOf(speler.getRating()));
        panel.add(tfRating);
        // Punten
        panel.add(new JLabel("Punten"));
        final JTextField tfPunten = new JTextField(String.valueOf(speler.getPunten()));
        panel.add(tfPunten);
        // KEIPunten
        panel.add(new JLabel("KEI Punten"));
        final JTextField tfKeiPunten = new JTextField(String.valueOf(speler.getKeipunten()));
        panel.add(tfKeiPunten);
        // Punten
        panel.add(new JLabel("KEI Kansen"));
        final JTextField tfKeiKansen = new JTextField(String.valueOf(speler.getKeikansen()));
        panel.add(tfKeiKansen);
        // KNSB
        panel.add(new JLabel("KNSB Nummer"));
        final JTextField tfKNSB = new JTextField(String.valueOf(speler.getKNSBnummer()));
        panel.add(tfKNSB);
        // Tegenstanders
        panel.add(new JLabel("Tegenstanders"));
        final JTextField tfTegenstander1 = new JTextField(speler.getTegenstanders()[0]);
        panel.add(tfTegenstander1);
        panel.add(new JLabel(""));
        final JTextField tfTegenstander2 = new JTextField(speler.getTegenstanders()[1]);
        panel.add(tfTegenstander2);
        panel.add(new JLabel(""));
        final JTextField tfTegenstander3 = new JTextField(speler.getTegenstanders()[2]);
        panel.add(tfTegenstander3);
        panel.add(new JLabel(""));
        final JTextField tfTegenstander4 = new JTextField(speler.getTegenstanders()[3]);
        panel.add(tfTegenstander4);
        // Geschiedenis string
        panel.add(new JLabel("Tegenstanders historie"));
        final JTextField tfGeschiedenis = new JTextField(speler.getSpeelgeschiedenis());
        panel.add(tfGeschiedenis);
        // Afwezigheidspunten
        panel.add(new JLabel("Afwezigheidspunt"));
        final JCheckBox cbAfwezigPunt = new JCheckBox("", speler.isAfwezigheidspunt());
        panel.add(cbAfwezigPunt);
        // Aanwezig
        panel.add(new JLabel("Aanwezig"));
        final JCheckBox cbAanwezig = new JCheckBox("", speler.isAanwezig());
        cbAanwezig.setEnabled(false);
        panel.add(cbAanwezig);
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                // Eerst alle getallen controleren, zodat de speler niet half bijgewerkt wordt
                if (tfNaam.getText().trim().isEmpty()) {
                    FoutMelding.info(BewerkSpelerDialoog.this, "Vul een naam in.");
                    tfNaam.requestFocusInWindow();
                    return;
                }
                Integer rating = leesGetal(tfRating, "Rating");
                Integer punten = rating == null ? null : leesGetal(tfPunten, "Punten");
                Integer wit = punten == null ? null : leesGetal(tfWit, "Witvoorkeur");
                Integer keipunten = wit == null ? null : leesGetal(tfKeiPunten, "KEI Punten");
                Integer keikansen = keipunten == null ? null : leesGetal(tfKeiKansen, "KEI Kansen");
                Integer knsb = keikansen == null ? null : leesGetal(tfKNSB, "KNSB Nummer");
                if (knsb == null) {
                    return;
                }
                // Naam
                speler.setNaam(tfNaam.getText().trim());
                // Initialen
                if (speler.getInitialen().length() == 0) {
                	speler.setInitialen();
                }
                 // OUD: speler.setInitialen(tfInit.getText());
                speler.setRating(rating);
                speler.setPunten(punten);
                speler.setWitvoorkeur(wit);
                speler.setKeipunten(keipunten);
                speler.setKeikansen(keikansen);
                speler.setKNSBnummer(knsb);
                // Tegenstanders
                String[] tgn = new String[4];
                tgn[0] = tfTegenstander1.getText();
                tgn[1] = tfTegenstander2.getText();
                tgn[2] = tfTegenstander3.getText();
                tgn[3] = tfTegenstander4.getText();
                speler.setTegenstanders(tgn);
                // Tegenstanders historie (alleen bij wijziging, zodat 'geen historie' (null) niet "" wordt)
                String oudeGeschiedenis = speler.getSpeelgeschiedenis() == null ? "" : speler.getSpeelgeschiedenis();
                if (!tfGeschiedenis.getText().equals(oudeGeschiedenis)) {
                    speler.setSpeelgeschiedenis(tfGeschiedenis.getText());
                }
                // Afwezigheidspunt
                speler.setAfwezigheidspunt(cbAfwezigPunt.isSelected());
                setVisible(false);
                // Als nieuwe speler, dan invoegen.
                if (!bestaandeSpeler) {
                    controller.addSpeler(speler.getGroep(), speler, locatie);
                }
                dispose();
            }
        });
        JButton cancelButton = new JButton("Annuleren");
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                setVisible(false);
                dispose();
            }
        }
        );
        panel.add(okButton);
        panel.add(cancelButton);
        getRootPane().setDefaultButton(okButton);
        return panel;
    }

    /**
     * Lees een geheel getal uit een invoerveld. Bij ongeldige invoer verschijnt een melding
     * en krijgt het veld de focus.
     *
     * @return het getal, of null als de invoer ongeldig is
     */
    private Integer leesGetal(JTextField veld, String naam) {
        try {
            return Integer.valueOf(veld.getText().trim());
        } catch (NumberFormatException e) {
            FoutMelding.info(this, "\"" + veld.getText() + "\" is geen geldig getal voor " + naam + ".");
            veld.requestFocusInWindow();
            veld.selectAll();
            return null;
        }
    }
}
