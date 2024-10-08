package nl.amity.ijc_ui.io;

import java.io.File;
import java.io.FileWriter;
import java.util.logging.*;

import nl.amity.ijc_ui.data.wedstrijden.Groepswedstrijden;
import nl.amity.ijc_ui.data.wedstrijden.Serie;
import nl.amity.ijc_ui.data.wedstrijden.Wedstrijd;
import nl.amity.ijc_ui.data.wedstrijden.Wedstrijden;
import nl.amity.ijc_ui.neural.NeuralHelper;
import nl.amity.ijc_ui.ui.util.Utils;

/**
 * Exports data for the neural prediction network. Only data part is exported
 *
 * @author Leo.vanderMeulen
 *
 */
public class OutputNeuralData implements WedstrijdenExportInterface {

	private final static Logger logger = Logger.getLogger(OutputNeuralData.class.getName());
	private final static String ls = System.lineSeparator();
	private int periode;
	private int ronde;

	@Override
	public boolean export(Wedstrijden wedstrijden) {
		periode = wedstrijden.getPeriode();
		ronde = wedstrijden.getRonde();
		String bestandsnaam = "R" + periode + "-" + ronde + "NeuralData.arff";
		return export(wedstrijden, bestandsnaam);
	}

	public boolean export(Wedstrijden wedstrijden, String bestandsnaam) {
		try {
			logger.log(Level.INFO, "Sla neural data op in bestand " + bestandsnaam);
			String result = "";
			result += getHeader();
			result += "@DATA" + ls;
			for (Groepswedstrijden gw : wedstrijden.getGroepswedstrijden()) {
				for (Serie serie : gw.getSeries()) {
					for (Wedstrijd w : serie.getWedstrijden()) {
						result += NeuralHelper.convertWedstrijd(w) + ls;
					}
				}
				if (!gw.getTriowedstrijden().isEmpty()) {
					for (Wedstrijd w : gw.getTriowedstrijden()) {
						result += NeuralHelper.convertWedstrijd(w) + ls;
					}

				}
			}
			String dirName = "R" + wedstrijden.getPeriode() + "-" + wedstrijden.getRonde();
			new File(dirName).mkdirs();

			FileWriter writer = new FileWriter(dirName + File.separator + bestandsnaam);
			writer.write(result);
			writer.close();
		} catch (Exception ex) {
            logger.log(Level.INFO, "Exception: " +  ex.getMessage());
            Utils.stacktrace(ex);
			return false;
		}
		return true;
	}

	private String getHeader() {
		String result = "";
		result += "@RELATION chess_result" + ls;
		result += "@ATTRIBUTE wit_rating NUMERIC" + ls;
		result += "@ATTRIBUTE wit_stand NUMERIC" + ls;
		result += "@ATTRIBUTE wit_groep NUMERIC" + ls;
		result += "@ATTRIBUTE wit_resultaat_1 NUMERIC" + ls;
		result += "@ATTRIBUTE wit_rating_1 NUMERIC" + ls;
		result += "@ATTRIBUTE wit_resultaat_2 NUMERIC" + ls;
		result += "@ATTRIBUTE wit_rating_2 NUMERIC" + ls;
		result += "@ATTRIBUTE wit_resultaat_3 NUMERIC" + ls;
		result += "@ATTRIBUTE wit_rating_3 NUMERIC" + ls;
		result += "@ATTRIBUTE wit_resultaat_4 NUMERIC" + ls;
		result += "@ATTRIBUTE wit_rating_4 NUMERIC" + ls;
		result += "@ATTRIBUTE zwart_rating NUMERIC" + ls;
		result += "@ATTRIBUTE zwart_stand NUMERIC" + ls;
		result += "@ATTRIBUTE zwart_groep NUMERIC" + ls;
		result += "@ATTRIBUTE zwart_resultaat_1 NUMERIC" + ls;
		result += "@ATTRIBUTE zwart_rating_1 NUMERIC" + ls;
		result += "@ATTRIBUTE zwart_resultaat_2 NUMERIC" + ls;
		result += "@ATTRIBUTE zwart_rating_2 NUMERIC" + ls;
		result += "@ATTRIBUTE zwart_resultaat_3 NUMERIC" + ls;
		result += "@ATTRIBUTE zwart_rating_3 NUMERIC" + ls;
		result += "@ATTRIBUTE zwart_resultaat_4 NUMERIC" + ls;
		result += "@ATTRIBUTE zwart_rating_4 NUMERIC" + ls;
		result += "@ATTRIBUTE resultaat {wit,remise,zwart}" + ls;
		result += ls + ls;
		return result;
	}
}
