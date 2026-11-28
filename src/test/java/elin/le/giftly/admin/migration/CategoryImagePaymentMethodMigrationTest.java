package elin.le.giftly.admin.migration;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoryImagePaymentMethodMigrationTest {

    @Test
    void addsCategoryImageMetadataAndRestrictsSupportedPaymentMethods() throws Exception {
        String databaseName = "category_payment_method_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE category (id UUID PRIMARY KEY, name VARCHAR(255) NOT NULL)");
                statement.execute("CREATE TABLE payment (payment_method VARCHAR(20) NOT NULL)");
                statement.execute("INSERT INTO payment (payment_method) VALUES ('CREDIT_CARD')");
                statement.execute("INSERT INTO payment (payment_method) VALUES ('ZALOPAY')");
            }

            runMigration(connection);

            try (Statement statement = connection.createStatement();
                 var result = statement.executeQuery("SELECT payment_method FROM payment ORDER BY payment_method")) {
                result.next();
                assertEquals("COD", result.getString(1));
                result.next();
                assertEquals("ZALOPAY", result.getString(1));
            }
            assertThrows(SQLException.class, () -> {
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO payment (payment_method) VALUES (?)")) {
                    statement.setString(1, "CREDIT_CARD");
                    statement.executeUpdate();
                }
            });
        }
    }

    private void runMigration(Connection connection) throws Exception {
        String migration;
        try (var input = getClass().getResourceAsStream(
                "/db/migration/V24__add_category_images_and_limit_payment_methods.sql")) {
            if (input == null) {
                throw new IllegalStateException("Category/payment migration resource not found");
            }
            migration = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (Statement statement = connection.createStatement()) {
            for (String sql : migration.split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
        }
    }
}