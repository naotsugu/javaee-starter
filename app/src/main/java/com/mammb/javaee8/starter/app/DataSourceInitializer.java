package com.mammb.javaee8.starter.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@DataSourceDefinition(
    name = DataSourceInitializer.DS_NAME,
    className = "org.h2.jdbcx.JdbcDataSource",
    url = "jdbc:h2:mem:identity;DB_CLOSE_DELAY=-1",
    initialPoolSize = 3,
    minPoolSize = 3,
    maxPoolSize = 100,
    properties = {
        "fish.payara.is-connection-validation-required=true",
        "fish.payara.connection-validation-method=custom-validation",
        "fish.payara.validation-classname=org.glassfish.api.jdbc.validation.H2ConnectionValidation",
        "fish.payara.connection-leak-timeout-in-seconds=5",
        "fish.payara.statement-leak-timeout-in-seconds=5"
    })
@ApplicationScoped
public class DataSourceInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataSourceInitializer.class);

    public static final String DS_NAME = "java:app/MyApp/MainDs";

    @Resource(lookup = DS_NAME)
    private DataSource dataSource;

    public void init(@Observes @Initialized(ApplicationScoped.class) Object o) {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.setQueryTimeout(10);
            boolean isValid = stmt.execute("SELECT '1'");
            if (!isValid) {
                throw new RuntimeException("## Connection validation failed.");
            }
            log.info("## Connection is valid");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
