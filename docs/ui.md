# Capa de UI

Cómo está armada la UI de JavaFX, cómo se arranca y qué se comprobó en cada entrega. La referencia visual es el prototipo funcional «Gimnasio – Prototipo funcional» (artefacto de Claude); de él se toman colores, tipografía, iconos y espaciados, pero no su lógica ni sus datos simulados: cada dato que muestra una pantalla sale de un caso de uso (ver [contratos.md](contratos.md)).

## Arranque

```
Launcher ─▶ App (JavaFX Application) ─▶ GymDeskUi ─▶ pantalla de arranque
              │                                          │ (en segundo plano)
              └─ connect(): DatabaseConfig → Database.connect → Database.migrate → Wiring.useCases
```

- `App` y `Wiring` viven en el paquete raíz, fuera de `ui/`: son la raíz de composición, el único lugar que conoce a la vez la infraestructura y la UI. La UI recibe los casos de uso ya armados en `ui.common.UseCases` y nunca importa `infrastructure`.
- La conexión y las migraciones corren fuera del hilo de JavaFX. Si fallan (PostgreSQL apagado, falta `DB_PASSWORD`…), la pantalla de arranque muestra la causa con «Reintentar» y «Salir».
- Cerrar la ventana no espera a una conexión en curso: `App` no mantiene su bloqueo durante operaciones de red y, si una conexión termina después de pedir la salida, la cierra y la descarta.

## Estructura

| Paquete / recurso | Qué contiene |
|---|---|
| `ui/GymDeskUi` | Entrada de la capa: abre la ventana y decide qué pantalla se muestra. |
| `ui/common/` | Piezas compartidas: `Background` (casos de uso fuera del hilo de JavaFX), `ErrorReport` y `FieldErrors` (errores según el contrato), `Session`, `Formats` e `Inputs` (formato y lectura de importes y fechas), `Dialogs`, `FxmlView`, `AppWindow`, `UseCases`. |
| `ui/<pantalla>/` | Una carpeta por pantalla: controlador y, cuando hay formulario, su ViewModel. |
| `resources/fxml/` | Vistas FXML, sin `fx:controller`: el controlador se construye con sus dependencias y se pasa a `FxmlView.load`. |
| `resources/css/app.css` | Tema de la app. |

Reglas que siguen todas las pantallas:

- Ningún caso de uso se llama desde el hilo de JavaFX: se usa `Background.run`.
- Los errores se tratan como dice el contrato: `ValidationException` marca cada campo (en el orden en que se validaron, el foco va al primero), `NotFoundException` avisa y recarga, `DomainException` muestra el mensaje tal cual y cualquier otro error muestra un mensaje genérico y se registra.
- La UI no calcula vigencias, totales ni estados. `Inputs` solo convierte texto: lo que no puede leer (por ejemplo `450,50`, con coma decimal) lo envía como `null` y el caso de uso responde con su propio mensaje.

## Ejecutar

```bash
docker compose up -d
DB_PASSWORD=12345 ./mvnw javafx:run
```

El proyecto compila para Java 25; con un JDK predeterminado más antiguo, define `JAVA_HOME` con un JDK 25 o posterior.

## Pruebas

```bash
./mvnw test
```

Las pruebas de `infrastructure/persistence` levantan PostgreSQL con Testcontainers. Con Docker 29 o posterior se omiten en silencio: Testcontainers 1.20.4 pide la API de Docker 1.32 y esas versiones aceptan como mínimo la 1.40. **Ajuste temporal**, sin tocar la configuración del proyecto:

```bash
./mvnw test -Dapi.version=1.40
```

La solución permanente es actualizar Testcontainers en `pom.xml`, fuera del alcance de la rama de UI.

## Validación por entrega

### Fase 0 · Arranque (10/11/2026)

Con JDK 26.0.2, Docker 29.0.3 y PostgreSQL 16 de `docker-compose.yml`:

| Comprobación | Resultado |
|---|---|
| `./mvnw test` | 444 pruebas: 0 fallos, 0 errores, 65 omitidas (Testcontainers, ver arriba) |
| `./mvnw test -Dapi.version=1.40` | 444 pruebas: 0 fallos, 0 errores, 0 omitidas |
| Pruebas de `ui/common` | 48: `Background`, `ErrorReport` (incluido el orden de los campos), `Formats`, `Inputs` (incluidos `450,50`, `1,2`, `1$2`, `12 34`), `Session` |
| Arranque con la base | Aplica V1–V3 en una base vacía y muestra el nombre del gimnasio y la fecha |
| Arranque sin `DB_PASSWORD` | Muestra «Falta la variable de entorno DB_PASSWORD.» con «Reintentar» y «Salir» |
| Cierre durante una conexión que no responde (servidor que acepta TCP y no contesta) | `stop()` termina en 1 ms; antes de la corrección esperaba a que fallara la conexión |
