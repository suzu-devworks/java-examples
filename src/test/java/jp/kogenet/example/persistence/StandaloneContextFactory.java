package jp.kogenet.example.persistence;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public class StandaloneContextFactory implements InitialContextFactory {
    private static final Context CONTEXT = new StandaloneContext();

    @Override
    public Context getInitialContext(Hashtable<?, ?> environment)
            throws NamingException {
        return CONTEXT;
    }

}