# Changelog

All significant changes to IJC_UI2 are recorded here, per version.
The version number is `appVersion` in `src/main/java/nl/amity/ijc_ui/ui/view/Hoofdscherm.java`.
The format is based on [Keep a Changelog](https://keepachangelog.com/).

## [Unreleased]

Record new features and bugfixes here as they are made. When releasing, rename this section to the new version number and date.

### Added

### Changed

### Fixed

## [2.0.1.7] - 2026-10-08

### Added
- **Debug** tab in the configuration dialog for choosing the log level, from OFF through SEVERE, WARNING, INFO, CONFIG, FINE, FINER and FINEST to ALL.
  - The level is stored in `configuratie.json` as `debugLevel`, with INFO as the default.
  - It takes effect as soon as you press OK, and is applied again when the configuration is loaded.
- JUnit test suite `GroepenIndelerTest` for the standard pairing: full trio, half trio, small groups, 5 players over two series, the doorschuif rules (third doorschuiver, absent players) and the full match schedule.

### Changed
- Log levels revised across the whole application:
  - INFO only for meaningful events.
  - CONFIG for loading and saving the configuration.
  - WARNING for errors, including stack traces.
  - FINE and FINER for pairing details.
  - FINEST for matrices, UI events and API responses.
  - Expensive debug output (for example matrix dumps) is only built when its level is enabled.
- All `System.out.println` and `printStackTrace()` output now goes through the logger, so it ends up in `IJC_UI.log`.
- Tokens and API message bodies are only logged at FINEST.
- The release zip no longer contains `configuratie.json` or `status.json`, because those hold club data.

### Fixed
- **Half trio** (odd number of players with an odd number of series): the third player of the trio was left out and the first player was paired twice. Every player now plays exactly once.
- Small groups: the minimum and maximum rating-difference limits could be larger than the group, so no series was planned. They are now limited to the group size.
- Match schedule highlighting: the check for odd groups used the table row count instead of the group size. Groups with too few players for the number of series are now fully highlighted.
- Corrupted "één" characters in pairing comments and log messages.

## [2.0.1.6] - 2026-10-07

### Added
- JUnit 5 test suite `GroepenIndelerFuzzyTest` for the fuzzy pairing:
  - Golden tests replay three real rounds (P1R4–P1R6) and check that the pairing matches what was actually played.
  - Rule tests check that every player is paired once and never against themselves, that the input is unchanged, even and odd group sizes, 5 players without `fuzzyOneven`, no rematch against the previous opponent, and the white/black preference.
- Anonymized test fixtures, plus `src/test/tools/anonymize_status.py` to make new ones. The script replaces names, KNSB numbers and UUIDs but keeps initials, because pairing needs them.
- Build: added `junit-jupiter` 5.11.3 and `maven-surefire-plugin` 3.5.2.

## [2.0.1.5] - 2026-10-07

### Fixed
- Fuzzy pairing: a doorschuiver weighting condition could never be true, so a player of the group itself against a player who had moved up never got that weight, and the weighting matrix was asymmetric.
- Fuzzy pairing: the "ZW balans" log line was only written when the 5-player series were not merged.

### Added
- `Release/IJC_UI.cmd` launcher.

## [2.0.1.4] - 2026-10-07

### Changed
- Removed deprecated API usage, such as `new Integer(...)`, `new Double(...)`, `getMenuShortcutKeyMask()` and `Date.getMonth()`, and applied Java 17 idioms.
- The standings export takes an explicit "reversed" option. Groups are listed highest first, and doorschuivers always come from the correct lower group.

### Known issues
- `Release/run.cmd` was generated incorrectly. It is replaced by `IJC_UI.cmd` in 2.0.1.5.

## [2.0.1.3] - 2026-10-07

### Added
- Logging to file: `IJC_UI.log` (UTF-8, rotates at 5 MB, each line has a timestamp, level and class), configured through `LoggingConfig` and `logging.properties`.
- Maven release profile: `mvn -Prelease package` builds a self-contained jar, `release-info.txt`, a launcher and a distribution zip in `Release/`.

### Changed
- Requires **Java 17** (was Java 8).
- `pom.xml` is complete: dependencies are bundled in the jar and updated (Groovy 4.0.24, Gson 2.11.0, JFreeChart 1.5.6, org.json 20240303).

### Fixed
- Sorting works again (it was disabled in 2.0.1.2):
  - players by points, with rating as tie-breaker and extra weight for KNSB members in the lowest group;
  - groups by level.
- Results are processed group by group in ascending level order. The main screen tabs show the groups in descending level order.
- Standings export: doorschuivers were taken from the wrong neighbouring group.
- Standings export: added a null check to the single-doorschuiver "champion" rule.

## [2.0.1.2] - 2026-08-27

### Removed
- All sorting temporarily disabled to get around a recurring sorting bug. As a result:
  - Clicking the rating or points column no longer sorts.
  - Players and groups stay in their stored order.

### Changed
- Small cleanup of the Plone 5.2 API code, with no change in behaviour.

## [2.0.1.0] - 2025-06-06

### Changed
- Apache POI updated from 5.2.5 to 5.4.1 (security fixes).
- Groups are sorted by points, descending, by default. The standings export, results processing and status loading always use this order, whatever sort is chosen on screen.
- Moving to the next round no longer imports the round into the player-history database.

### Fixed
- Sorting routine: the points sort direction was wrong, and changing the sort on screen also changed the order of the exported standings.
- Standings export: groups are sorted again before the doorschuivers are determined.
- Loading a status file without match groups no longer crashes.
- The white/black balance fields no longer throw a NullPointerException when a match group is missing.
- In automatic mode, sorting by points now redoes the pairing and refreshes the screen.

## [2.0.0.3] - 2024-09-10

### Added
- Maven build (`pom.xml`).
- `LesTekstDialoog`: edit the lesson text before it is published through the external API. Cancel skips the export.
- The doorschuivers footer text in the standings export is wrapped at 80 characters.

### Changed
- Plone 5.2 API is live. Login, creating and publishing the club evening page, and listing and deleting users now send real HTTP requests. The lesson text comes from the API template.
- Standings export lists the highest group first.
- New doorschuiven (promotion) routine:
  - It goes through the groups by level.
  - It promotes one player fewer if the last candidate is absent.
  - A single player is only promoted when they can no longer be caught (more than 4 points ahead of number 2), unless "laatste ronde doorschuiven altijd" is set.
  - The last promotion only happens if it makes the receiving group even.
- Clicking the rating or points column toggles ascending and descending. In automatic mode this redoes the pairing.

### Fixed
- Sorting by points actually sorted by rating.
- Groups are now sorted after a status file is loaded.
- Export through the API no longer goes ahead when login returned no token.

## [2.0.0.2] - 2024-08-28

First version in this repository, based on IJC_UI by Leo van de Meulen and Lars Dam.

- Management of the internal youth competition as a ladder or Keizer competition, organized by group, period and round.
- Players: add, import, edit, move up or down a group, and view history in an ObjectDB player database with charts.
- Pairing: a standard indeler and a fuzzy indeler, creating and editing the match schedule, external players, and entering results.
- Exports: standings, results, match schedule, sign-in sheet, Excel, KNSB, OSBO, KEI points and neural-network training data.
- Result prediction (Weka neural network).
- External publishing through a Plone 5.2 API (still stubbed in this version).
- Passwords stored encrypted in a keystore.
