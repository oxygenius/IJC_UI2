/**
 * Copyright (C) 2024 Lars Dam
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
 * - ...
 */
package nl.amity.ijc_ui.ui.view;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import nl.amity.ijc_ui.io.GroepenReader;
import nl.amity.ijc_ui.ui.control.IJCController;

/**
 * Toont het te bewerken LesTekst template als tekst voor de API export
 *
 * @author Lars.Dam
 *
 */
public class LesTekstDialoog extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4268561726139515755L;

	private final static Logger logger = Logger.getLogger(GroepenReader.class.getName());

	private static IJCController controller;
	JEditorPane jEditorPaneLesTekst = new JEditorPane();




	String lesTekst;
	String lesTekstOrig;
	Boolean cancel = false;
	
	public Boolean getCanceled() {
		return cancel;
	}
	public String getLesTekst() {
		return lesTekst;
	}
	public void setLesTekst(String txtles) {
		lesTekstOrig = txtles;
		lesTekst = lesTekstOrig;
		jEditorPaneLesTekst.setText(lesTekst);
	}

	public LesTekstDialoog(String title) {
        super();
        controller = IJCController.getInstance();
        //frame.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        setModal(true);
        setModalExclusionType(Dialog.ModalExclusionType.NO_EXCLUDE);
        //frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(title);
        setAlwaysOnTop(true);
        setSize(650, 400);
        JPanel panel = new JPanel();
        //panel.setLayout(new GridLayout(8, 2));
        panel.add(new JLabel("Bewerk de tekst en klik op OK voor verwerking."));
        //panel.add(jEditorPaneLesTekst);
    	//Put the editor pane in a scroll pane.
    	JScrollPane editorScrollPane = new JScrollPane(jEditorPaneLesTekst);
    	editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    	editorScrollPane.setPreferredSize(new Dimension(550, 245));
    	editorScrollPane.setMinimumSize(new Dimension(550, 200));
        panel.add(editorScrollPane);
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	lesTekst = jEditorPaneLesTekst.getText();
                setVisible(false);
                //dispose();
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		lesTekst = lesTekstOrig;
        		cancel = true;
        		setVisible(false);
        		//dispose();
        	}
        });
        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		lesTekst = lesTekstOrig;
        		jEditorPaneLesTekst.setText(lesTekst);
        	}
        });
        panel.add(okButton);
        panel.add(cancelButton);
        panel.add(undoButton);
        panel.add(new JTextArea("Gebruik van eenvoudig HTML tags toegestaan (afhankelijk van API)\r\nOverig tags:\r\n%Uitslag% : Hier wordt de uitslag van de huidige ronde in geplaatst\r\n%Stand% : Hier wordt de nieuwe stand geplaatst"));
        getContentPane().add(panel);
        setSize(600, 400);
        //setLocationRelativeTo(frame);
        //setVisible(true);
	}
}
