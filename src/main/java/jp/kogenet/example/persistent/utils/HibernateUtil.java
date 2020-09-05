package jp.kogenet.example.persistent.utils;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateUtil {

    public static SessionFactory getSessionFactory() {
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                .build();

        SessionFactory factory = null;
        try {
            factory = new MetadataSources(registry).buildMetadata()
                    .buildSessionFactory();
        } catch (Exception e) {
            System.err.println(
                    "Initial SessionFactory creation failed." + e.toString());
            // The registry would be destroyed by the SessionFactory,
            // but we had trouble building the SessionFactory
            // so destroy it manually.
            StandardServiceRegistryBuilder.destroy(registry);
        }

        return factory;
    }

}