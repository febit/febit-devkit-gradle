# Changelog

## [1.7.0] - not yet released

### Breaking Changes

- **standard-java**: `test` now runs only `**/*Test.class` and excludes the `integration` tag; tagged tests run in the new
  `integrationTest` task

### New Features

- **standard-java**: Added `SpotlessSetup` — spotless on `src/*/java/**/*.java` (UTF-8, `endWithNewline`,
  `trimTrailingWhitespace`), `check` depends on `spotlessCheck`
- **standard-java**: Added `TestTaskSetup` — `build/tmp/tests` as `java.io.tmpdir` for all `Test` tasks, test filtering and an
  `integrationTest` task
- **standard-java**: Added `MockitoAgentSetup` / `MockitoAgentProvider` — `mockito-core` from the test runtime classpath is
  passed to test JVMs as `-javaagent`, skipped when Mockito is not used
- **standard-java**: Added `JacocoSetup` — applies jacoco and enables the `JacocoReport` XML report

### Fixes

- **standard-java**: `LombokSetup` orders `generateEffectiveLombokConfig` after codegen tasks via `configureEach`
- **standard-java**: `TestTaskSetup` and `MockitoAgentSetup` work when `standard-java` is applied before `java`
- **common**: `JavaUtils.classSimpleName` keeps names without a package separator (previously empty)

### Improvements

- **standard-java**: Pinned lombok 1.18.48
- **standard-maven-publish**: Replaced deprecated `project.getProperties()` with `providers.gradlePropertiesPrefixedBy`

### Dependencies

- Gradle Wrapper 9.5.1 → 9.7.1
- commons-collections4 4.5.0 → 4.6.0
- freefair lombok plugin 9.5.0 → 9.7.0
- junit 6.1.0 → 6.1.3
- lombok 1.18.46 → 1.18.48
- mockito 5.23.0 → 5.24.0
- slf4j 2.0.18 → 2.0.19
- spotbugs 4.10.2 → 4.10.4
- Added spotless 8.10.3
- Added tabletest-junit 1.2.2
- Removed the license plugin

### Build

- Switched from the license plugin to spotless and normalized formatting

### Tests

- Added unit tests for `febit-gradle-common` utilities (table-driven via tabletest-junit)
- Added unit tests for `standard/java`, `standard/bom`, `standard/maven-publish` and `codegen/*`
