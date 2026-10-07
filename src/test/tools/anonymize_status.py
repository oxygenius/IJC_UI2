"""
Maak een geanonimiseerde testfixture van een status JSON bestand.

Gebruik:
    python src/test/tools/anonymize_status.py R1-6/status20261007_131124-uitslag.json \
        src/test/resources/fixtures/fuzzy/P1R6.json

Alleen 'wedstrijdgroepen' (invoer voor de indeling) en 'wedstrijden' (de indeling
zoals die gemaakt is) worden bewaard. Namen, KNSB nummers en UUIDs worden vervangen.
Initialen blijven staan, omdat de indeling daarmee eerdere tegenstanders herkent.
"""
import json
import sys
import uuid

NAMESPACE = uuid.UUID("6f1c2a52-6a0e-4c43-9a43-1b8f4f0e1f00")


def main(src, dst):
    with open(src, encoding="utf-8") as f:
        status = json.load(f)

    anon = {}

    def anonymize(speler):
        key = speler["uid"]
        if key not in anon:
            nr = len(anon) + 1
            anon[key] = {
                "naam": "Speler %02d" % nr,
                "uid": str(uuid.uuid5(NAMESPACE, str(nr))),
            }
        speler["naam"] = anon[key]["naam"]
        speler["uid"] = anon[key]["uid"]
        speler["KNSBnummer"] = 0

    for groep in status["wedstrijdgroepen"]["groepen"]:
        for speler in groep["spelers"]:
            anonymize(speler)
    for gws in status["wedstrijden"]["groepswedstrijden"]:
        for serie in gws.get("series", []):
            for w in serie["wedstrijden"]:
                anonymize(w["wit"])
                anonymize(w["zwart"])
        for w in gws.get("triowedstrijden", []):
            anonymize(w["wit"])
            anonymize(w["zwart"])

    fixture = {
        "automatisch": status.get("automatisch", True),
        "wedstrijdgroepen": status["wedstrijdgroepen"],
        "wedstrijden": status["wedstrijden"],
    }
    with open(dst, "w", encoding="utf-8") as f:
        json.dump(fixture, f, indent=1, ensure_ascii=False)


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
