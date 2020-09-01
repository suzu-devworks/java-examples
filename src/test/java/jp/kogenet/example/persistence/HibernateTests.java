package jp.kogenet.example.persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
    private Session session;

    @BeforeAll
    static void setup() {
        sessionFactory = HibernateUtil.getSessionFactory();
        System.out.println("SessionFactory created");
    }

    @AfterAll
    static void tearDown() {
        if (sessionFactory != null)
            sessionFactory.close();
        System.out.println("SessionFactory destroyed");
    }

    @BeforeEach
    public void openSession() {
        session = sessionFactory.openSession();
        System.out.println("Session created");
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

        session.beginTransaction();

        User user = new User(null, "Alice", "alice@example.local", "password for alica.");
        Integer id = (Integer) session.save(user);

        session.getTransaction().commit();

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