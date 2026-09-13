package reproducer.issue1819;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

public final class TestDatabase implements AutoCloseable {

    public enum Type {
        H2,
        POSTGRESQL,
        MYSQL
    }

    private final String name;
    private final AutoCloseable container;
    private final Map<String, Object> properties;

    private TestDatabase(String name, AutoCloseable container, Map<String, Object> properties) {
        this.name = name;
        this.container = container;
        this.properties = properties;
    }

    static TestDatabase start(Type type) {
        switch (type) {
            case H2:
                return h2();
            case POSTGRESQL:
                return postgres();
            case MYSQL:
                return mysql();
            default:
                throw new IllegalArgumentException("Unsupported database: " + type);
        }
    }

    private static TestDatabase h2() {
        Map<String, Object> properties = jdbcProperties(
                "org.h2.Driver",
                "jdbc:h2:mem:issue1819;DB_CLOSE_DELAY=-1",
                "sa",
                "",
                "org.hibernate.dialect.H2Dialect");
        return new TestDatabase("H2 2.2", null, properties);
    }

    private static TestDatabase postgres() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("issue1819")
                .withUsername("blaze")
                .withPassword("blaze");
        container.start();
        return new TestDatabase("PostgreSQL 15", container, jdbcProperties(
                container.getDriverClassName(),
                container.getJdbcUrl(),
                container.getUsername(),
                container.getPassword(),
                "org.hibernate.dialect.PostgreSQLDialect"));
    }

    private static TestDatabase mysql() {
        MySQLContainer<?> container = new MySQLContainer<>("mysql:8.4")
                .withDatabaseName("issue1819")
                .withUsername("blaze")
                .withPassword("blaze");
        container.start();
        return new TestDatabase("MySQL 8.4", container, jdbcProperties(
                container.getDriverClassName(),
                container.getJdbcUrl(),
                container.getUsername(),
                container.getPassword(),
                "org.hibernate.dialect.MySQLDialect"));
    }

    private static Map<String, Object> jdbcProperties(
            String driver, String url, String user, String password, String dialect) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.driver", driver);
        properties.put("jakarta.persistence.jdbc.url", url);
        properties.put("jakarta.persistence.jdbc.user", user);
        properties.put("jakarta.persistence.jdbc.password", password);
        properties.put("hibernate.dialect", dialect);
        return properties;
    }

    String name() {
        return name;
    }

    Map<String, Object> properties() {
        return properties;
    }

    @Override
    public void close() throws Exception {
        if (container != null) {
            container.close();
        }
    }
}
