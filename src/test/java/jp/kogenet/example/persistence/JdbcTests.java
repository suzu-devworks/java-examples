package jp.kogenet.example.persistence;

import static com.ninja_squad.dbsetup.Operations.insertInto;
import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static com.ninja_squad.dbsetup.Operations.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
                ValueGenerators.dateSequence().startingAt(LocalDate.now()).incrementingBy(1, ChronoUnit.DAYS))
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
    public void testCreate() {
    }


    @Test
    public void testUpdate() {
    }


    @Test
    public void testGet() {
    }


    @Test
    public void testList() throws SQLException {
        dbSetupTracker.skipNextLaunch();

        List<WorkItem> resultList = new ArrayList<>();

        Connection connection = null;
        try {
            // connection = getDataSource().getConnection();
            connection = getConnection();
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
    public void testDelete() {
    }

}
