# IJC_UI2

*Dutch chess pairing tool for internal youth competitions. Documentation is in Dutch.*

Indelingsprogramma voor de interne jeugdcompetitie van een schaakvereniging, ontwikkeld door Lars Dam.
Gebaseerd op IJC_UI, ontwikkeld door Leo van der Meulen en Lars Dam.

## Wat doet het?

IJC_UI2 ondersteunt de competitieleider of trainer op de clubavond bij het indelen en bijhouden van de jeugdcompetitie: een laddercompetitie of Keizercompetitie, verdeeld in groepen, perioden en rondes.

- **Spelers**: toevoegen, importeren, bewerken, doorschuiven of terugschuiven naar een andere groep, en de speelhistorie bekijken met grafieken.
- **Indelen**: twee indelers (standaard en fuzzy), die rekening houden met rating, eerdere tegenstanders, wit/zwart-balans en doorschuivers. Het wedstrijdschema kan daarna met de hand worden aangepast.
- **Uitslagen**: uitslagen invoeren, externe spelers verwerken, de stand bijwerken en naar de volgende ronde gaan.
- **Exporteren**: stand, uitslagen, speelschema, intekenlijst, Excel, KNSB, OSBO en KEI-punten.
- **Publiceren**: clubavondpagina met uitslagen en stand op de verenigingswebsite (Plone 5.2), met een lestekst die je vooraf kunt aanpassen.
- **Voorspellen**: uitslagvoorspelling met een neuraal netwerk (Weka).

## Installatie

1. Installeer **Java 17** of nieuwer, bijvoorbeeld [Eclipse Temurin](https://adoptium.net/).
2. Download de nieuwste `ijc_ui-<versie>.zip` bij [Releases](https://github.com/oxygenius/IJC_UI2/releases).
3. Pak de zip uit in een eigen map en start `IJC_UI.cmd`.

De zip bevat geen club- of spelersgegevens. Bij de eerste start stel je via **Bestand → Instellingen** de vereniging en de competitie in.

### Bestanden in de programmamap

| Bestand / map | Inhoud |
|---|---|
| `configuratie.json` | Instellingen van de vereniging en de competitie |
| `status.json` | Huidige stand van de competitie: groepen, spelers en uitslagen |
| `keystore.ks` | Versleuteld opgeslagen wachtwoorden, bijvoorbeeld voor de website |
| `db/spelers.odb` | Spelersdatabase met de speelhistorie |
| `R<periode>-<ronde>/` | Uitvoer per ronde (uitslagen, stand, statusbestanden) |
| `IJC_UI.log` | Logbestand |
| `Leeg.docx` | Sjabloon voor de intekenlijst |
| `Template.xlsx` | Sjabloon voor de Excel-export. Dit sjabloon zit niet in de zip; zet je eigen sjabloon in de programmamap. |

> **Let op:** `configuratie.json`, `status.json`, `keystore.ks`, `db/` en de rondemappen bevatten persoonsgegevens van (jeugd)leden. Deel ze niet en zet ze nooit in git. Maak wel regelmatig een back-up van de programmamap.

## Gebruik op de clubavond

1. Start het programma. De laatst opgeslagen competitie (`status.json`) wordt automatisch geopend.
2. Vink per groep aan wie er aanwezig is.
3. Deel in via **Indeling → Maak wedstrijdgroep** en **Maak speelschema**, of zet **Automatisch aan/uit** aan. Pas het schema zo nodig aan met **Bewerk speelschema**.
4. Exporteer het speelschema of de intekenlijst en druk die af.
5. Voer na afloop de uitslagen in via **Indeling → Vul uitslagen in**.
6. Kies **Indeling → Volgende ronde**. De stand wordt bijgewerkt en de uitvoer komt in de map `R<periode>-<ronde>`.
7. Exporteer of publiceer de uitslagen en de stand.

## Problemen melden

Het programma schrijft een logbestand `IJC_UI.log` in de programmamap.

1. Zet bij een probleem via **Bestand → Instellingen → Debug** het logniveau op `FINE` of `FINEST`.
2. Herhaal wat er misging.
3. Meld het probleem bij [Issues](https://github.com/oxygenius/IJC_UI2/issues) en voeg het logbestand toe.

Zet het niveau daarna terug op `INFO`. Controleer het logbestand eerst op namen van spelers voordat je het deelt.

## Wijzigingen

Zie [CHANGELOG.md](CHANGELOG.md) voor de wijzigingen per versie.

## Ontwikkeling

### Benodigd

- JDK 17
- Maven 3.9 of nieuwer. De ObjectDB-repository staat al in `pom.xml`.

### Bouwen en testen

```sh
mvn package              # bouwt target/ijc_ui-<versie>.jar
mvn test                 # draait de unittests
mvn -Prelease package    # bouwt de release: jar, zip, release-info.txt en IJC_UI.cmd in Release/
```

De tests (`GroepenIndelerTest`, `GroepenIndelerFuzzyTest`) gebruiken competitiegegevens in `src/test/resources/fixtures/`. Nieuwe testgegevens moeten eerst geanonimiseerd worden met `src/test/tools/anonymize_status.py`. Dat script vervangt namen, KNSB-nummers en UUID's maar behoudt de initialen, omdat de indeler die nodig heeft.

### Opbouw van de code

Alle code staat onder `src/main/java/nl/amity/ijc_ui/`.

| Package | Inhoud |
|---|---|
| `data` | Groepen, spelers, wedstrijden en de koppeling met de website-API |
| `db` | Spelersdatabase (ObjectDB) |
| `io` | Import en export (stand, uitslagen, Excel, KNSB, OSBO, KEI) |
| `neural` | Uitslagvoorspelling (Weka) |
| `ui/control` | Controller, indelers en uitslagverwerking |
| `ui/model`, `ui/view` | Swing-tabelmodellen, schermen en dialogen |
| `util` | Logging |

### Een nieuwe versie uitbrengen

1. Verhoog het versienummer in `pom.xml` (`<version>`). Dat is de enige plek: het programma leest de versie via `version.properties`, dat Maven bij het bouwen vult.
2. Werk [CHANGELOG.md](CHANGELOG.md) bij: hernoem de sectie *Nog niet uitgebracht* naar het nieuwe versienummer met de datum.
3. Bouw met `mvn -Prelease package`.
4. Commit, maak een tag `v<versie>` en push de commit en de tag.
5. Maak een GitHub-release met de zip uit `Release/` en de tekst uit de changelog:
   ```sh
   gh release create v<versie> Release/ijc_ui-<versie>.zip --title "IJC_UI <versie>" --notes-file <notities.md>
   ```

Release-zips worden niet in git opgenomen; ze staan alleen bij Releases.

De build bewaakt een paar regels automatisch:

| Controle | Wanneer | Wat gebeurt er bij een fout |
|---|---|---|
| Release-zip en jar bevatten geen `configuratie.json`, `status.json`, keystore, spelersdatabase of rondemap | `mvn -Prelease package` | De release wordt afgebroken |
| `CHANGELOG.md` heeft een sectie voor de versie uit `pom.xml` | `mvn test` (`VersieTest`) | De test faalt |
| Alle fixtures zijn geanonimiseerd: spelers heten `Speler NN` en hebben KNSB-nummer 0 | `mvn test` (`FixturesAnoniemTest`) | De test faalt |

## Licentie

Dit programma valt onder de [GNU General Public License versie 3](https://www.gnu.org/licenses/gpl-3.0.html).
Copyright © 2016–2026 Leo van der Meulen en Lars Dam.
