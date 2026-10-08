# Wijzigingen

Alle belangrijke wijzigingen in IJC_UI2, per versie.
Het versienummer is `<version>` in `pom.xml`.
De opzet volgt [Keep a Changelog](https://keepachangelog.com/nl/).

## [Nog niet uitgebracht]

Noteer hier nieuwe functies en bugfixes zodra ze gemaakt zijn. Hernoem deze sectie bij een release naar het nieuwe versienummer met de datum.

### Toegevoegd
- De release-build controleert dat de zip en de jar geen club- of spelersgegevens bevatten (`configuratie.json`, `status.json`, keystore, spelersdatabase, rondemappen). Anders wordt de release afgebroken.
- `CHANGELOG.md` zit nu in de release-zip.
- Test `VersieTest`: controleert dat de changelog een sectie heeft voor de huidige versie.
- Test `FixturesAnoniemTest`: controleert dat alle testfixtures geanonimiseerd zijn.

### Gewijzigd
- Het versienummer staat nog maar op één plek: `<version>` in `pom.xml`. Het programma leest het via `version.properties` (klasse `Versie`). `appVersion` in `Hoofdscherm.java` hoeft niet meer met de hand te worden bijgewerkt.
- README herschreven, met installatie, gebruik op de clubavond, problemen melden en ontwikkeling.
- Changelog vertaald naar het Nederlands.

### Opgelost

## [2.0.1.7] - 2026-10-08

### Toegevoegd
- Tabblad **Debug** in het instellingenscherm om het logniveau te kiezen, van OFF via SEVERE, WARNING, INFO, CONFIG, FINE, FINER en FINEST tot ALL.
  - Het niveau wordt in `configuratie.json` bewaard als `debugLevel`, met INFO als standaard.
  - Het werkt direct na OK en wordt opnieuw toegepast bij het laden van de instellingen.
- JUnit-testset `GroepenIndelerTest` voor de standaardindeler: volledig trio, half trio, kleine groepen, 5 spelers over twee series, de doorschuifregels (derde doorschuiver, afwezige spelers) en het volledige speelschema.

### Gewijzigd
- Logniveaus in de hele applicatie herzien:
  - INFO alleen voor betekenisvolle gebeurtenissen.
  - CONFIG voor het laden en opslaan van de instellingen.
  - WARNING voor fouten, met stacktrace.
  - FINE en FINER voor details van de indeling.
  - FINEST voor matrices, UI-gebeurtenissen en API-antwoorden.
  - Zware debuguitvoer (bijvoorbeeld matrixdumps) wordt alleen opgebouwd als dat niveau aan staat.
- Alle uitvoer van `System.out.println` en `printStackTrace()` gaat nu via de logger en komt dus in `IJC_UI.log`.
- Tokens en de inhoud van API-berichten worden alleen op FINEST gelogd.
- De release-zip bevat geen `configuratie.json` en `status.json` meer, omdat daarin clubgegevens staan.

### Opgelost
- **Half trio** (oneven aantal spelers bij een oneven aantal series): de derde speler van het trio werd overgeslagen en de eerste speler werd twee keer ingedeeld. Nu speelt iedere speler precies één keer.
- Kleine groepen: de grenzen voor het minimale en maximale ratingverschil konden groter zijn dan de groep, waardoor er geen serie werd ingepland. Ze worden nu begrensd op de groepsgrootte.
- Markering in het speelschema: de controle op oneven groepen gebruikte het aantal tabelrijen in plaats van de groepsgrootte. Groepen met te weinig spelers voor het aantal series worden nu volledig gemarkeerd.
- Verminkte tekens in "één" in commentaar en logmeldingen van de indeler.

## [2.0.1.6] - 2026-10-07

### Toegevoegd
- JUnit 5-testset `GroepenIndelerFuzzyTest` voor de fuzzy indeler:
  - Golden tests spelen drie echte rondes na (P1R4–P1R6) en controleren dat de indeling overeenkomt met wat er echt gespeeld is.
  - Regeltests controleren dat iedere speler één keer en nooit tegen zichzelf is ingedeeld, dat de invoer ongewijzigd blijft, even en oneven groepsgroottes, 5 spelers zonder `fuzzyOneven`, geen herhaling tegen de vorige tegenstander, en de wit/zwart-voorkeur.
- Geanonimiseerde testgegevens, plus `src/test/tools/anonymize_status.py` om nieuwe te maken. Het script vervangt namen, KNSB-nummers en UUID's maar behoudt de initialen, omdat de indeler die nodig heeft.
- Build: `junit-jupiter` 5.11.3 en `maven-surefire-plugin` 3.5.2 toegevoegd.

## [2.0.1.5] - 2026-10-07

### Opgelost
- Fuzzy indeler: een voorwaarde voor de weging van doorschuivers kon nooit waar zijn. Een speler uit de groep zelf tegen een doorgeschoven speler kreeg daardoor nooit die weging, en de wegingsmatrix was niet symmetrisch.
- Fuzzy indeler: de logregel "ZW balans" werd alleen geschreven als de series van 5 spelers niet werden samengevoegd.

### Toegevoegd
- Startbestand `Release/IJC_UI.cmd`.

## [2.0.1.4] - 2026-10-07

### Gewijzigd
- Verouderde API-aanroepen verwijderd, zoals `new Integer(...)`, `new Double(...)`, `getMenuShortcutKeyMask()` en `Date.getMonth()`, en Java 17-constructies toegepast.
- De standexport heeft een expliciete optie "omgekeerd". De groepen staan met de hoogste bovenaan, en doorschuivers komen altijd uit de juiste lagere groep.

### Bekende problemen
- `Release/run.cmd` werd verkeerd aangemaakt. In 2.0.1.5 is het vervangen door `IJC_UI.cmd`.

## [2.0.1.3] - 2026-10-07

### Toegevoegd
- Logging naar bestand: `IJC_UI.log` (UTF-8, wordt bij 5 MB vervangen, elke regel met tijdstip, niveau en klasse), ingesteld via `LoggingConfig` en `logging.properties`.
- Maven-releaseprofiel: `mvn -Prelease package` bouwt een zelfstandige jar, `release-info.txt`, een startbestand en een distributie-zip in `Release/`.

### Gewijzigd
- Vereist **Java 17** (was Java 8).
- `pom.xml` is volledig: de afhankelijkheden zitten in de jar en zijn bijgewerkt (Groovy 4.0.24, Gson 2.11.0, JFreeChart 1.5.6, org.json 20240303).

### Opgelost
- Sorteren werkt weer (het stond uit sinds 2.0.1.2):
  - spelers op punten, met rating als tiebreak en extra weging voor KNSB-leden in de laagste groep;
  - groepen op niveau.
- Uitslagen worden per groep verwerkt, van laag naar hoog niveau. De tabbladen in het hoofdscherm tonen de groepen van hoog naar laag niveau.
- Standexport: doorschuivers werden uit de verkeerde aangrenzende groep gehaald.
- Standexport: controle op null toegevoegd aan de "kampioen"-regel voor één doorschuiver.

## [2.0.1.2] - 2026-08-27

### Verwijderd
- Sorteren tijdelijk helemaal uitgezet om een terugkerende sorteerfout te omzeilen. Daardoor:
  - Klikken op de kolom rating of punten sorteert niet meer.
  - Spelers en groepen blijven in de opgeslagen volgorde staan.

### Gewijzigd
- Kleine opschoning van de code voor de Plone 5.2-API, zonder verandering in gedrag.

## [2.0.1.0] - 2025-06-06

### Gewijzigd
- Apache POI bijgewerkt van 5.2.5 naar 5.4.1 (beveiligingsupdates).
- Groepen worden standaard gesorteerd op punten, aflopend. De standexport, de uitslagverwerking en het laden van de status gebruiken altijd deze volgorde, welke sortering er op het scherm ook is gekozen.
- Bij de volgende ronde wordt de ronde niet meer in de spelershistorie-database geïmporteerd.

### Opgelost
- Sorteerroutine: de sorteerrichting op punten was verkeerd, en een andere sortering op het scherm veranderde ook de volgorde in de geëxporteerde stand.
- Standexport: de groepen worden opnieuw gesorteerd voordat de doorschuivers worden bepaald.
- Een statusbestand zonder wedstrijdgroepen laden geeft geen crash meer.
- De velden voor de wit/zwart-balans geven geen NullPointerException meer als een wedstrijdgroep ontbreekt.
- In automatische modus wordt bij sorteren op punten opnieuw ingedeeld en het scherm ververst.

## [2.0.0.3] - 2024-09-10

### Toegevoegd
- Maven-build (`pom.xml`).
- `LesTekstDialoog`: de lestekst aanpassen voordat die via de externe API wordt gepubliceerd. Met Annuleren wordt de export overgeslagen.
- De doorschuiverstekst onder de standexport wordt afgebroken op 80 tekens.

### Gewijzigd
- De Plone 5.2-API werkt echt. Inloggen, de clubavondpagina aanmaken en publiceren, en gebruikers opvragen en verwijderen versturen nu echte HTTP-verzoeken. De lestekst komt uit het API-sjabloon.
- De standexport begint met de hoogste groep.
- Nieuwe doorschuifroutine:
  - De groepen worden op niveau doorlopen.
  - Er schuift één speler minder door als de laatste kandidaat afwezig is.
  - Eén speler schuift alleen door als die niet meer in te halen is (meer dan 4 punten voor op nummer 2), tenzij "laatste ronde doorschuiven altijd" aan staat.
  - De laatste doorschuiver gaat alleen door als de ontvangende groep daardoor even wordt.
- Klikken op de kolom rating of punten wisselt tussen oplopend en aflopend. In automatische modus wordt dan opnieuw ingedeeld.

### Opgelost
- Sorteren op punten sorteerde in werkelijkheid op rating.
- Groepen worden nu gesorteerd na het laden van een statusbestand.
- Exporteren via de API gaat niet meer door als het inloggen geen token opleverde.

## [2.0.0.2] - 2024-08-28

Eerste versie in deze repository, gebaseerd op IJC_UI van Leo van der Meulen en Lars Dam.

- Beheer van de interne jeugdcompetitie als ladder- of Keizercompetitie, verdeeld in groepen, perioden en rondes.
- Spelers: toevoegen, importeren, bewerken, doorschuiven of terugschuiven, en de historie bekijken in een ObjectDB-spelersdatabase met grafieken.
- Indelen: een standaardindeler en een fuzzy indeler, het speelschema maken en bewerken, externe spelers, en uitslagen invoeren.
- Exporteren: stand, uitslagen, speelschema, intekenlijst, Excel, KNSB, OSBO, KEI-punten en trainingsgegevens voor het neurale netwerk.
- Uitslagvoorspelling (neuraal netwerk met Weka).
- Publiceren via een Plone 5.2-API (in deze versie nog niet werkend).
- Wachtwoorden versleuteld opgeslagen in een keystore.
