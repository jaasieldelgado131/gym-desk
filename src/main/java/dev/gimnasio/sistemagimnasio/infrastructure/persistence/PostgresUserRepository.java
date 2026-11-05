package dev.gimnasio.sistemagimnasio.infrastructure.persistence;

import dev.gimnasio.sistemagimnasio.application.port.out.UserCredentials;
import dev.gimnasio.sistemagimnasio.application.port.out.UserRepository;
import dev.gimnasio.sistemagimnasio.domain.user.Role;
import dev.gimnasio.sistemagimnasio.domain.user.User;
import org.jdbi.v3.core.Jdbi;

import java.util.Objects;
import java.util.Optional;

public final class PostgresUserRepository implements UserRepository {
    private final Jdbi jdbi;

    public PostgresUserRepository(Jdbi jdbi) {
        this.jdbi = Objects.requireNonNull(jdbi, "jdbi");
    }

    @Override
    public Optional<UserCredentials> findCredentialsByUsername(String username) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT id, name, username, role, active, password_hash
                        FROM app_user
                        WHERE username = :username""")
                .bind("username", username)
                .map((rs, ctx) -> new UserCredentials(
                        new User(rs.getLong("id"), rs.getString("name"), rs.getString("username"),
                                Role.valueOf(rs.getString("role")), rs.getBoolean("active")),
                        rs.getString("password_hash")))
                .findOne());
    }
}
