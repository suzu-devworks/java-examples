package jp.kogenet.example.persistence;

import static com.ninja_squad.dbsetup.Operations.insertInto;
import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static com.ninja_squad.dbsetup.Operations.sql;
import static org.assertj.db.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.destination.DriverManagerDestination;
import com.ninja_squad.dbsetup.generator.ValueGenerators;
import com.ninja_squad.dbsetup.operation.Operation;

import org.assertj.db.type.Changes;
import org.assertj.db.type.Source;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jp.kogenet.example.persistence.entities.User;
import jp.kogenet.example.persistence.utils.HibernateUtil;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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

    // @formatter:off
    private static final Operation INSERT_USER_ITEMS_DATA = sequenceOf(
        insertInto("user_items")
            .withGeneratedValue("item_code", ValueGenerators.sequence().startingAt(1000L).incrementingBy(10))
            .withGeneratedValue("item_name",
                    ValueGenerators.stringSequence("ITEM-").startingAt(1000L).incrementingBy(10).withLeftPadding(6))
            .withGeneratedValue("purchase_date",
                    ValueGenerators.dateSequence().startingAt(LocalDate.now()).incrementingBy(1, ChronoUnit.DAYS))
            .withDefaultValue("last_updated_at", ZonedDateTime.now())
            .columns("user_id")
                .repeatingValues(1)
                .times(100)
            .build());
    // @formatter:on

    @BeforeEach
    void preparation() {
        session = sessionFactory.openSession();
        System.out.println("Session created");

        Operation operation = sequenceOf(HibernateTests.TRUCATE_ALL,
                HibernateTests.INSERT_REFERENCE_DATA,
                HibernateTests.INSERT_USER_ITEMS_DATA);
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
    void cleanUp() {
        if (session != null)
            session.close();
        System.out.println("Session closed\n");
    }

    @Test
    public void testCreate() {
        System.out.println("Running testCreate...");

        Changes changes = new Changes(dbSource);
        changes.setStartPointNow();

        // do test function
        session.beginTransaction();
        User user = new User(null, "Dave", "dave@example.local",
                "password for dave.");
        Integer id = (Integer) session.save(user);
        session.getTransaction().commit();

        changes.setEndPointNow();

        Assertions.assertTrue(id > 0);
        // @formatter:off
        assertThat(changes)
            .hasNumberOfChanges(1)
            .change()
                .isOnTable("users")
                .isCreation()
                .hasPksValues(4)
                .rowAtStartPoint()
                    .doesNotExist()
                .rowAtEndPoint()
                    .exists()
                    .hasValues(4, "Dave", "dave@example.local",
                    "password for dave.                                              ");
        // @formatter:on

    }

    @Test
    public void testUpdate() {
        System.out.println("Running testUpdate...");

        Integer id = 3;
        User user = session.find(User.class, id);

        user.setEmail("charlie@example.local");
        user.setPassword("updated password for charlie.");

        Changes changes = new Changes(dbSource);
        changes.setStartPointNow();

        session.beginTransaction();
        // It has been updated even with commit alone.
        session.update(user);
        // session.save(user);
        // session.saveOrUpdate(user);
        session.getTransaction().commit();

        changes.setEndPointNow();

        User updatedUser = session.find(User.class, id);

        Assertions.assertNotNull(updatedUser);
        Assertions.assertEquals(user.getName(), updatedUser.getName());
        Assertions.assertEquals(user.getEmail(), updatedUser.getEmail());
        Assertions.assertEquals(user.getPassword(), updatedUser.getPassword());

        // @formatter:off
        assertThat(changes)
            .hasNumberOfChanges(1)
            .change()
                .isOnTable("users")
                .isModification()
                .hasPksValues(id)
                .column("id").isNotModified()
                .column("user_name").isNotModified()
                .column("email")
                    .valueAtStartPoint()
                        .isEqualTo("carol@example.local")
                    .valueAtEndPoint()
                        .isEqualTo("charlie@example.local")
                .column("password")
                    .valueAtStartPoint()
                        .isEqualTo("password for carol.                                             ")
                    .valueAtEndPoint()
                        .isEqualTo("updated password for charlie.                                   ")
                ;
        // @formatter:on
    }

    @Test
    public void testGet() {
        System.out.println("Running testGet...");
        dbSetupTracker.skipNextLaunch();

        Changes changes = new Changes(dbSource);
        changes.setStartPointNow();

        Integer id = 1;
        User user = session.find(User.class, id);

        changes.setEndPointNow();

        // jupiter
        Assertions.assertEquals("Alice", user.getName());
        Assertions.assertEquals("alice@example.local", user.getEmail());
        Assertions.assertEquals(
                "password for alica.                                             ",
                user.getPassword());

        // hamcrest
        assertThat(user.getName(), is(equalTo("Alice")));
        assertThat(user.getEmail(), is(equalTo("alice@example.local")));
        assertThat(user.getPassword(), is(equalTo(
                "password for alica.                                             ")));

        // asseretJ
        assertThat(user.getName()).isEqualTo("Alice");
        assertThat(user.getEmail()).isEqualTo("alice@example.local");
        assertThat(user.getPassword()).isEqualTo(
                "password for alica.                                             ");

        assertThat(changes).hasNumberOfChanges(0);

    }

    @Test
    public void testList() {
        System.out.println("Running testList...");
        dbSetupTracker.skipNextLaunch();

        Query<User> query = session.createQuery("from User", User.class);
        List<User> resultList = query.getResultList();

        Assertions.assertFalse(resultList.isEmpty());
        Assertions.assertEquals(3, resultList.size());
        
     }

    @Test
    public void testDelete() {
        System.out.println("Running testDelete...");

        Integer id = 2;
        User user = session.find(User.class, id);

        Changes changes = new Changes(dbSource);
        changes.setStartPointNow();

        // do test function
        session.beginTransaction();
        session.delete(user);
        session.getTransaction().commit();

        User deletedUser = session.find(User.class, id);

        changes.setEndPointNow();

        Assertions.assertNull(deletedUser);

        // @formatter:off
        assertThat(changes)
            .hasNumberOfChanges(1)
            .change()
                .isOnTable("users")
                .isDeletion()
                .hasPksValues(id)
                .rowAtStartPoint()
                    .exists()
                .rowAtEndPoint()
                    .doesNotExist();
        // @formatter:on
    }

}