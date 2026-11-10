package dev.gimnasio.sistemagimnasio.infrastructure.persistence;

import dev.gimnasio.sistemagimnasio.application.port.out.UserCredentials;
import dev.gimnasio.sistemagimnasio.application.port.out.UserRepository;
import dev.gimnasio.sistemagimnasio.domain.user.Role;
import dev.gimnasio.sistemagimnasio.domain.user.User;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class PostgresUserRepository implements UserRepository {
    private final Jdbi jdbi;

    public PostgresUserRepository(Jdbi jdbi) {
        this.jdbi = Objects.requireNonNull(jdbi, "jdbi");
    }

    /** Sin distinguir mayúsculas, igual que el índice único de nombre_usuario. */
    @Override
    public Optional<UserCredentials> findCredentialsByUsername(String username) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT id, nombre, nombre_usuario, rol::text AS rol, activo, contrasena_hash
                        FROM usuario
                        WHERE lower(nombre_usuario) = lower(:username)
                        """)
                .bind("username", username)
                .map(PostgresUserRepository::mapCredentials)
                .findOne());
    }

    @Override
    public Optional<User> findById(long id) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT id, nombre, nombre_usuario, rol::text AS rol, activo
                        FROM usuario
                        WHERE id = :id
                        """)
                .bind("id", id)
                .map(PostgresUserRepository::mapUser)
                .findOne());
    }

    private static UserCredentials mapCredentials(ResultSet rs, StatementContext ctx) throws SQLException {
        return new UserCredentials(mapUser(rs, ctx), rs.getString("contrasena_hash"));
    }

    private static User mapUser(ResultSet rs, StatementContext ctx) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getString("nombre_usuario"),
                parseRole(rs.getString("rol")),
                rs.getBoolean("activo"));
    }

    private static Role parseRole(String role) {
        return Role.valueOf(role.toUpperCase(Locale.ROOT));
    }
}
