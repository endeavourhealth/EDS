package org.endeavourhealth.core.mySQLDatabase;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;

public enum PersistenceManager {
    INSTANCE;

    private EntityManagerFactory emFactory;

    private PersistenceManager() {
        emFactory = Persistence.createEntityManagerFactory("newPersistenceUnit", new HashMap());
    }

    public EntityManager getEntityManager() {
        return emFactory.createEntityManager();
    }

    public void close() {
        emFactory.close();
    }
}
