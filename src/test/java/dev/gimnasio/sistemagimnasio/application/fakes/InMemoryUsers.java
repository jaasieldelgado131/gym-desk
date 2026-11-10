package dev.gimnasio.sistemagimnasio.application.fakes;

import dev.gimnasio.sistemagimnasio.application.port.out.UserCredentials;
import dev.gimnasio.sistemagimnasio.application.port.out.UserRepository;
import dev.gimnasio.sistemagimnasio.domain.user.Role;
import dev.gimnasio.sistemagimnasio.domain.user.User;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryUsers implements UserRepository {
    private final Map<Long, User> byId = new LinkedHashMap<>();

    public User add(String name, Role role, boolean active) {
        User user = new User((long) byId.size() + 1, name, name.toLowerCase(), role, active);
        byId.put(user.id(), user);
        return user;
    }

    public User admin() {
        return add("Admin" + (byId.size() + 1), Role.ADMINISTRADOR, true);
    }

    public void replace(User user) {
        byId.put(user.id(), user);
    }

    @Override
    public Optional<UserCredentials> findCredentialsByUsername(String username) {
        return byId.values().stream()
                .filter(user -> user.username().equalsIgnoreCase(username))
                .map(user -> new UserCredentials(user, "hash"))
                .findFirst();
    }

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }
}
