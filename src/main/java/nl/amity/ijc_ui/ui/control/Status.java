package nl.amity.ijc_ui.ui.control;

import java.util.ArrayList;

import nl.amity.ijc_ui.data.groepen.Groepen;
import nl.amity.ijc_ui.data.groepen.Speler;
import nl.amity.ijc_ui.data.wedstrijden.Wedstrijden;

public class Status {
    public boolean automatisch = true;
    public Groepen groepen;
    public Groepen wedstrijdgroepen;
    public Wedstrijden wedstrijden;
    public Groepen resultaatVerwerkt;
    public ArrayList<Speler> externGespeeld;

}
