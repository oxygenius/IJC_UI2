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

### Met de installer (aanbevolen)

1. Download de nieuwste `IJC_UI2-setup-<versie>.exe` bij [Releases](https://github.com/oxygenius/IJC_UI2/releases).
2. Start de installer en volg de stappen. Beheerdersrechten zijn niet nodig, en Java hoeft niet apart geïnstalleerd te worden: dat zit erin.
3. Start het programma via het Startmenu of de snelkoppeling op het bureaublad.

De installer zet het programma in `%LOCALAPPDATA%\Programs\IJC_UI2`. Je gegevens komen in **`Documenten\IJC_UI2`**; het Startmenu heeft daar een snelkoppeling *Gegevensmap IJC_UI2* naartoe. Voor een nieuwe versie start je gewoon de nieuwe installer: je gegevens blijven staan, ook als je het programma verwijdert.

Ziet Windows een waarschuwing van SmartScreen ("Windows heeft uw pc beschermd")? Kies dan *Meer informatie → Toch uitvoeren*. Zie [Ondertekening](#ondertekening).

### Met de zip

1. Installeer **Java 17** of nieuwer, bijvoorbeeld [Eclipse Temurin](https://adoptium.net/).
2. Download de nieuwste `ijc_ui-<versie>.zip` bij [Releases](https://github.com/oxygenius/IJC_UI2/releases).
3. Pak de zip uit in een eigen map en start `IJC_UI.cmd`. Je gegevens komen dan in diezelfde map.

De installer en de zip bevatten geen club- of spelersgegevens. Bij de eerste start stel je via **Bestand → Instellingen** de vereniging en de competitie in.

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
| `Template.xlsx` | Sjabloon voor de Excel-export, met één werkblad per groep. Zit alleen in de zip als het bij het bouwen in de projectmap stond; zet anders je eigen sjabloon in de programmamap. |

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

## Ondertekening

De Windows-bestanden worden ondertekend via SignPath, zodra die aanvraag is goedgekeurd. Free code signing provided by [SignPath.io](https://signpath.io), certificate by [SignPath Foundation](https://signpath.org). Zie het [Code signing policy](CODE_SIGNING.md) voor wat er ondertekend wordt, door wie, en het privacybeleid.

Ook een ondertekend programma kan de eerste tijd nog een SmartScreen-waarschuwing geven, totdat het genoeg gedownload is.

## Ontwikkeling

### Benodigd

- JDK 17
- Maven 3.9 of nieuwer. De ObjectDB-repository staat al in `pom.xml`.

### Bouwen en testen

```sh
mvn package              # bouwt target/ijc_ui-<versie>.jar
mvn test                 # draait de unittests
mvn -Prelease package    # bouwt de release: jar, zip, release-info.txt en IJC_UI.cmd in Release/
mvn -Prelease,installer package   # idem, plus de Windows-installer IJC_UI2-setup-<versie>.exe
```

Voor de installer zijn nodig: een JDK 17 of nieuwer met `jpackage` (Maven gebruikt de JDK van `JAVA_HOME`) en [Inno Setup 6](https://jrsoftware.org/isinfo.php). Staat Inno Setup niet op de standaardplek, geef dan het pad mee met `-Discc.exe=...`. Het installerscript staat in `installer/IJC_UI2.iss`.

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
3. Controleer lokaal met `mvn -Prelease,installer package` dat alles bouwt en de tests slagen.
4. Commit, maak een tag `v<versie>` en push de commit en de tag:
   ```sh
   git tag -a v<versie> -m "IJC_UI <versie>"
   git push origin main v<versie>
   ```
5. De workflow [`release.yml`](.github/workflows/release.yml) doet de rest op GitHub: bouwen en testen, de installer laten ondertekenen via SignPath (als dat is ingesteld) en de GitHub-release maken met de installer, de zip en de tekst uit de changelog. Bij SignPath moet je het ondertekenen nog wel goedkeuren.

Alleen in een noodgeval maak je de release met de hand. Die is dan niet ondertekend:
```sh
python .github/scripts/release_notes.py <versie> > notes.md
gh release create v<versie> Release/IJC_UI2-setup-<versie>.exe Release/ijc_ui-<versie>.zip --title "IJC_UI <versie>" --notes-file notes.md
```

De map `Release/` staat niet in git; ze bevat alleen bouwresultaten en eventueel je eigen gegevens als je het programma daar draait. Bij elke release-build worden jars en zips van eerdere versies uit `Release/` verwijderd (die staan bij Releases); `configuratie.json`, `status.json`, `keystore.ks`, `db/` en logbestanden blijven staan. `Leeg.docx` en `Template.xlsx` worden meegenomen als ze in de projectmap staan.

De build bewaakt een paar regels automatisch:

| Controle | Wanneer | Wat gebeurt er bij een fout |
|---|---|---|
| Release-zip en jar bevatten geen `configuratie.json`, `status.json`, keystore, spelersdatabase of rondemap | `mvn -Prelease package` | De release wordt afgebroken |
| `CHANGELOG.md` heeft een sectie voor de versie uit `pom.xml` | `mvn test` (`VersieTest`) | De test faalt |
| Alle fixtures zijn geanonimiseerd: spelers heten `Speler NN` en hebben KNSB-nummer 0 | `mvn test` (`FixturesAnoniemTest`) | De test faalt |

## Licentie

Dit programma valt onder de [GNU General Public License versie 3](LICENSE).
Copyright © 2016–2026 Leo van der Meulen en Lars Dam.
