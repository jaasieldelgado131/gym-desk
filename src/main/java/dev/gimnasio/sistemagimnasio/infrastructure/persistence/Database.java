package dev.gimnasio.sistemagimnasio.infrastructure.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public final class Database {
    private static final int MAX_POOL_SIZE = 5;

    private Database() {
    }

    /** Falla al crearse si la base no responde, para detectarlo al arrancar y no al primer uso. */
    public static HikariDataSource connect(DatabaseConfig config) {
        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("gimnasio");
        hikari.setJdbcUrl(config.url());
        hikari.setUsername(config.user());
        hikari.setPassword(config.password());
        hikari.setMaximumPoolSize(MAX_POOL_SIZE);
        return new HikariDataSource(hikari);
    }

    public static void migrate(DataSource dataSource) {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}
