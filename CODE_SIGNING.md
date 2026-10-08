# Code signing policy

*Nederlands: dit beleid beschrijft hoe de Windows-bestanden van IJC_UI2 worden ondertekend. Het is in het Engels geschreven voor de certificaatverstrekker.*

Free code signing provided by [SignPath.io](https://signpath.io), certificate by [SignPath Foundation](https://signpath.org).

## Project

- **Project:** IJC_UI2, a pairing program for the internal youth competition of a chess club.
- **Official repository:** https://github.com/oxygenius/IJC_UI2
- **License:** [GNU General Public License v3](LICENSE). All components are open source; there are no proprietary components.
- **Downloads:** only from [GitHub Releases](https://github.com/oxygenius/IJC_UI2/releases) of the official repository.

## What is signed

- `IJC_UI2.exe`, the application launcher built with `jpackage`.
- `IJC_UI2-setup-<version>.exe`, the Windows installer built with Inno Setup.

All signed files carry product name `IJC_UI2` and the product version of the release.

## How releases are built and signed

- Releases are built from source only by the GitHub Actions workflow [`.github/workflows/release.yml`](.github/workflows/release.yml), triggered by pushing a version tag `v<version>`.
- Local builds are never signed.
- Every signing request is approved manually by an approver before it is signed.

## Team roles

| Role | Members |
|---|---|
| Authors (committers) | [Lars Dam (@oxygenius)](https://github.com/oxygenius) |
| Reviewers | [Lars Dam (@oxygenius)](https://github.com/oxygenius) |
| Approvers | [Lars Dam (@oxygenius)](https://github.com/oxygenius) |

All team members use multi-factor authentication for GitHub and SignPath. Changes from non-committers (pull requests) are reviewed before they are merged.

## Privacy policy

This program will not transfer any information to other networked systems unless specifically requested by the user or the person installing or operating it.

Network communication only happens when the user explicitly chooses a menu action:

- publishing results and standings to the club website configured under *Instellingen → ExportAPI*;
- the administrator functions *Test request* and *Delete users API* under *Overig*, which contact the website of chess club De Stelling (`www.svdestelling.nl`).

There is no telemetry, update check or other background communication. All competition data (players, results, settings) is stored locally on the user's computer.
