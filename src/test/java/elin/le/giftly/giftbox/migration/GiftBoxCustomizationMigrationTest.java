package elin.le.giftly.giftbox.migration;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GiftBoxCustomizationMigrationTest {

    @Test
    void migratesBoxAndLegacyComponentsAndEnforcesOnePerComponentType() throws Exception {
        String databaseName = "gift_box_customization_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL")) {
            UUID boxProductId = UUID.randomUUID();
            UUID cardProductId = UUID.randomUUID();
            UUID accessoryProductId = UUID.randomUUID();
            UUID contentProductId = UUID.randomUUID();
            UUID boxVariantId = UUID.randomUUID();
            UUID cardVariantId = UUID.randomUUID();
            UUID accessoryVariantId = UUID.randomUUID();
            UUID contentVariantId = UUID.randomUUID();
            UUID configId = UUID.randomUUID();
            UUID duplicateConfigId = UUID.randomUUID();

            createLegacySchema(connection);
            insertProduct(connection, boxProductId, "READY_MADE_GIFT_BOX");
            insertProduct(connection, cardProductId, "GREETING_CARD");
            insertProduct(connection, accessoryProductId, "PACKAGING_ACCESSORY");
            insertProduct(connection, contentProductId, "REGULAR");
            insertVariant(connection, boxVariantId, boxProductId);
            insertVariant(connection, cardVariantId, cardProductId);
            insertVariant(connection, accessoryVariantId, accessoryProductId);
            insertVariant(connection, contentVariantId, contentProductId);
            insertLegacyConfig(connection, configId, boxVariantId);
            insertLegacyItem(connection, UUID.randomUUID(), configId, cardVariantId, 1);
            insertLegacyItem(connection, UUID.randomUUID(), configId, accessoryVariantId, 1);
            insertLegacyItem(connection, UUID.randomUUID(), configId, contentVariantId, 3);
            insertLegacyConfig(connection, duplicateConfigId, boxVariantId);
            insertLegacyItem(connection, UUID.randomUUID(), duplicateConfigId, accessoryVariantId, 1);
            insertLegacyItem(connection, UUID.randomUUID(), duplicateConfigId, accessoryVariantId, 1);

            runMigration(connection);

            assertEquals(1, countComponents(connection, configId, "BOX"));
            assertEquals(1, countComponents(connection, configId, "GREETING_CARD"));
            assertEquals(1, countComponents(connection, configId, "ACCESSORY"));
            assertEquals(1, countContentItems(connection, configId, 3));
            assertEquals(0, countComponents(connection, duplicateConfigId, "ACCESSORY"));
            assertEquals(2, countContentItems(connection, duplicateConfigId, 1));
            assertThrows(SQLException.class,
                    () -> insertComponent(connection, UUID.randomUUID(), configId, cardVariantId, "GREETING_CARD", 1));
            assertThrows(SQLException.class,
                    () -> insertComponent(connection, UUID.randomUUID(), configId, contentVariantId, "RIBBON", 2));
            assertThrows(SQLException.class,
                    () -> insertProduct(connection, UUID.randomUUID(), "REGULAR", "RIBBON"));
        }
    }

    private void createLegacySchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE product (id UUID PRIMARY KEY, product_type VARCHAR(30) NOT NULL)");
            statement.execute("CREATE TABLE product_variant (id UUID PRIMARY KEY, product_id UUID NOT NULL)");
            statement.execute("CREATE TABLE gift_box_config (id UUID PRIMARY KEY, box_variant_id UUID NOT NULL)");
            statement.execute("CREATE TABLE gift_box_config_item ("
                    + "id UUID PRIMARY KEY, config_id UUID NOT NULL, product_variant_id UUID NOT NULL, quantity INTEGER NOT NULL)");
        }
    }

    private void insertProduct(Connection connection, UUID id, String productType) throws SQLException {
        insertProduct(connection, id, productType, null);
    }

    private void insertProduct(Connection connection, UUID id, String productType, String componentType)
            throws SQLException {
        String componentColumn = componentType == null ? "" : ", custom_component_type";
        String componentValue = componentType == null ? "" : ", '" + componentType + "'";
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO product (id, product_type" + componentColumn + ") VALUES ('"
                    + id + "', '" + productType + "'" + componentValue + ")");
        }
    }

    private void insertVariant(Connection connection, UUID id, UUID productId) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO product_variant (id, product_id) VALUES ('" + id + "', '" + productId + "')");
        }
    }

    private void insertLegacyConfig(Connection connection, UUID id, UUID boxVariantId) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO gift_box_config (id, box_variant_id) VALUES ('" + id + "', '" + boxVariantId + "')");
        }
    }

    private void insertLegacyItem(Connection connection, UUID id, UUID configId, UUID variantId, int quantity)
            throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO gift_box_config_item (id, config_id, product_variant_id, quantity) VALUES ('"
                    + id + "', '" + configId + "', '" + variantId + "', " + quantity + ")");
        }
    }

    private void insertComponent(
            Connection connection, UUID id, UUID configId, UUID variantId, String componentType, int quantity)
            throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO gift_box_config_item (id, config_id, product_variant_id, quantity, component_type) "
                    + "VALUES ('" + id + "', '" + configId + "', '" + variantId + "', " + quantity + ", '"
                    + componentType + "')");
        }
    }

    private int countComponents(Connection connection, UUID configId, String componentType) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM gift_box_config_item "
                     + "WHERE config_id = '" + configId + "' AND component_type = '" + componentType + "'")) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private int countContentItems(Connection connection, UUID configId, int quantity) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM gift_box_config_item "
                     + "WHERE config_id = '" + configId + "' AND component_type IS NULL AND quantity = " + quantity)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private void runMigration(Connection connection) throws Exception {
        String migration;
        try (var input = getClass().getResourceAsStream(
                "/db/migration/V19__custom_gift_box_components.sql")) {
            if (input == null) {
                throw new IllegalStateException("Gift box customization migration resource not found");
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