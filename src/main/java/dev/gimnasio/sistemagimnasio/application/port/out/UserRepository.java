package dev.gimnasio.sistemagimnasio.application.port.out;

import java.util.Optional;

public interface UserRepository {
    Optional<UserCredentials> findCredentialsByUsername(String username);
}
