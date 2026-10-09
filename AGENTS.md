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
