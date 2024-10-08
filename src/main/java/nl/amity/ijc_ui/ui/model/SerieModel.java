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
 * - ...
 */
package nl.amity.ijc_ui.ui.model;

import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.table.AbstractTableModel;
import nl.amity.ijc_ui.data.groepen.Groep;
import nl.amity.ijc_ui.data.groepen.Speler;
import nl.amity.ijc_ui.data.wedstrijden.Groepswedstrijden;
import nl.amity.ijc_ui.data.wedstrijden.Serie;
import nl.amity.ijc_ui.data.wedstrijden.Wedstrijd;
import nl.amity.ijc_ui.data.wedstrijden.Wedstrijden;
import nl.amity.ijc_ui.ui.control.IJCController;

/**
 * Serie 4 is de set van TrioWedstrijden!!!
 * 
 * @author Leo van der Meulen
 */
public class SerieModel extends AbstractTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private IJCController controller = null;
	private JComponent component;
	private int groepID;
	private int serieID;

	private String[] columnNames = { "", "Wit", "", "", "Zwart" };

	public SerieModel() {
		this(0, 0, null);
	}

	public SerieModel(int groep, int serie, JComponent comp) {
		component = comp;
		groepID = groep;
		serieID = serie;
		init();
	}

	public void init() {
		controller = IJCController.getInstance();
	}

	public void setGroep(int groepID) {
		this.groepID = groepID;
	}

	public int getGroep() {
		return groepID;
	}

	public void setSerie(int serieID) {
		this.serieID = serieID;
	}

	public int getSerie() {
		return serieID;
	}

	@Override
	public int getRowCount() {
		Wedstrijden ws = controller.getWedstrijden();
		int count;
		if (serieID < 3) {
			Serie serie = ws.getGroepswedstrijdenNiveau(groepID).getSerie(serieID);
			count = serie == null ? 0 : serie.getWedstrijden().size();
		} else {
			ArrayList<Wedstrijd> tw = ws.getGroepswedstrijdenNiveau(groepID).getTriowedstrijden();
			count = tw == null ? 0 : tw.size();
		}
		return count;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int column) {
		if (column >= 0 && column < columnNames.length) {
			return columnNames[column];
		} else {
			return "";

		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Wedstrijd ws;
		if (serieID < 3) {
			Serie serie = controller.getWedstrijden().getGroepswedstrijdenNiveau(groepID).getSerie(serieID);
			ws = serie.getWedstrijd(rowIndex);
		} else {
			ws = controller.getWedstrijden().getGroepswedstrijdenNiveau(groepID).getTriowedstrijden().get(rowIndex);
		}
		switch (columnIndex) {
		case 0:
			return new Integer(ws.getWit().getId());
		case 1:
			return ws.getWit().getNaam().substring(0, Math.min(14, ws.getWit().getNaam().length())) + " (" + ws.getWit().getRating() + ")";
		case 2:
			return "-";
		case 3:
			return new Integer(ws.getZwart().getId());
		case 4:
			return ws.getZwart().getNaam().substring(0, Math.min(14, ws.getZwart().getNaam().length())) + " (" + ws.getZwart().getRating() + ")";
		default:
			return null;
		}
	}

	@Override
	public Class<?> getColumnClass(int col) {
		switch (col) {
		case 0:
			return Integer.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		case 3:
			return Integer.class;
		case 4:
			return String.class;
		default:
			return String.class;
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		fireTableCellUpdated(row, col);
		component.repaint();
	}

	public void setSpeler(int wedstrijdID, int spelerID, boolean wit) {
		Groepswedstrijden gws = controller.getWedstrijden().getGroepswedstrijdenNiveau(groepID);
		Wedstrijd ws = null;
		if (serieID == 3) {
			ArrayList<Wedstrijd> trio = gws.getTriowedstrijden();
			ws = trio.get(wedstrijdID);
		} else {
			Serie serie = gws.getSerie(serieID);
			ws = serie.getWedstrijd(wedstrijdID);
			if (ws == null) {
				return;
			}
		}
		Groep groep = controller.getWedstrijdGroepByID(groepID);
		if (groep == null) {
			return;
		}
		Speler s = groep.getSpelerByID(spelerID);
		if (s == null) {
			return;
		}
		if (wit) {
			ws.setWit(s);
		} else {
			ws.setZwart(s);
		}
		component.repaint();
	}

	public void wisSerie(int serieID) {
		Groepswedstrijden gws = controller.getWedstrijden().getGroepswedstrijdenNiveau(groepID);
		if (serieID == 3) {
			gws.setTriowedstrijden(new ArrayList<Wedstrijd>());
		} else {
			gws.getSerie(serieID).wis();
		}
	}

	public void insertWedstrijd(int id) {
		Wedstrijd w = new Wedstrijd(0, Speler.dummySpeler(groepID), Speler.dummySpeler(groepID), 0);
		Groepswedstrijden gws = controller.getWedstrijden().getGroepswedstrijdenNiveau(groepID);
		ArrayList<Wedstrijd> legeLijst = new ArrayList<>();
		if (serieID < 3) {
			Serie serie = controller.getWedstrijden().getGroepswedstrijdenNiveau(groepID).getSerie(serieID);
			if (serie == null) {
				Serie s = new Serie();
				s.setNummer(serieID);
				s.setWedstrijden(legeLijst);
				gws.addSerie(s);
				s.addWedstrijd(w);
			} else {
				serie.addWedstrijd(w, id);
			}
		} else {
			ArrayList<Wedstrijd> triowedstrijden = controller.getWedstrijden().getGroepswedstrijdenNiveau(groepID)
					.getTriowedstrijden();
			if (triowedstrijden == null) {
				gws.setTriowedstrijden(legeLijst);
				gws.addTrioWedstrijd(w);
			} else {
				triowedstrijden.add(w);
			}
		}
		component.repaint();
	}

	public Wedstrijd getWedstrijd(int idx) {
		if (serieID < 3) {
			Serie serie = controller.getWedstrijden().getGroepswedstrijdenNiveau(groepID).getSerie(serieID);
			return serie.getWedstrijd(idx);
		} else if (serieID == 3) {
			ArrayList<Wedstrijd> trio = controller.getWedstrijden().getGroepswedstrijdenNiveau(groepID)
					.getTriowedstrijden();
			return trio.get(idx);
		}
		return null;
	}

	public void verwijderWedstrijd(int idx) {
		if (serieID < 3) {
			Serie serie = controller.getWedstrijden().getGroepswedstrijdenNiveau(groepID).getSerie(serieID);
			serie.getWedstrijden().remove(idx);
		} else {
			controller.getWedstrijden().getGroepswedstrijdenNiveau(groepID).getTriowedstrijden().remove(idx);
		}
	}

	public void forceRepaint() {
		component.repaint();
	}
}
