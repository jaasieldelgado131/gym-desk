package dev.gimnasio.sistemagimnasio.application.port.out;

import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.Optional;

public interface UserRepository {
    Optional<UserCredentials> findCredentialsByUsername(String username);

    Optional<User> findById(long id);
}
