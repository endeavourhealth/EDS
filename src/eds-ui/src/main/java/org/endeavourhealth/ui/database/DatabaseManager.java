package org.endeavourhealth.ui.database;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.db.DBAppender;
import ch.qos.logback.classic.db.names.DefaultDBNameResolver;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.db.ConnectionSource;
import ch.qos.logback.core.db.dialect.SQLDialectCode;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.endeavourhealth.core.data.admin.models.DefinitionItemType;
import org.endeavourhealth.ui.DependencyType;
import org.endeavourhealth.ui.ExecutionStatus;
import org.endeavourhealth.ui.database.administration.*;
import org.endeavourhealth.ui.database.definition.DbActiveItem;
import org.endeavourhealth.ui.database.definition.DbAudit;
import org.endeavourhealth.ui.database.definition.DbItem;
import org.endeavourhealth.ui.database.definition.DbItemDependency;
import org.endeavourhealth.ui.database.lookups.DbSourceOrganisation;
import org.endeavourhealth.ui.database.lookups.DbSourceOrganisationSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class DatabaseManager {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String LOGGING_SCHEMA_PREFIX = "Logging.";
    private static final String ASYNC_APPENDER = "ASYNC";

    //singleton
    private static DatabaseManager ourInstance = new DatabaseManager();
    public static DatabaseManager getInstance() {
        return ourInstance;
    }

    private DatabaseI databaseImplementation = null;
    private ComboPooledDataSource cpds = null;


    public void setConnectionProperties(String url, String username, String password) {

        //this would be where we plug in support for different databases
        this.databaseImplementation = new SqlServerDatabase();

        try {

            //need to force the loading of the Driver class before we try to create any connections
            Class.forName(net.sourceforge.jtds.jdbc.Driver.class.getCanonicalName());

            cpds = new ComboPooledDataSource();
            cpds.setDriverClass("net.sourceforge.jtds.jdbc.Driver");
            cpds.setJdbcUrl(url);
            cpds.setUser(username);
            cpds.setPassword(password);

            //arbitrary pool settings
            cpds.setInitialPoolSize(5);
            cpds.setMinPoolSize(5);
            cpds.setAcquireIncrement(5);
            cpds.setMaxPoolSize(20);
            cpds.setMaxStatements(180);
            cpds.setMaxIdleTime(300); //if a connection is idle for 5 mins, discard it

            LOG.info("Database connection pool set up during server startup");

        } catch (ClassNotFoundException | PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws ClassNotFoundException, SQLException {

        //no connection pooling option
        //return DriverManager.getConnection(getInstance().url, getInstance().username, getInstance().password);

        Connection conn = getInstance().cpds.getConnection();

        //occasional problems getting a connection that's already closed, so try this quick check
        if (conn.isClosed()) {
            return getConnection();
        }

        conn.setAutoCommit(false); //never want auto-commit
        return conn;
    }

    public static void closeConnection(Connection connection) throws SQLException {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOG.error("Error closing connection", e);
            }
        }
    }


    public static DatabaseI db() {
        return getInstance().databaseImplementation;
    }

    public void registerLogbackDbAppender() {

        //we need our own implementation of a conneciton source, because logback fails to detect the DB type when against Azure
        LogbackConnectionSource connectionSource = new LogbackConnectionSource();
        LogbackDbNameResolver nameResolver = new LogbackDbNameResolver();

        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        DBAppender dbAppender = new DBAppender();
        dbAppender.setContext(rootLogger.getLoggerContext());
        dbAppender.setConnectionSource(connectionSource);
        dbAppender.setName("DB Appender");
        dbAppender.setDbNameResolver(nameResolver);
        dbAppender.start();

        //use an async appender so logging to DB doesn't block
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(rootLogger.getLoggerContext());
        asyncAppender.setName(ASYNC_APPENDER);
        //    // excluding caller data (used for stack traces) improves appender's performance
        //    asyncAppender.setIncludeCallerData(false);
        //    // set threshold to 0 to disable discarding and keep all events
        //    asyncAppender.setDiscardingThreshold(0);
        //    asyncAppender.setQueueSize(256);
        asyncAppender.addAppender(dbAppender);
        asyncAppender.start();



        rootLogger.addAppender(asyncAppender);
    }

    public void deregisterLogbackDbAppender() {

        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        Appender appender = rootLogger.getAppender(ASYNC_APPENDER);
        if (appender != null) {
            appender.stop();
        }
    }

    public void sqlTest() throws Exception {

        List<UUID> uuidList = new ArrayList<>();
        uuidList.add(UUID.randomUUID());
        uuidList.add(UUID.randomUUID());
        uuidList.add(UUID.randomUUID());

        db().retrieveEndUserForEmail("Email");
        db().retrieveEndUserForEmail("Ema'il");

        db().retrieveSuperUsers();

        db().retrieveEndUserPwdForUserNotExpired(UUID.randomUUID());

        db().retrieveEndUserEmailInviteForUserNotCompleted(UUID.randomUUID());

        db().retrieveEndUserEmailInviteForToken("Token");

        db().retrieveOrganisationEndUserLinksForOrganisationNotExpired(UUID.randomUUID());

        db().retrieveOrganisationEndUserLinksForUserNotExpired(UUID.randomUUID());

        db().retrieveOrganisationEndUserLinksForOrganisationEndUserNotExpired(UUID.randomUUID(), UUID.randomUUID());

        db().retrieveLatestItemForUuid(UUID.randomUUID());

        db().retrieveDependentItems(UUID.randomUUID(), DependencyType.IsContainedWithin);

        db().retrieveNonDependentItems(UUID.randomUUID(), DependencyType.IsChildOf, DefinitionItemType.ReportFolder);

        List<DbActiveItem> activeItems = new ArrayList<>();
        DbActiveItem a = new DbActiveItem();
        a.setItemUuid(UUID.randomUUID());
        a.setAuditUuid(UUID.randomUUID());
        activeItems.add(a);
        a = new DbActiveItem();
        a.setItemUuid(UUID.randomUUID());
        a.setAuditUuid(UUID.randomUUID());
        activeItems.add(a);
        db().retrieveItemsForActiveItems(activeItems);

        db().retrieveActiveItemForItemUuid(UUID.randomUUID());

        db().retrieveActiveItemDependentItems(UUID.randomUUID(), UUID.randomUUID(), DependencyType.Uses);

        db().retrieveActiveItemRecentItems(UUID.randomUUID(), UUID.randomUUID(), 5);

        db().retrieveCountDependencies(UUID.randomUUID(), DependencyType.IsChildOf);

        db().retrieveItemDependenciesForItem(UUID.randomUUID(), UUID.randomUUID());

        db().retrieveItemDependenciesForItemType(UUID.randomUUID(), UUID.randomUUID(), DependencyType.Uses);

        db().retrieveItemDependenciesForDependentItem(UUID.randomUUID());

        db().retrieveItemDependenciesForDependentItemType(UUID.randomUUID(), DependencyType.Uses);

        db().retrieveAuditsForUuids(uuidList);

        db().retrieveLatestAudit();

        List<DbAbstractTable> entities = new ArrayList<>();

        DbSourceOrganisation sourceOrganisation = new DbSourceOrganisation();
        sourceOrganisation.setName("Name");
        sourceOrganisation.setOdsCode("OdsCode");
        sourceOrganisation.setReferencedByData(true);
        entities.add(sourceOrganisation);

        UUID orgUuid = UUID.randomUUID();

        DbEndUser user = new DbEndUser();
        user.assignPrimaryUUid();
        user.setEmail("Email");
        user.setForename("Forename");
        user.setSuperUser(false);
        user.setSurname("Surname");
        user.setTitle("Title");
        UUID userUuid = user.getEndUserUuid();
        entities.add(user);

        DbEndUserEmailInvite invite = new DbEndUserEmailInvite();
        invite.assignPrimaryUUid();
        invite.setDtCompleted(null);
        invite.setEndUserUuid(userUuid);
        invite.setUniqueToken("Token");
        entities.add(invite);

        DbEndUserPwd pwd = new DbEndUserPwd();
        pwd.assignPrimaryUUid();
        pwd.setDtExpired(null);
        pwd.setFailedAttempts(0);
        pwd.setOneTimeUse(false);
        pwd.setEndUserUuid(userUuid);
        pwd.setPwdHash("PwdHash");
        entities.add(pwd);

        DbOrganisationEndUserLink organisationEndUserLink = new DbOrganisationEndUserLink();
        organisationEndUserLink.assignPrimaryUUid();
        organisationEndUserLink.setAdmin(false);
        organisationEndUserLink.setEndUserUuid(userUuid);
        organisationEndUserLink.setDtExpired(null);
        organisationEndUserLink.setOrganisationUuid(orgUuid);
        entities.add(organisationEndUserLink);

        DbSourceOrganisationSet set = new DbSourceOrganisationSet();
        set.assignPrimaryUUid();
        set.setOrganisationUuid(orgUuid);
        set.setName("Name");
        set.setOdsCodes("OdsCodes");
        entities.add(set);

        DbAudit audit = new DbAudit();
        audit.assignPrimaryUUid();
        audit.setOrganisationUuid(orgUuid);
        audit.setTimeStamp(Instant.now());
        audit.setEndUserUuid(userUuid);
        UUID auditUuid = audit.getAuditUuid();
        entities.add(audit);

        DbItem item = new DbItem();
        item.assignPrimaryUUid();
        item.setAuditUuid(auditUuid);
        item.setTitle("Title");
        item.setDescription("Description");
        item.setXmlContent("XmlContent");
        UUID itemUuid = item.getItemUuid();
        entities.add(item);

        DbActiveItem activeItem = new DbActiveItem();
        activeItem.assignPrimaryUUid();
        activeItem.setAuditUuid(auditUuid);
        activeItem.setItemUuid(itemUuid);
        activeItem.setDeleted(false);
        activeItem.setItemTypeId(DefinitionItemType.ReportFolder);
        activeItem.setOrganisationUuid(orgUuid);
        entities.add(activeItem);

        DbItemDependency itemDependency = new DbItemDependency();
        itemDependency.assignPrimaryUUid();
        itemDependency.setAuditUuid(auditUuid);
        itemDependency.setDependencyTypeId(DependencyType.Uses);
        itemDependency.setDependentItemUuid(itemUuid);
        itemDependency.setItemUuid(itemUuid);
        entities.add(itemDependency);

        //now insert the new entities
        for (DbAbstractTable entity: entities) {
            entity.setSaveMode(TableSaveMode.INSERT);
            LOG.debug("INSERT " + entity.getClass());
            entity.writeToDb();
            LOG.debug("ok");
        }

        //now we've tested inserting, test an update to each item
        for (DbAbstractTable entity: entities) {
            LOG.debug("UPDATE " + entity.getClass());
            entity.setSaveMode(TableSaveMode.UPDATE);
            entity.writeToDb();
            LOG.debug("ok");
        }

        //now we've tested inserting and updating, we should test a delete
        Collections.reverse(entities); //reverse, so the FK dependencies don't cause problems

        for (DbAbstractTable entity: entities) {
            LOG.debug("DELETE " + entity.getClass());
            entity.setSaveMode(TableSaveMode.DELETE);
            entity.writeToDb();
            LOG.debug("ok");
        }


    }

    /**
     * because the three logging tables are in a schema, we need to override the resolver to insert the schema name
     */
    class LogbackDbNameResolver extends DefaultDBNameResolver {
        @Override
        public <N extends Enum<?>> String getTableName(N tableName) {
            return LOGGING_SCHEMA_PREFIX + super.getTableName(tableName);
        }
    }

    /**
     * Connection source implementation for LogBack, as it seems unable to correctly work out it should use SQL Server dialect
     */
    class LogbackConnectionSource implements ConnectionSource {

        public LogbackConnectionSource() {}

        @Override
        public Connection getConnection() throws SQLException {
            return cpds.getConnection();
        }

        @Override
        public SQLDialectCode getSQLDialectCode() {
            return databaseImplementation.getLogbackDbDialectCode();
        }

        @Override
        public boolean supportsGetGeneratedKeys() {
            return false;
        }

        @Override
        public boolean supportsBatchUpdates() {
            return false;
        }

        @Override
        public void start() {}

        @Override
        public void stop() {}

        @Override
        public boolean isStarted() {
            return true;
        }
    }
}

