package jp.kogenet.example.persistence;

import static com.ninja_squad.dbsetup.Operations.insertInto;
import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static com.ninja_squad.dbsetup.Operations.sql;
import static org.assertj.db.api.Assertions.assertThat;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.destination.DriverManagerDestination;
import com.ninja_squad.dbsetup.operation.Operation;

import org.assertj.db.type.Changes;
import org.assertj.db.type.Source;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jp.kogenet.example.persistent.entities.User;
import jp.kogenet.example.persistent.utils.HibernateUtil;

public class HibernateTests {

    private static SessionFactory sessionFactory;
    private static DbSetupTracker dbSetupTracker = new DbSetupTracker();
    private static DriverManagerDestination destination;
    private static Source dbSource;
    private Session session;

    @BeforeAll
    static void setup() {

        sessionFactory = HibernateUtil.getSessionFactory();
        System.out.println("SessionFactory created");

        Configuration configure = new Configuration().configure();
        String url = configure.getProperty(AvailableSettings.URL);
        String user = configure.getProperty(AvailableSettings.USER);
        String pass = configure.getProperty(AvailableSettings.PASS);

        destination = new DriverManagerDestination(url, user, pass);
        dbSource = new Source(url, user, pass);
    }

    @AfterAll
    static void tearDown() {
        if (sessionFactory != null)
            sessionFactory.close();
        System.out.println("SessionFactory destroyed");

    }

    private static final Operation TRUCATE_ALL = sequenceOf(
            // truncate("users", "user_items","user_status"),
            sql("TRUNCATE TABLE users RESTART IDENTITY"),
            sql("TRUNCATE TABLE user_items RESTART IDENTITY"),
            sql("TRUNCATE TABLE user_status RESTART IDENTITY"));

    // @formatter:off
    private static final Operation INSERT_REFERENCE_DATA = sequenceOf(
            insertInto("users")
                    .columns("user_name", "email", "password")
                    .values("Alice", "alice@example.local","password for alica.")
                    .values("Bob", "bob@example.local","password for bob.")
                    .values("Carol", "carol@example.local","password for carol.")
                    .build()
            );
    // @formatter:on

    @BeforeEach
    public void openSession() {
        session = sessionFactory.openSession();
        System.out.println("Session created");

        Operation operation = sequenceOf(HibernateTests.TRUCATE_ALL,
                HibernateTests.INSERT_REFERENCE_DATA);
        DbSetup dbSetup = new DbSetup(destination, operation);

        // new DataSourceDestination(dataSource),
        // or without DataSource:
        // DbSetup dbSetup = new DbSetup(new
        // DriverManagerDestination(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD)
        // , operation);

        dbSetupTracker.launchIfNecessary(dbSetup);
        // If you want to skip creating test data,
        // call dbSetupTracker.skipNextLaunch() in test case.

    }

    @AfterEach
    public void closeSession() {
        if (session != null)
            session.close();
        System.out.println("Session closed\n");
    }

    @Test
    public void testCreate() {
        System.out.println("Running testCreate...");

        Changes changes = new Changes(dbSource);
        changes.setStartPointNow();

        session.beginTransaction();

        User user = new User(null, "Dave", "dave@example.local",
                "password for dave.");
        Integer id = (Integer) session.save(user);

        session.getTransaction().commit();

        changes.setEndPointNow();

        // @formatter:off
        assertThat(changes)
            .hasNumberOfChanges(1)
            .change()
                .isCreation()
                .rowAtEndPoint()
                    .hasValues(4, "Dave", "dave@example.local",
                        "password for dave.                                              ");
        // @formatter:on
        Assertions.assertTrue(id > 0);
    }

    @Test
    public void testUpdate() {
    }

    @Test
    public void testGet() {
    }

    @Test
    public void testList() {
    }

    @Test
    public void testDelete() {
    }

}