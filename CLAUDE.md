# Developer Guide

## Project Overview

**Direct Print Service** is a Spring Boot REST API that bridges web applications with desktop printers. It runs as a local service on `http://localhost:9100` and provides endpoints for printer discovery, status management, and PDF printing with configurable options (scaling, DPI, orientation, paper size). An interactive Swagger UI is served at the root path.

## Tech Stack

- **Language:** Java 8 (1.8)
- **Framework:** Spring Boot 2.2.8.RELEASE
- **Web Server:** Jetty (Tomcat is explicitly excluded)
- **PDF Processing:** Apache PDFBox 2.0.24
- **API Docs:** SpringDoc OpenAPI 1.4.3 (Swagger UI)
- **Build Tool:** Maven (with Maven Wrapper 0.5.6)
- **Testing:** JUnit 5 via Spring Boot Test

## Build & Run Commands

```bash
# Build (tests are skipped by default via maven.test.skip=true in pom.xml)
./mvnw clean package

# Run the service
./mvnw spring-boot:run

# Run tests explicitly (skipped by default)
./mvnw test

# Build without wrapper
mvn clean package
```

The service starts on port **9100** (configured in `application.properties`).

## Project Structure

```
src/
├── main/
│   ├── java/com/github/gdl/
│   │   ├── DirectPrintServiceApplication.java  # Entry point, app bootstrap, /restart endpoint
│   │   ├── DirectPrintController.java          # REST API controller (/v1/*), core print logic
│   │   ├── DesktopConfig.java                  # System tray icon and desktop GUI integration
│   │   ├── OpenAPIConfig.java                  # Swagger/OpenAPI metadata configuration
│   │   └── Orientation.java                    # Enum: PORTRAIT, LANDSCAPE, REVERSE_LANDSCAPE
│   └── resources/
│       ├── application.properties              # Server port, logging, Spring config
│       └── images/
│           └── icons8-send-to-printer-100.png  # System tray icon
└── test/
    └── java/com/github/gdl/
        └── DirectPrintServiceApplicationTests.java  # Skeleton test (empty)
```

All source classes live in a single flat package: `com.github.gdl`.

## Architecture

### Entry Point
`DirectPrintServiceApplication.java` bootstraps Spring Boot with `headless(false)` to enable AWT/Swing desktop features (system tray). It holds a static `ConfigurableApplicationContext` for programmatic restart via the `GET /restart` endpoint.

### REST API
`DirectPrintController.java` is the main controller mapped to `/v1` with CORS `origins = "*"`.

| Method | Endpoint            | Purpose                        |
|--------|---------------------|--------------------------------|
| GET    | `/v1/printers`      | List all available printers    |
| GET    | `/v1/printer/default` | Get default printer name     |
| POST   | `/v1/printer/default` | Set default printer by name  |
| POST   | `/v1/print`         | Print Base64-encoded PDF       |
| POST   | `/v1/print/binary`  | Print uploaded PDF file        |
| POST   | `/v1/print/url`     | Print PDF from URL             |
| GET    | `/restart`          | Restart the service            |

**Common print parameters:** `scaling`, `showPageBorder`, `dpi` (default 300), `width` (default 595 — A4), `height` (default 842 — A4), `orientation`, `withPrintDialog`.

### Desktop Integration
`DesktopConfig.java` creates a system tray icon with a popup menu (About, Restart, Exit) when the OS supports it.

### PDF Processing
All print endpoints ultimately load a PDF via PDFBox (`PDDocument.load`), create a `PDFPrintable`, configure a `PrinterJob` with custom `Paper` dimensions and `PageFormat`, then either show a print dialog or print directly.

## Key Configuration

File: `src/main/resources/application.properties`

- `server.port=9100` — service port
- `spring.main.lazy-initialization=true` — deferred bean creation for faster startup
- `spring.jmx.enabled=false` — JMX disabled
- `springdoc.swagger-ui.path=/` — Swagger UI at root
- `logging.file.name=logs/direct-print-service.log` — log file path

## Code Conventions

- **Package structure:** Single flat package `com.github.gdl` (no sub-packages)
- **Naming:** PascalCase for classes/enums, camelCase for methods and variables
- **Annotations:** Spring Boot annotations for DI, `@RestController`, `@Configuration`, `@CrossOrigin`
- **Error handling:** Checked exceptions declared via `throws` (IOException, PrinterException)
- **Logging:** SLF4J via `LoggerFactory.getLogger()`
- **API docs:** `@Operation(summary = "...")` on controller methods for OpenAPI
- **Style:** Java Stream API and lambdas used where appropriate; minimal inline comments; self-documenting method names

## Dependencies (pom.xml)

| Dependency | Purpose |
|-----------|---------|
| `spring-boot-starter-web` | REST API (Tomcat excluded) |
| `spring-boot-starter-jetty` | Embedded Jetty server |
| `spring-boot-starter-test` | Testing (JUnit Vintage excluded) |
| `org.apache.pdfbox:pdfbox:2.0.24` | PDF loading and printing |
| `org.springdoc:springdoc-openapi-ui:1.4.3` | Swagger UI / OpenAPI |

## Testing

Tests are **skipped by default** (`maven.test.skip=true` in pom.xml). The only test file (`DirectPrintServiceApplicationTests.java`) is a skeleton with no test methods. To run: `./mvnw test`.

## Notes for Development

- The application requires a desktop environment (or headless-compatible setup) due to AWT system tray and `javax.print` usage.
- CORS is fully open (`*`) — this is intentional since the service runs locally.
- The default printer is managed in-memory via a static `PrintService` field in the controller; it resets on restart.
- No CI/CD pipeline is configured.
- No database or persistence layer — the service is stateless.
- License: Apache License Version 2.0.
