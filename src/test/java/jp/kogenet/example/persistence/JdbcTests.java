package jp.kogenet.example.persistence;

import static com.ninja_squad.dbsetup.Operations.insertInto;
import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static com.ninja_squad.dbsetup.Operations.sql;
import static org.assertj.db.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.generator.ValueGenerators;
import com.ninja_squad.dbsetup.operation.Operation;

import org.assertj.db.type.Changes;
import org.assertj.db.type.DateValue;
import org.assertj.db.type.Source;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import jp.kogenet.example.persistence.entities.WorkItem;

public class JdbcTests {

    private static DbSetupTracker dbSetupTracker = new DbSetupTracker();
    private static DataSourceDestination destination;
    private static Source dbSource;

    private static void setupDataSourceJNDI(DataSource source)
            throws NamingException {

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "jp.kogenet.example.persistence.StandaloneContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES,
                "jp.kogenet.example.persistence");

        Context context = new InitialContext();
        context.bind("java:comp/env/jdbc/datasource", source);

    }

    private DataSource getDataSource() throws NamingException {

        Context context = new InitialContext();
        DataSource ds = (DataSource) context
                .lookup("java:comp/env/jdbc/datasource");

        return ds;
    }

    private Connection getConnection() throws SQLException {
        // configuration from hibernate.cfg.xml
        Configuration configure = new Configuration().configure();
        String url = configure.getProperty(AvailableSettings.URL);
        String user = configure.getProperty(AvailableSettings.USER);
        String pass = configure.getProperty(AvailableSettings.PASS);

        Connection connection = DriverManager.getConnection(url, user, pass);

        return connection;
    }

    private static ZonedDateTime toZonedDateTimeSafe(OffsetDateTime o,
            ZoneId zoneId) {
        return (o != null) ? o.atZoneSameInstant(zoneId) : null;
    }

    private static final Operation SETUP_WORK_ITEMS = sequenceOf(
    // @formatter:off
        sql("DROP TABLE IF EXISTS work_items CASCADE"),
        sql("CREATE TABLE work_items ( id SERIAL PRIMARY KEY"
                + ", code CHAR(5) NOT NULL" 
                + ", name VARCHAR(60) NOT NULL" 
                + ", price NUMERIC(10,2) NOT NULL"
                + ", quantity SMALLINT"
                + ", purchase_date date"
                + ", last_updated_at timestamp with time zone"
                + ", clob_data text"
                + ", blob_data bytea"
                + ")")
        );
    // @formatter:on

    private static final Operation INITIALIZE_WORK_ITEMS = sequenceOf(
    // @formatter:off
        sql("TRUNCATE TABLE work_items RESTART IDENTITY"),
        insertInto("work_items")
            .withGeneratedValue("code",
                ValueGenerators.stringSequence("").startingAt(1000L).incrementingBy(10).withLeftPadding(5))
            .withGeneratedValue("name",
                ValueGenerators.stringSequence("ITEM-").startingAt(1000L).incrementingBy(10).withLeftPadding(5))
            .withGeneratedValue("price",
                ValueGenerators.sequence().startingAt(1000L).incrementingBy(10))
            .withGeneratedValue("quantity",
                ValueGenerators.sequence().startingAt(10L).incrementingBy(1))
            .withGeneratedValue("purchase_date",
                ValueGenerators.dateSequence().startingAt(LocalDate.parse("2020-09-01")).incrementingBy(1, ChronoUnit.DAYS))
            .columns("last_updated_at")
                .repeatingValues(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Asia/Tokyo")))
                .times(10)
            .build() 
        );
    // @formatter:on

    @BeforeAll
    static void setup() throws NamingException {

        // configuration from hibernate.cfg.xml
        Configuration configure = new Configuration().configure();
        String url = configure.getProperty(AvailableSettings.URL);
        String user = configure.getProperty(AvailableSettings.USER);
        String pass = configure.getProperty(AvailableSettings.PASS);

        // create postgreSQL datasource.
        PGSimpleDataSource source = new PGSimpleDataSource();
        source.setURL(url);
        source.setUser(user);
        source.setPassword(pass);

        // setup standalone JNDI.
        setupDataSourceJNDI(source);

        // setup DBSetup.
        destination = new DataSourceDestination(source);

        DbSetup dbSetup = new DbSetup(destination, JdbcTests.SETUP_WORK_ITEMS);
        dbSetup.launch();

        // setup AssertJ-DB.
        dbSource = new Source(url, user, pass);

    }

    @BeforeEach
    void preparation() {

        // @formatter:off
        Operation operation = sequenceOf(
                JdbcTests.INITIALIZE_WORK_ITEMS);
        // @formatter:on
        DbSetup dbSetup = new DbSetup(destination, operation);

        dbSetupTracker.launchIfNecessary(dbSetup);

    }

    @Test
    public void testCreate() throws SQLException {

        var changes = new Changes(dbSource);
        changes.setStartPointNow();

        // do test function
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            var statement = connection.prepareStatement(
                    "insert into work_items (code, name, price, quantity, purchase_date, last_updated_at) "
                            + "values (?, ?, ?, ?, ?, ?)");
            statement.setString(1, "10001");
            statement.setString(2, "ITEM-ADDITONAL-10001");
            statement.setBigDecimal(3, BigDecimal.valueOf(10020L, 2));
            statement.setInt(4, 1024);
            statement.setObject(5, LocalDate.parse("2020-09-17"));
            statement.setObject(6, ZonedDateTime.now().toOffsetDateTime());

            var rowsInserted = statement.executeUpdate();
            System.out.println(rowsInserted + " rows insert.");

            connection.commit();

            statement.setString(1, "10002");
            statement.setString(2, "ITEM-ADDITONAL-10002");
            statement.setBigDecimal(3, BigDecimal.valueOf(10021L, 2));
            statement.setInt(4, 2048);
            statement.setObject(5, LocalDate.parse("2020-09-18"));
            statement.setObject(6, ZonedDateTime.now().toOffsetDateTime());

            var rowsInserted2nd = statement.executeUpdate();
            System.out.println(rowsInserted2nd + " rows insert..");

            connection.rollback();

            statement.close();

        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        changes.setEndPointNow();

        // @formatter:off
        assertThat(changes)
            .hasNumberOfChanges(1)
            .change()
                .isOnTable("work_items")
                .isCreation()
                .hasPksValues(11)
                .rowAtStartPoint()
                    .doesNotExist()
                .rowAtEndPoint()
                    .exists()
                    .value("code").isEqualTo("10001")
                    .value("name").isEqualTo("ITEM-ADDITONAL-10001")
                    .value("price").isEqualTo(BigDecimal.valueOf(10020L, 2))
                    .value("quantity").isEqualTo(1024)
                    .value("purchase_date").isEqualTo(DateValue.from(LocalDate.parse("2020-09-17")))
                    .value("last_updated_at").isDateTime().isNotNull()
                    ;
        // @formatter:on
    }

    @Test
    public void testUpdate() throws SQLException, NamingException {
        var changes = new Changes(dbSource);
        changes.setStartPointNow();

        // do test function
        Connection connection = null;
        try {
            connection = getDataSource().getConnection();
            connection.setAutoCommit(false);

            var statement = connection.prepareStatement(
                    "update work_items set name = ?, price = ?, quantity = ?, purchase_date = ?, last_updated_at = ? "
                            + "where id = ?");
            statement.setString(1, "ITEM-MODIFY-1090#");
            statement.setBigDecimal(2, BigDecimal.valueOf(10030L, 2));
            statement.setInt(3, 2048);
            statement.setObject(4, LocalDate.parse("2020-09-01"));
            statement.setObject(5, ZonedDateTime.now().toOffsetDateTime());

            statement.setInt(6, 10);

            var rowsUpdated = statement.executeUpdate();
            System.out.println(rowsUpdated + " rows updated");

            connection.commit();
            statement.close();

        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        changes.setEndPointNow();

        // @formatter:off
        assertThat(changes)
            .hasNumberOfChanges(1)
            .change()
                .isOnTable("work_items")
                .isModification()
                .hasPksValues(10)
                .column("id").isNotModified()
                .column("code").isNotModified()
                .column("name")
                    .valueAtStartPoint()
                        .isEqualTo("ITEM-01090")
                    .valueAtEndPoint()
                        .isEqualTo("ITEM-MODIFY-1090#")
                .column("price")
                    .valueAtStartPoint()
                        .isEqualTo(BigDecimal.valueOf(109000L, 2))
                    .valueAtEndPoint()
                        .isEqualTo(BigDecimal.valueOf(10030L, 2))
                .column("purchase_date")
                    .valueAtStartPoint()
                        .isEqualTo(DateValue.from(LocalDate.parse("2020-09-10")))
                    .valueAtEndPoint()
                        .isEqualTo(DateValue.from(LocalDate.parse("2020-09-01")))
                .column("last_updated_at").isModified()
                ;
        // @formatter:on
    }

    @Test
    public void testGet() throws SQLException, NamingException {
        dbSetupTracker.skipNextLaunch();

        Connection connection = null;
        WorkItem item = null;
        try {
            connection = getDataSource().getConnection();
            connection.setAutoCommit(false);

            var statement = connection.prepareStatement(
                    "select id, code, name, price, quantity, purchase_date, last_updated_at"
                            + " from work_items where id = ?");
            statement.setInt(1, 10);

            ResultSet result = statement.executeQuery();
            while (result.next()) {
                item = WorkItem.builder().id(result.getInt("id"))
                        .code(result.getString("code"))
                        .name(result.getString("name"))
                        .price(result.getBigDecimal("price"))
                        .quantity(result.getInt("quantity"))
                        .purchaseDate(result.getObject("purchase_date",
                                LocalDate.class))
                        .lastUpdatedAt(JdbcTests.toZonedDateTimeSafe(
                                result.getObject("last_updated_at",
                                        OffsetDateTime.class),
                                ZoneId.of("Asia/Tokyo")))
                        .build();

                System.out.println(item.toString());
            }
            result.close();
            statement.close();

        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        Assertions.assertNotNull(item);
        Assertions.assertEquals(10, item.getId());
        Assertions.assertEquals("01090", item.getCode());
        Assertions.assertEquals("ITEM-01090", item.getName());
        Assertions.assertEquals(BigDecimal.valueOf(109000L, 2),
                item.getPrice());
        Assertions.assertEquals(19, item.getQuantity());
        Assertions.assertEquals(LocalDate.parse("2020-09-10"),
                item.getPurchaseDate());

    }

    @Test
    public void testList() throws SQLException, NamingException {
        dbSetupTracker.skipNextLaunch();

        List<WorkItem> resultList = new ArrayList<>();

        Connection connection = null;
        try {
            connection = getDataSource().getConnection();
            connection.setAutoCommit(false);

            Statement statement = connection.createStatement();
            String sql = "select code, name, price, quantity, purchase_date, last_updated_at"
                    + " from work_items order by quantity desc";
            ResultSet result = statement.executeQuery(sql);
            while (result.next()) {
                WorkItem item = WorkItem.builder()
                        .code(result.getString("code"))
                        .name(result.getString("name"))
                        .price(result.getBigDecimal("price"))
                        .quantity(result.getInt("quantity"))
                        .purchaseDate(result.getObject("purchase_date",
                                LocalDate.class))
                        .lastUpdatedAt(JdbcTests.toZonedDateTimeSafe(
                                result.getObject("last_updated_at",
                                        OffsetDateTime.class),
                                ZoneId.of("Asia/Tokyo")))
                        .build();

                System.out.println(item.toString());
                resultList.add(item);
            }
            result.close();
            statement.close();

        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        Assertions.assertFalse(resultList.isEmpty());
        Assertions.assertEquals(10, resultList.size());
    }

    @Test
    public void testDelete() throws SQLException, NamingException {

        var changes = new Changes(dbSource);
        changes.setStartPointNow();

        // do test function
        Connection connection = null;
        try {
            connection = getDataSource().getConnection();
            connection.setAutoCommit(false);

            var statement = connection
                    .prepareStatement("DELETE from work_items WHERE id = ?");
            statement.setInt(1, 10);

            var rowsDeleted = statement.executeUpdate();
            System.out.println(rowsDeleted + " rows deleted");

            connection.commit();
            statement.close();

        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        changes.setEndPointNow();

        // @formatter:off
        assertThat(changes)
            .hasNumberOfChanges(1)
            .change()
                .isOnTable("work_items")
                .isDeletion()
                .hasPksValues(10)
                .rowAtStartPoint()
                    .exists()
                .rowAtEndPoint()
                    .doesNotExist()
                ;
        // @formatter:on

    }

    @Test
    public void testBatch() throws SQLException, NamingException {

        var changes = new Changes(dbSource);
        changes.setStartPointNow();

        // do test function
        Connection connection = null;
        try {
            connection = getDataSource().getConnection();
            connection.setAutoCommit(false);
            // @formatter:off
            // | Isolation Level  | lost update | Dirty reads | Non-repeatable reads | Phantom reads |
            // |------------------|-------------|-------------|----------------------|---------------|
            // | READ UNCOMMITTED | ×          | ○          | ○                   | ○            |
            // | READ COMMITTED   | ×          | ×          | ○                   | ○            |
            // | REPEATABLE READ  | ×          | ×          | ×                   | ○            |
            // | SERIALIZABLE     | ×          | ×          | ×                   | ×            |
            // @formatter:on
            connection.setTransactionIsolation(
                    Connection.TRANSACTION_SERIALIZABLE);

            var statement = connection.prepareStatement(
                    "insert into work_items (code, name, price, quantity, purchase_date, last_updated_at) "
                            + "values (?, ?, ?, ?, ?, ?)");
            statement.setString(1, "10101");
            statement.setString(2, "ITEM-ADDITONAL-10101");
            statement.setBigDecimal(3, BigDecimal.valueOf(10101L, 2));
            statement.setInt(4, 1024);
            statement.setObject(5, LocalDate.parse("2020-09-20"));
            statement.setObject(6, ZonedDateTime.now().toOffsetDateTime());
            statement.addBatch();

            statement.setString(1, "10102");
            statement.setString(2, "ITEM-ADDITONAL-10102");
            statement.setBigDecimal(3, BigDecimal.valueOf(10102L, 2));
            statement.setInt(4, 2048);
            statement.setObject(5, LocalDate.parse("2020-09-21"));
            statement.setObject(6, ZonedDateTime.now().toOffsetDateTime());
            statement.addBatch();

            var rowsChanged = statement.executeBatch();
            System.out.println(rowsChanged + " rows Changed.");

            connection.commit();
            statement.close();

        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        changes.setEndPointNow();

        // @formatter:off
        assertThat(changes)
            .hasNumberOfChanges(2)
            .changeOfCreation(1)
                .isOnTable("work_items")
                .hasPksValues(12)
                .rowAtStartPoint()
                    .doesNotExist()
                .rowAtEndPoint()
                    .exists()
            .changeOfCreation(0)
                .isOnTable("work_items")
                .hasPksValues(11)
                .rowAtStartPoint()
                    .doesNotExist()
                .rowAtEndPoint()
                    .exists()
                    ;
        // @formatter:on

    }

    @Test
    public void testCLOB() throws SQLException, NamingException, IOException {

        var changes = new Changes(dbSource);
        changes.setStartPointNow();

        // do test function
        Connection connection = null;
        try {
            connection = getDataSource().getConnection();
            connection.setAutoCommit(false);

            ClassLoader classLoader = getClass().getClassLoader();
            Path filePath = Paths
                    .get(classLoader.getResource("two-column.csv").getFile());
            var fileSize = Files.size(filePath);
            System.out.println("> size = " + Long.toString(fileSize));

            var statement = connection.prepareStatement(
                    "update work_items set clob_data = ? where id = ?");
            try (var reader = Files.newBufferedReader(filePath)) {
                statement.setCharacterStream(1, reader);
            }
            statement.setInt(2, 2);

            var rowsChanged = statement.executeUpdate();
            System.out.println(rowsChanged + " rows Changed.");

            connection.commit();
            statement.close();

            var statement2 = connection.prepareStatement(
                    "select clob_data from work_items where id = ?");
            statement2.setInt(1, 2);

            ResultSet result2 = statement2.executeQuery();
            result2.next();

            try (var reader = result2.getCharacterStream(1)) {
                try (var reader2 = new BufferedReader(reader)) {
                    reader2.lines().forEach(text -> {
                        System.out.println("> " + text);
                    });
                }
            }
            result2.close();
            statement2.close();

        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        changes.setEndPointNow();

        // @formatter:off
        assertThat(changes)
            .hasNumberOfChanges(1)
            .changeOfModification()
                .isOnTable("work_items")
                .hasPksValues(2)
                .column("code").isNotModified()
                .column("name").isNotModified()
                .column("price").isNotModified()
                .column("quantity").isNotModified()
                .column("purchase_date").isNotModified()
                .column("last_updated_at").isNotModified()
                .column("clob_data").isModified()
                .column("blob_data").isNotModified()
            ;
        // @formatter:on
    }

    @Test
    public void testBLOB() throws SQLException, NamingException, IOException,
            NoSuchAlgorithmException {

        var changes = new Changes(dbSource);
        changes.setStartPointNow();

        // do test function
        Connection connection = null;
        try {
            connection = getDataSource().getConnection();
            connection.setAutoCommit(false);

            var classLoader = getClass().getClassLoader();
            var filePath = Paths
                    .get(classLoader.getResource("user-trash.png").getFile());
            var fileSize = Files.size(filePath);
            System.out.println("> size = " + Long.toString(fileSize));

            var statement = connection.prepareStatement(
                    "update work_items set blob_data = ? where id = ?");

            try (var stream = Files.newInputStream(filePath)) {
                statement.setBinaryStream(1, stream);
            }
            statement.setInt(2, 3);

            var rowsChanged = statement.executeUpdate();
            System.out.println(rowsChanged + " rows Changed.");

            connection.commit();
            statement.close();

            var statement2 = connection.prepareStatement(
                    "select blob_data from work_items where id = ?");
            statement2.setInt(1, 3);

            ResultSet result2 = statement2.executeQuery();
            result2.next();

            try (var stream = result2.getBinaryStream(1)) {
                var bytes = stream.readAllBytes();
                var sha512 = MessageDigest.getInstance("SHA-512");
                var sha512_result = sha512.digest(bytes);
                System.out.println("> SHA-512 :"
                        + Base64.getEncoder().encodeToString(sha512_result));
            }

            try (var stream = result2.getBinaryStream(1)) {
                var readSize = 1024;
                var buffer = new byte[readSize];
                var readedSize = 0;
                while ((readedSize = stream.read(buffer, 0, readSize)) > 0) {
                    var s = Base64.getEncoder().encodeToString(buffer);
                    System.out.println(">[" + readedSize + "] " + s);
                }
            }

            result2.close();
            statement2.close();

        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        changes.setEndPointNow();

        // @formatter:off
        assertThat(changes)
            .hasNumberOfChanges(1)
            .changeOfModification()
                .isOnTable("work_items")
                .hasPksValues(3)
                .column("code").isNotModified()
                .column("name").isNotModified()
                .column("price").isNotModified()
                .column("quantity").isNotModified()
                .column("purchase_date").isNotModified()
                .column("last_updated_at").isNotModified()
                .column("clob_data").isNotModified()
                .column("blob_data").isModified()
            ;
        // @formatter:on
    }

    @Test
    public void testCallProcedure() throws SQLException, NamingException {
        dbSetupTracker.skipNextLaunch();

        String upperCased = null;
        // do test function
        Connection connection = null;
        try {
            connection = getDataSource().getConnection();
            connection.setAutoCommit(false);

            var upperFunc = connection.prepareCall("{? = call upper( ? ) }");
            upperFunc.registerOutParameter(1, Types.VARCHAR);
            upperFunc.setString(2, "lowercase to uppercase");
            upperFunc.execute();

            upperCased = upperFunc.getString(1);
            upperFunc.close();

        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        assertEquals("LOWERCASE TO UPPERCASE", upperCased);
    }
}
