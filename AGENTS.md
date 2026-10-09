# AGENTS.md

Build and verification commands for this project.

## Build verification

Maven is required. Run from the project root (`C:\Users\lpdam\Documents\Code\IJC_UI2`):

```sh
# Compile and run tests (includes VersieTest + FixturesAnoniemTest)
mvn test

# Full build
mvn package

# Release build (jar + zip + release-info.txt in Release/)
mvn -Prelease package

# Release + Windows installer
mvn -Prelease,installer package
```

## Test scope

The unit tests (`GroepenIndelerTest`, `GroepenIndelerFuzzyTest`) plus the
`VersieTest` / `FixturesAnoniemTest` checks can be run with `mvn test`.

## Maven location on this machine

Maven is not on PATH. IntelliJ IDEA bundles Maven 3.9.16 at:

```
C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3
```

Set `MAVEN_HOME` and `JAVA_HOME` before running `mvn`:

```sh
$env:MAVEN_HOME = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3"
$env:JAVA_HOME  = "C:\Program Files\Java\jdk-25"
& "$env:MAVEN_HOME\bin\mvn.cmd" test
```

## Release process

The release process is automated via GitHub Actions. To create a new release:

1. **Update version** in `pom.xml` (`<version>`)
2. **Update CHANGELOG.md**: Move items from `[Nog niet uitgebracht]` to a new version section `[x.y.z.w] - date`
3. **Commit** the changes with message "Versie x.y.z.w"
4. **Create and push tag**: `git tag -a v<x.y.z.w> -m "Versie x.y.z.w"` then `git push origin v<x.y.z.w>`
5. **GitHub Actions** (`.github/workflows/release.yml`) will automatically:
   - Build the release artifacts (jar, zip, Windows installer)
   - Create the GitHub Release with artifacts
   - Publish to GitHub Releases

The `gh` CLI is available at `/c/Program Files/GitHub CLI/gh.exe` for manual release operations if needed.

**Important**: The `Release/` directory is in `.gitignore` - never commit release artifacts. The repository is public, so ensure no club data (configuratie.json, status.json, keystore.ks) is included in releases.
