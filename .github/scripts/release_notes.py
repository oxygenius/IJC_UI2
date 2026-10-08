"""
Haal de releasetekst voor een versie uit CHANGELOG.md.

Gebruik:
    python .github/scripts/release_notes.py 2.0.1.8 > notes.md
"""
import re
import sys


def main(versie):
    with open("CHANGELOG.md", encoding="utf-8") as f:
        changelog = f.read()
    m = re.search(r"^## \[" + re.escape(versie) + r"\][^\n]*\n(.*?)(?=^## \[|\Z)", changelog, re.S | re.M)
    if not m:
        sys.exit("CHANGELOG.md heeft geen sectie '## [%s]'" % versie)
    sys.stdout.reconfigure(encoding="utf-8")
    print(m.group(1).strip())
    print()
    print("Vereist Windows 10 of nieuwer (installer) of Java 17 of nieuwer (zip). "
          "Alle wijzigingen: zie [CHANGELOG.md](https://github.com/oxygenius/IJC_UI2/blob/main/CHANGELOG.md).")


if __name__ == "__main__":
    main(sys.argv[1])
