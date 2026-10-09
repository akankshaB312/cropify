# Crop Yield Prediction System

## Phase 1 – Project Scaffold

This repository contains the initial backend scaffold for the **Crop Yield Prediction System**. It includes:
- Maven project configuration (`pom.xml`).
- Database schema and seed data.
- Basic documentation on prerequisites, database setup, and how to build/run tests.

### Prerequisites
- **Java 17** (or newer)
- **Maven** 3.8+
- **MySQL 8.x** server
- (Optional) MySQL Workbench or any client that can execute `.sql` scripts.

### Database Setup
1. Create a MySQL database (if not already existing):
   ```sql
   CREATE DATABASE IF NOT EXISTS crop_management;
   ```
2. Run the schema script to create tables and indexes:
   ```bash
   mysql -u <user> -p < crop_management < database/schema.sql
   ```
3. Populate demo data:
   ```bash
   mysql -u <user> -p < crop_management < database/seed.sql
   ```
   The seed script is idempotent – it can be run multiple times without creating duplicate `crop_types`.

### Building the Project
```bash
mvn clean compile        # compile source code
mvn test                # run unit tests (none yet, but the build verifies the pom)
```

> **Note:** Java's `System.getenv` does **not** automatically read a `.env` file. You must export the required environment variables in your shell or IDE before running the application.

### Environment Variables
Create a `.env` file (or configure your environment) with the following keys:
- `DB_URL` – JDBC URL, e.g. `jdbc:mysql://localhost:3306/crop_management`
- `DB_USER` – MySQL username
- `DB_PASSWORD` – MySQL password

A template is provided as `.env.example`.

### Testing the Database Connection
Below is a minimal example that attempts to obtain a JDBC connection using the configuration classes.
```java
package com.cropyield;

import com.cropyield.config.DBConnection;
import java.sql.Connection;

public class ConnectionTest {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Connection successful: " + !conn.isClosed());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
```
Compile and run it with Maven (requires the `exec-maven-plugin` configured or run from your IDE):
```bash
mvn compile exec:java -Dexec.mainClass="com.cropyield.ConnectionTest"
```
Make sure the environment variables `DB_URL`, `DB_USER`, and `DB_PASSWORD` are set in the same process before executing the program.

### Next Steps (Phase 2)
- Implement data access objects (DAO) using JDBC.
- Add service layer to calculate predicted yields.
- Write unit and integration tests.

---
*This README is part of Phase 1 and will be expanded as the project evolves.*
