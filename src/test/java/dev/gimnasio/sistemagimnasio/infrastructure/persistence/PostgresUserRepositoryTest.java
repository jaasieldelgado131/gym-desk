package dev.gimnasio.sistemagimnasio.infrastructure.persistence;

import com.zaxxer.hikari.HikariDataSource;
import dev.gimnasio.sistemagimnasio.application.port.out.UserCredentials;
import dev.gimnasio.sistemagimnasio.application.usecase.authenticate.Authenticate;
import dev.gimnasio.sistemagimnasio.application.usecase.authenticate.AuthenticateCommand;
import dev.gimnasio.sistemagimnasio.application.usecase.authenticate.AuthenticationException;
import dev.gimnasio.sistemagimnasio.domain.user.Role;
import dev.gimnasio.sistemagimnasio.domain.user.User;
import dev.gimnasio.sistemagimnasio.infrastructure.security.Argon2PasswordHasher;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Contra PostgreSQL real; se omite si Docker no está disponible. */
@Testcontainers(disabledWithoutDocker = true)
class PostgresUserRepositoryTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    private static HikariDataSource dataSource;
    private static Jdbi jdbi;
    private final PostgresUserRepository repository = new PostgresUserRepository(jdbi);

    @BeforeAll
    static void startDatabase() {
        dataSource = Database.connect(new DatabaseConfig(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword()));
        Database.migrate(dataSource);
        jdbi = Jdbi.create(dataSource);
    }

    @AfterAll
    static void closeDatabase() {
        dataSource.close();
    }

    @BeforeEach
    void emptyUsers() {
        jdbi.useHandle(handle -> handle.execute("TRUNCATE app_user RESTART IDENTITY"));
    }

    private long insert(String name, String username, String role, boolean active, String passwordHash) {
        return jdbi.withHandle(handle -> handle.createUpdate("""
                        INSERT INTO app_user (name, username, password_hash, role, active)
                        VALUES (:name, :username, :hash, :role, :active)""")
                .bind("name", name).bind("username", username).bind("hash", passwordHash)
                .bind("role", role).bind("active", active)
                .executeAndReturnGeneratedKeys("id").mapTo(Long.class).one());
    }

    @Test
    void encuentraAlUsuario_conTodosSusDatosYSuHash() {
        long id = insert("Lucía Pérez", "lucia", "CAJA", true, "hash-de-lucia");
        insert("Pedro Ruiz", "pedro", "RECEPCION", false, "hash-de-pedro");

        assertEquals(Optional.of(new UserCredentials(new User(id, "Lucía Pérez", "lucia", Role.CAJA, true), "hash-de-lucia")),
                repository.findCredentialsByUsername("lucia"));
    }

    @Test
    void usuarioDesactivado_seDevuelveIgual_paraQueElCasoDeUsoDecida() {
        insert("Pedro Ruiz", "pedro", "RECEPCION", false, "hash-de-pedro");
        assertFalse(repository.findCredentialsByUsername("pedro").orElseThrow().user().active());
    }

    @Test
    void usuarioInexistente_devuelveVacio() {
        insert("Lucía Pérez", "lucia", "CAJA", true, "hash-de-lucia");
        assertEquals(Optional.empty(), repository.findCredentialsByUsername("nadie"));
    }

    @Test
    void laBusqueda_esExacta_tambienEnMayusculas() {
        insert("Lucía Pérez", "lucia", "CAJA", true, "hash-de-lucia");
        assertEquals(Optional.empty(), repository.findCredentialsByUsername("Lucia"));
    }

    @Test
    void nombreDeUsuarioRepetido_loRechazaLaBase() {
        insert("Lucía Pérez", "lucia", "CAJA", true, "hash-1");
        assertThrows(UnableToExecuteStatementException.class,
                () -> insert("Lucía Gómez", "lucia", "RECEPCION", true, "hash-2"));
    }

    @Test
    void rolDesconocidoOUsuarioVacio_loRechazaLaBase() {
        assertThrows(UnableToExecuteStatementException.class, () -> insert("Ana", "ana", "GERENTE", true, "hash"));
        assertThrows(UnableToExecuteStatementException.class, () -> insert("Ana", "  ", "CAJA", true, "hash"));
    }

    @Test
    void migrarOtraVez_noFallaNiDuplicaNada() {
        insert("Lucía Pérez", "lucia", "CAJA", true, "hash-de-lucia");
        assertDoesNotThrow(() -> Database.migrate(dataSource));
        assertTrue(repository.findCredentialsByUsername("lucia").isPresent());
    }

    @Test
    void autenticacionCompleta_conPostgresYArgon2() {
        Argon2PasswordHasher hasher = new Argon2PasswordHasher();
        long id = insert("Lucía Pérez", "lucia", "CAJA", true, hasher.hash("Mostrador#2026"));
        Authenticate authenticate = new Authenticate(repository, hasher);

        assertEquals(id, authenticate.execute(new AuthenticateCommand("lucia", "Mostrador#2026")).id());
        assertThrows(AuthenticationException.class,
                () -> authenticate.execute(new AuthenticateCommand("lucia", "otra")));
    }
}
