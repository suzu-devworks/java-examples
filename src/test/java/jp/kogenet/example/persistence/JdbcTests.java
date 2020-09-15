package jp.kogenet.example.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

public class JdbcTests {

    @BeforeAll
    static void setup() {

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
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "jp.kogenet.example.persistence.StandaloneContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES,
                "jp.kogenet.example.persistence");

        try {
            Context context = new InitialContext();
            context.bind("java:comp/env/jdbc/datasource", source);

        } catch (NamingException e) {
            e.printStackTrace();
        }

    }

    private DataSource getDataSource() {
        DataSource ds = null;
        try {
            Context context = new InitialContext();
            ds = (DataSource) context.lookup("java:comp/env/jdbc/datasource");
        } catch (NamingException e) {
            e.printStackTrace();
            return null;
        }
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

    @Test
    public void test() throws SQLException {

        Connection connection = null;
        try {
            // connection = getDataSource().getConnection();
            connection = getConnection();
            connection.setAutoCommit(false);

            Statement statement = connection.createStatement();
            String sql = "select item_code, item_name, purchase_date from user_items";
            ResultSet result = statement.executeQuery(sql);
            while (result.next()) {
                Integer code = result.getInt("item_code");
                String name = result.getString("item_name");
                LocalDate localDate = result.getObject("purchase_date",
                        LocalDate.class);

                System.out.println("" + code + "\t" + name + "\t" + localDate);
            }
            result.close();
            statement.close();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

    }
}
