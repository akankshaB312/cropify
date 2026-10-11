# Integration Testing Guide for Crop Yield Prediction System

## Purpose
This document describes how to run the **DAO integration tests** that validate the interaction with a real (in‑memory) database. The tests are part of **Phase 7** and are executed under a dedicated Maven profile `integration-tests`.

## Prerequisites
- **Java 17** (or newer) installed and `JAVA_HOME` set.
- **Maven 3.8+** available on the PATH.
- **Docker** is *optional*. The integration tests use an **H2 in‑memory** database, so they run without Docker. If Docker is present, you may replace H2 with Testcontainers, but that is not required for the current setup.
- No environment variables are required for the integration tests because they use the H2 URL set by the `integration-tests` profile.

## Maven Profiles
| Profile | Description | Activation |
|---------|-------------|------------|
| `default` (no profile) | Runs only unit tests (`src/test/java/**/*Test.java`). Integration tests (`*IntegrationTest.java`) are excluded via Surefire configuration. | Implicit |
| `integration-tests` | Adds H2 as a test‑scope dependency, sets the system property `integration.tests.enabled=true`, and configures the Failsafe plugin to execute `**/*IntegrationTest.java` during the `integration-test` and `verify` phases. | Activate with `-Pintegration-tests` |

## Running Tests
### Unit Tests (always safe)
```bash
mvn clean test
```
All regular unit tests should pass. No database is started.

### Integration Tests
```bash
mvn clean verify -Pintegration-tests
```
This will:
1. Compile the project.
2. Start the **failsafe** plugin which runs the integration tests.
3. Spin up an H2 in‑memory database using the URL `jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1`.
4. Execute DAO integration tests against this isolated database.
5. Tear down the database automatically when the JVM exits.

If Docker is **not** available (the typical case on the assistant’s environment), the above command still works because it relies solely on H2.

## Safety Guard
`DatabaseConfig.validate()` contains a check that refuses connections to non‑test databases unless the system property `integration.tests.enabled` is set to `true`. This prevents accidental connections to production or development MySQL instances when running the regular unit test suite.

## Test Database Schema
The H2 database is initialized by the integration test classes (`CropRecordDAOIntegrationTest` and `CropTypeDAOIntegrationTest`). They execute the SQL statements required to create the following tables (simplified):
```sql
CREATE TABLE crop_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL,
    yield_per_hectare DECIMAL(10,2) NOT NULL
);

CREATE TABLE crop_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farmer_name VARCHAR(100) NOT NULL,
    crop_type_id BIGINT NOT NULL,
    land_area_ha DECIMAL(10,2) NOT NULL,
    predicted_yield_kg DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (crop_type_id) REFERENCES crop_type(id)
);
```
If the production schema evolves, update the DDL in the test classes accordingly.

## Adding New Integration Tests
1. Place the test class under `src/test/java/com/cropyield/dao/` and name it `*IntegrationTest.java`.
2. Use JUnit 5 annotations (`@Test`, `@BeforeEach`, `@AfterEach`).
3. Ensure the test does **not** rely on environment variables; it should use the H2 URL via `DatabaseConfig.getUrl()`.
4. The test will automatically be picked up when the `integration-tests` profile is active.

---
*Keep this file under version control. It serves as the single source of truth for running integration tests safely.*
