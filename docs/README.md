# Sistema Gimnasio

Aplicación de escritorio (JavaFX 21, Java 25, Maven) para administrar un gimnasio: socios, cobros, ventas de mostrador, inventario, usuarios y configuración.

Arquitectura por capas (hexagonal): el dominio es Java puro y no depende de nada; la aplicación define casos de uso y puertos; infraestructura y UI implementan esos puertos o los consumen.

## Estado actual

| Capa | Estado |
|---|---|
| `domain` | Entidades, value objects y reglas creados a partir de la especificación |
| `application` | Caso de uso `Authenticate` con sus puertos `UserRepository` y `PasswordHasher` |
| `infrastructure` | Vacía |
| `ui` | Vacía (solo queda la plantilla `HelloApplication` de IntelliJ) |

## Modelo de datos

Tablas, relaciones y reglas de la base: [modelo-de-datos.md](modelo-de-datos.md). El DBML de origen está en [modelo-de-datos.dbml](modelo-de-datos.dbml).

## Estructura

Paquete base: `dev.gimnasio.sistemagimnasio`. Lo marcado como *(planeado)* aún no existe en disco.

```
sistema-gimnasio/
├── pom.xml
├── docs/README.md
└── src/
    ├── main/java/dev/gimnasio/sistemagimnasio/
    │   ├── HelloApplication, HelloController, Launcher   plantilla de IntelliJ; se reemplaza por App.java
    │   │
    │   ├── domain/                      Java puro: reglas y conceptos del negocio
    │   │   ├── payment/                 Folio, CashPayment, PaymentMethod (lo comparten billing y sales)
    │   │   ├── shared/                  Money, DomainException
    │   │   ├── member/                  Member, Membership, Plan, MemberStatus, MembershipRules, Deactivation, Phone, Email
    │   │   ├── billing/                 MembershipPayment
    │   │   ├── sales/                   Sale, SaleLine
    │   │   ├── inventory/               Product, StockMovement, ProductCategory, MovementReason
    │   │   ├── settings/                GymSettings
    │   │   └── user/                    User, Role, Permission
    │   │
    │   ├── application/                 lo que el sistema hace
    │   │   ├── port/out/                (planeado) interfaces que la aplicación necesita del exterior
    │   │   │   ├── MemberRepository, MembershipPaymentRepository, SaleRepository
    │   │   │   ├── ProductRepository, UserRepository, SettingsRepository
    │   │   │   └── FolioGenerator, AppClock, PasswordHasher, ReceiptPrinter
    │   │   └── usecase/                 una carpeta por caso de uso: el caso, su command, su result y sus excepciones
    │   │       ├── authenticate/        Authenticate, AuthenticateCommand, AuthenticationException
    │   │       ├── (planeado) chargemembership/, sellatcounter/
    │   │       ├── (planeado) deactivatemember/, reactivatemember/, voidmembershippayment/
    │   │       ├── (planeado) registerinventorymovement/, daysummary/
    │   │       └── (planeado) plancatalog/, productcatalog/, useradmin/   (CRUD agrupado por área)
    │   │
    │   ├── infrastructure/              implementa los puertos (vacía)
    │   │   ├── persistence/             (planeado) repositorios con JDBI + SQL, migraciones Flyway
    │   │   ├── security/                (planeado) hash de contraseñas (Argon2) y sesión
    │   │   ├── printing/                (planeado) recibos y tickets
    │   │   ├── backup/                  (planeado) pg_dump diario
    │   │   └── time/                    (planeado) reloj del sistema (implementa AppClock)
    │   │
    │   └── ui/                          una carpeta por pantalla (FXML + controlador + ViewModel) (vacía)
    │       ├── login/  dashboard/  billing/  sales/  members/  deactivations/     (planeado)
    │       ├── plans/  products/  inventory/  users/  settings/                   (planeado)
    │       └── common/                  navegación, diálogos, formatos, atajos     (planeado)
    │
    ├── main/resources/
    │   ├── db/migration/                (planeado) V1__schema.sql, V2__initial_data.sql
    │   ├── i18n/messages_es.properties  (planeado)
    │   └── fxml/   css/app.css   logback.xml   (planeado)
    │
    └── test/java/dev/gimnasio/sistemagimnasio/
        ├── domain/member/               MembershipRulesTest
        ├── application/usecase/         pruebas con repositorios falsos en memoria, en la misma carpeta que su caso de uso
        └── infrastructure/persistence/  pruebas contra PostgreSQL (Testcontainers)
```

## Reglas de dependencia

- `domain` no importa nada de `application`, `infrastructure` ni `ui`, ni JavaFX ni librerías de persistencia.
- `application` depende solo de `domain`; define los puertos (`port/out`) que implementa `infrastructure`.
- `infrastructure` y `ui` dependen de `application` y `domain`, nunca entre sí.

## Tecnologías previstas

Aún **no** están en el `pom.xml`: PostgreSQL, JDBI, Flyway, Argon2, Logback, Testcontainers. Hoy el `pom.xml` incluye JavaFX 21 y JUnit 5.

## Desarrollo

```bash
./mvnw compile
./mvnw javafx:run
```

## Base de datos (Docker)

```bash
docker compose up -d
```

`docker-compose.yml` levanta solo PostgreSQL 16 (base, usuario `gimnasio`, puerto 5432, volumen persistente `pgdata`). La app se ejecuta en el equipo con `./mvnw javafx:run`. La contraseña por defecto es solo para desarrollo; cámbiala con la variable `DB_PASSWORD`.
