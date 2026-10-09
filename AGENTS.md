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
