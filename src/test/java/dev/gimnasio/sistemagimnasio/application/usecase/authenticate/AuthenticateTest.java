package dev.gimnasio.sistemagimnasio.application.usecase.authenticate;

import dev.gimnasio.sistemagimnasio.application.port.out.PasswordHasher;
import dev.gimnasio.sistemagimnasio.application.port.out.UserCredentials;
import dev.gimnasio.sistemagimnasio.application.port.out.UserRepository;
import dev.gimnasio.sistemagimnasio.domain.shared.DomainException;
import dev.gimnasio.sistemagimnasio.domain.user.Role;
import dev.gimnasio.sistemagimnasio.domain.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticateTest {

    /** Hash falso pero distinto de la contraseña, para detectar comparaciones en claro. */
    private static final class FakeHasher implements PasswordHasher {
        @Override
        public String hash(String rawPassword) {
            return "hash(" + new StringBuilder(rawPassword).reverse() + ")";
        }

        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            return hash(rawPassword).equals(passwordHash);
        }
    }

    private static final class InMemoryUsers implements UserRepository {
        private final Map<String, UserCredentials> byUsername = new HashMap<>();
        int lookups;

        void add(User user, String passwordHash) {
            byUsername.put(user.username(), new UserCredentials(user, passwordHash));
        }

        @Override
        public Optional<UserCredentials> findCredentialsByUsername(String username) {
            lookups++;
            return Optional.ofNullable(byUsername.get(username));
        }

        @Override
        public Optional<User> findById(long id) {
            return byUsername.values().stream().map(UserCredentials::user)
                    .filter(user -> user.id() == id).findFirst();
        }
    }

    private final FakeHasher hasher = new FakeHasher();
    private final InMemoryUsers users = new InMemoryUsers();
    private final Authenticate authenticate = new Authenticate(users, hasher);

    private User register(String username, String password, Role role, boolean active) {
        User user = new User((long) (users.byUsername.size() + 1), "Persona " + username, username, role, active);
        users.add(user, hasher.hash(password));
        return user;
    }

    private String rejection(String username, String password) {
        return assertThrows(AuthenticationException.class,
                () -> authenticate.execute(new AuthenticateCommand(username, password))).getMessage();
    }


    @Test
    void usuarioYContrasenaCorrectos_devuelveAlUsuarioConSuRol() {
        User caja = register("lucia", "Mostrador#2026", Role.CAJA, true);
        register("pedro", "Recepcion#2026", Role.RECEPCION, true);

        User result = authenticate.execute(new AuthenticateCommand("lucia", "Mostrador#2026"));

        assertSame(caja, result);
        assertEquals(Role.CAJA, result.role());
    }

    @Test
    void espaciosAlrededorDelUsuario_seIgnoran() {
        User admin = register("admin", "Gym-Admin-1", Role.ADMINISTRADOR, true);
        assertSame(admin, authenticate.execute(new AuthenticateCommand("  admin \t", "Gym-Admin-1")));
    }


    @Test
    void contrasenaIncorrecta_seRechazaConMensajeClaro() {
        register("lucia", "Mostrador#2026", Role.CAJA, true);
        assertEquals(Authenticate.INVALID_CREDENTIALS, rejection("lucia", "mostrador#2026"));
    }

    /** Así no se puede averiguar qué usuarios existen probando nombres. */
    @Test
    void usuarioInexistente_yContrasenaIncorrecta_danExactamenteElMismoMensaje() {
        register("lucia", "Mostrador#2026", Role.CAJA, true);
        assertEquals(rejection("lucia", "otra"), rejection("nadie", "otra"));
    }

    @ParameterizedTest
    @CsvSource({"' Mostrador#2026'", "'Mostrador#2026 '"})
    void contrasenaConEspaciosDeMas_noEsLaMisma(String password) {
        register("lucia", "Mostrador#2026", Role.CAJA, true);
        assertEquals(Authenticate.INVALID_CREDENTIALS, rejection("lucia", password));
    }

    @Test
    void contrasenaQueContieneEspacios_funcionaSiSeEscribeIgual() {
        User user = register("pedro", "mi clave segura", Role.RECEPCION, true);
        assertSame(user, authenticate.execute(new AuthenticateCommand("pedro", "mi clave segura")));
    }

    @Test
    void escribirElHashGuardado_noDaAcceso() {
        register("lucia", "Mostrador#2026", Role.CAJA, true);
        String storedHash = users.byUsername.get("lucia").passwordHash();
        assertEquals(Authenticate.INVALID_CREDENTIALS, rejection("lucia", storedHash));
    }

    @ParameterizedTest
    @CsvSource(value = {"NULL, clave", "'', clave", "'   ', clave", "lucia, NULL", "lucia, ''"}, nullValues = "NULL")
    void datosVacios_sePidenSinConsultarAlRepositorio(String username, String password) {
        register("lucia", "Mostrador#2026", Role.CAJA, true);
        assertEquals(Authenticate.MISSING_DATA, rejection(username, password));
        assertEquals(0, users.lookups);
    }


    @Test
    void usuarioDesactivadoConContrasenaCorrecta_noEntra_ySeLeDiceElMotivo() {
        register("pedro", "Recepcion#2026", Role.ADMINISTRADOR, false);
        DomainException e = assertThrows(DomainException.class,
                () -> authenticate.execute(new AuthenticateCommand("pedro", "Recepcion#2026")));
        assertEquals("El usuario está desactivado.", e.getMessage());
    }

    /** Sin la contraseña correcta no se revela que la cuenta existe. */
    @Test
    void usuarioDesactivadoConContrasenaIncorrecta_recibeElMensajeGenerico() {
        register("pedro", "Recepcion#2026", Role.RECEPCION, false);
        assertEquals(Authenticate.INVALID_CREDENTIALS, rejection("pedro", "otra"));
    }


    @Test
    void laContrasenaNoApareceAlImprimirElComandoNiLasCredenciales() {
        AuthenticateCommand command = new AuthenticateCommand("lucia", "Mostrador#2026");
        UserCredentials credentials = new UserCredentials(
                new User(1L, "Lucía", "lucia", Role.CAJA, true), hasher.hash("Mostrador#2026"));

        assertFalse(command.toString().contains("Mostrador#2026"), command.toString());
        assertFalse(credentials.toString().contains(credentials.passwordHash()), credentials.toString());
    }

    @Test
    void elMensajeDeRechazo_noIncluyeLaContrasenaEscrita() {
        register("lucia", "Mostrador#2026", Role.CAJA, true);
        assertFalse(rejection("lucia", "MiClaveFiltrada").contains("MiClaveFiltrada"));
    }


    /** Un fallo técnico no debe disfrazarse de contraseña incorrecta. */
    @Test
    void siFallaLaVerificacionDelHash_elErrorSePropagaSinConvertirseEnRechazo() {
        IllegalStateException failure = new IllegalStateException("hash ilegible");
        PasswordHasher broken = new PasswordHasher() {
            @Override
            public String hash(String rawPassword) {
                throw failure;
            }

            @Override
            public boolean matches(String rawPassword, String passwordHash) {
                throw failure;
            }
        };
        users.add(new User(1L, "Lucía", "lucia", Role.CAJA, true), "dañado");

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> new Authenticate(users, broken).execute(new AuthenticateCommand("lucia", "Mostrador#2026")));
        assertSame(failure, thrown);
    }
}
