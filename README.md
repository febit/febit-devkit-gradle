# Febit Devkit - Gradle

A set of Gradle plugins and utilities for Java projects.

- **Group**: `org.febit.devkit.gradle`
- **Java**: 17+
- **Gradle**: 9.7.1 (wrapper included)
- **Published to**: Maven Central

## Quick Start

### 1. Apply once in the root project

These four plugins configure the project they are applied to and all of its subprojects:

```groovy
// build.gradle (root)
plugins {
  id 'org.febit.standard-java' version '<version>'
  id 'org.febit.standard-maven-publish' version '<version>'
  id 'org.febit.codegen-module' version '<version>'
  id 'org.febit.codegen-manifest' version '<version>'
}

allprojects {
  standardMavenPublish {
    pom { /* extra MavenPom configuration */ }
  }
  codegenModule {
    defaultTemplate = fromFile("$rootDir/etc/module.java.tmpl")
  }
  spotless {
    java {
      licenseHeaderFile rootProject.file('etc/license-header.txt')
      removeUnusedImports()
    }
  }
}
```

### 2. Configure the modules

```groovy
// demo/build.gradle
codegenModule {
  module 'com.example.demo.DemoModule'
}

codegenManifest {
  manifest {
    path = 'build-info.properties'
    directProperty 'name', 'demo'
    sysProperty 'javaVersion', 'java.version'
  }
}
```

```groovy
// demo-bom/build.gradle     the project name must end with "-bom"
plugins {
  id 'org.febit.standard-bom' version '<version>'
}
```

```groovy
// demo-dependencies/build.gradle     the project name must end with "-dependencies"
standardMavenPublish {
  importBom project(':demo-bom')
}
```

### 3. Publish

```properties
# gradle.properties
publish.release.releasesUrl=https://oss.sonatype.org/service/local/staging/deploy/maven2
publish.release.username=
publish.release.password=
publish.release.signing=true

signing.keyId=
signing.password=
signing.secretKeyRingFile=
```

```bash
./gradlew publish -Ppublish-profile=release
```

## Plugins

| Plugin ID | Apply in | Description |
| --- | --- | --- |
| `org.febit.standard-java` | root project | Standard Java setup |
| `org.febit.standard-bom` | `-bom` module | Java platform with sibling `api` constraints |
| `org.febit.standard-maven-publish` | root project | Maven publication, signing, repository profiles |
| `org.febit.codegen-module` | root project | Generate module info classes |
| `org.febit.codegen-manifest` | root project | Generate manifest resources |

None of them applies `java`/`java-library`; add those per module.

### `org.febit.standard-java`

- **Spotless** — formats `src/*/java/**/*.java` (UTF-8, newline, no trailing whitespace); `check` depends on `spotlessCheck`
- **Java** — UTF-8 compile with `-parameters`, `-Xlint:unchecked`, `-Xlint:deprecation`; javadoc encoding (`-Xdoclint:none`); jar manifest with build/version metadata; test logging
- **Dependency management** — Spring dependency-management plugin; generated-POM customization enabled for `-dependencies` modules
- **Lombok** — applied, ordered after codegen tasks
- **Tests** — `build/tmp/tests` as `java.io.tmpdir`; `test` runs only `**/*Test.class` and excludes the `integration` tag, which runs in the registered `integrationTest` task
- **Jacoco** — applied with the XML report enabled
- **Mockito agent** — `mockito-core` from the test runtime classpath is passed to test JVMs as `-javaagent`; skipped when Mockito is absent

### `org.febit.standard-bom`

Java platform that adds its sibling Java subprojects as `api` constraints. The project name must end with `-bom`.

### `org.febit.standard-maven-publish`

Creates a `mavenArtifact` publication for each `java-library`/`java-platform` project; `-bom`/`-dependencies` modules are published with `pom` packaging. Gradle module metadata is disabled, and `install` aliases `publishToMavenLocal`.

Settings come from `publish.<profile>.*` Gradle properties, with `<profile>` taken from the `publish-profile` property (`gradle.properties` or `-Ppublish-profile=<profile>`):

| Key | Description |
| --- | --- |
| `url` | repository URL |
| `snapshotsUrl` / `releasesUrl` | URL for `-SNAPSHOT` / release versions (falls back to `url`) |
| `allowInsecureProtocol` | `true` allows plain HTTP |
| `username` / `password` | basic credentials |
| `auth-header` / `auth-header-token` | header credentials (`auth-header` defaults to `Authorization`) |
| `signing` | `true` signs the publications (needs the `signing.*` properties) |

| `standardMavenPublish` member | Description |
| --- | --- |
| `importBom(project)` | imports the project into the POM `dependencyManagement` |
| `pom { … }` | extra `MavenPom` configuration |
| `enabled` | `false` skips the publication tasks |

### `org.febit.codegen-module`

Extension `codegenModule`. Renders Java sources into `build/generated/sources/codegen-module` (Groovy `SimpleTemplateEngine`), adds them to the `main` source set and wires `compileJava`.

| `codegenModule` member | Description |
| --- | --- |
| `module(name)` | renders `name` with `defaultTemplate` |
| `module(name, template)` | renders `name` with an explicit template |
| `defaultTemplate = …` | template used by `module(name)` |
| `gitDir.set(…)` | directory holding `.git` (default: the root project's) |
| `fromClasspath(path)` / `fromFile(path)` | template from classpath / file |
| `febitTmpl()` | built-in `febit.tmpl` (`default.tmpl` is the default) |

`name` must be a full class name; the default package is not supported.

| Template parameter | Value |
| --- | --- |
| `classPackage`, `classFullName`, `classSimpleName` | parts of `name` |
| `groupId`, `artifactId`, `version` | project coordinates |
| `commitId` | HEAD commit of `gitDir`, or `UNKNOWN` |
| `buildJdk`, `buildTime` | JVM version, build time (`Instant`; `${buildTime.epochSecond}`) |

Both templates expose static `groupId()`/`artifactId()`/`version()`/`commitId()`/`buildTime()`; `febit.tmpl` additionally adds `@org.febit.lang.annotation.Generated` and implements `org.febit.lang.module.IModule` (requires `febit-lang` on the compile classpath).

### `org.febit.codegen-manifest`

Extension `codegenManifest`. Writes resources into `build/generated/resources/codegen-manifest`, adds them to the `main` source set and wires `processResources`.

| `codegenManifest` member | Description |
| --- | --- |
| `manifest { … }` | adds one output file |
| `path` | output path; `properties` is the only format (UTF-8, sorted keys) |
| `directProperty(name, value)` | constant value |
| `sysProperty(name, property)` | JVM system property `property` as `name` |
| `sysEnvProperty(name, env)` | environment variable `env` as `name` |

Missing system properties and environment variables are skipped.

## Development

Two modules:

| Module | Description |
| --- | --- |
| `febit-gradle-common` (`common/`) | Gradle utilities (`GradleUtils`, `JavaUtils`, `GitUtils`, `FileExtraUtils`, `FolderUtils`, `SocketPorts`, `RunOnce`, `Defaults`, …), the `Setup` plugin hook and the `CodegenTask` marker interface |
| `febit-gradle-basic-plugin` (`plugin-basic/`) | the plugins above |

Targets from the [Makefile](Makefile):

```bash
make clean        # ./gradlew clean
make build        # ./gradlew build
make test         # ./gradlew test
make check        # ./gradlew check
make build-fast   # ./gradlew build -x check -x test
```

`check` includes `spotlessCheck`; run `./gradlew spotlessApply` to fix formatting.

## License

Apache License, Version 2.0 — see [LICENSE.txt](LICENSE.txt).
