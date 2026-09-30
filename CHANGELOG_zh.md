# Changelog

## [1.7.0] - 2026-09-30

### Breaking Changes

- **standard-java**: `test` 仅运行 `**/*Test.class` 且排除 `integration` 标签；带标签的测试改由新增的 `integrationTest` 任务运行

### New Features

- **standard-java**: 新增 `SpotlessSetup`，对 `src/*/java/**/*.java` 应用 spotless（UTF-8、`endWithNewline`、
  `trimTrailingWhitespace`），`check` 依赖 `spotlessCheck`
- **standard-java**: 新增 `TestTaskSetup`，所有 `Test` 任务使用 `build/tmp/tests` 作为 `java.io.tmpdir`，`test` 限定
  `**/*Test.class` 并排除 `integration` 标签，新增 `integrationTest` 任务运行带标签的测试
- **standard-java**: 新增 `MockitoAgentSetup` / `MockitoAgentProvider`，将测试运行时 classpath 上的 `mockito-core` 以
  `-javaagent` 传给测试 JVM，未使用 Mockito 时不注入
- **standard-java**: 新增 `JacocoSetup`，应用 jacoco 并开启 `JacocoReport` XML 报告

### Fixes

- **standard-java**: `LombokSetup` 用 `configureEach` 让 `generateEffectiveLombokConfig` 排在 codegen 任务之后
- **standard-java**: `TestTaskSetup` 与 `MockitoAgentSetup` 在 `standard-java` 先于 `java` 应用时同样生效
- **common**: `JavaUtils.classSimpleName` 无包名分隔符时返回原名（此前返回空串）

### Improvements

- **standard-java**: 固定 lombok 1.18.48
- **standard-maven-publish**: 以 `providers.gradlePropertiesPrefixedBy` 替换废弃的 `project.getProperties()`

### Dependencies

- Gradle Wrapper 9.5.1 → 9.7.1
- commons-collections4 4.5.0 → 4.6.0
- freefair lombok plugin 9.5.0 → 9.7.0
- junit 6.1.0 → 6.1.3
- lombok 1.18.46 → 1.18.48
- mockito 5.23.0 → 5.24.0
- slf4j 2.0.18 → 2.0.19
- spotbugs 4.10.2 → 4.10.4
- 新增 spotless 8.10.3
- 新增 tabletest-junit 1.2.2
- 移除 license 插件

### Build

- license 插件替换为 spotless，并统一代码格式

### Tests

- 补充 `febit-gradle-common` 工具类单测（多为 tabletest-junit 表驱动）
- 补充 `standard/java`、`standard/bom`、`standard/maven-publish`、`codegen/*` 单测
