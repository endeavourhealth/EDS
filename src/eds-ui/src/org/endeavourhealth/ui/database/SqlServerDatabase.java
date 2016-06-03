package org.endeavourhealth.ui.database;

import ch.qos.logback.core.db.dialect.SQLDialectCode;
import org.endeavourhealth.core.data.admin.models.DefinitionItemType;
import org.endeavourhealth.ui.DependencyType;
import org.endeavourhealth.ui.ExecutionStatus;
import org.endeavourhealth.ui.ProcessorState;
import org.endeavourhealth.ui.database.administration.*;
import org.endeavourhealth.ui.database.definition.DbActiveItem;
import org.endeavourhealth.ui.database.definition.DbAudit;
import org.endeavourhealth.ui.database.definition.DbItem;
import org.endeavourhealth.ui.database.definition.DbItemDependency;
import org.endeavourhealth.ui.database.execution.*;
import org.endeavourhealth.ui.database.lookups.DbSourceOrganisation;
import org.endeavourhealth.ui.database.lookups.DbSourceOrganisationSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.*;
import java.time.Instant;
import java.util.*;

/**
 * Database implementation for SQL Server. To support other DB types, create a new sub-class of DatabaseI
 */
final class SqlServerDatabase implements DatabaseI {
    private static final Logger LOG = LoggerFactory.getLogger(SqlServerDatabase.class);
    private static final String ALIAS = "z";

    private HashMap<TableAdapter, String> parameterisedInsertSql = new HashMap<>();
    private HashMap<TableAdapter, String> parameterisedUpdateSql = new HashMap<>();
    private HashMap<TableAdapter, String> parameterisedDeleteSql = new HashMap<>();

    private int executeScalarQuery(String sql, Object... parameters) throws Exception {
        LOG.trace("Executing {}", sql);

        Connection connection = DatabaseManager.getConnection();
        PreparedStatement s = connection.prepareStatement(sql);
        appendPreparedStatementParameters(s, parameters);

        try {
            s.execute();

            ResultSet rs = s.getResultSet();
            rs.next();
            int ret = rs.getInt(1);

            rs.close();

            return ret;
        } catch (SQLException sqlEx) {
            LOG.error("Error with SQL {}", sql);
            throw sqlEx;
        } finally {
            DatabaseManager.closeConnection(connection);
        }
    }

    private void executeQueryNoResult(String sql, Object... parameters) throws Exception {
        LOG.trace("Executing {}", sql);

        Connection connection = DatabaseManager.getConnection();
        PreparedStatement s = connection.prepareStatement(sql);
        appendPreparedStatementParameters(s, parameters);

        try {
            s.execute();
            connection.commit();

        } catch (SQLException sqlEx) {
            LOG.error("Error with SQL {}", sql);
            throw sqlEx;
        } finally {
            DatabaseManager.closeConnection(connection);
        }
    }


    @Override
    public SQLDialectCode getLogbackDbDialectCode() {
        return SQLDialectCode.MSSQL_DIALECT;
    }

    @Override
    public <T extends DbAbstractTable> void writeEntity(T entity) throws Exception {
        List<T> v = new ArrayList<>();
        v.add(entity);
        writeEntities(v);
    }

    @Override
    public <T extends DbAbstractTable> void writeEntities(List<T> entities) throws Exception {
        if (entities.isEmpty()) {
            return;
        }

        LOG.trace("Writing {} entities to DB", entities.size());

        StringJoiner sqlLogging = new StringJoiner("\r\n");
        List<T> copy = new ArrayList<>(entities);

        Connection connection = DatabaseManager.getConnection();
        try {
            while (!copy.isEmpty()) {

                T first = copy.remove(0);

                String sql = getParameterisedWriteSql(first);
                LOG.trace("Executing {}", sql);

                sqlLogging.add(sql);
                PreparedStatement s = connection.prepareStatement(sql);
                appendWriteParameters(first, s);
                s.addBatch();

                //for greater efficiency, we can re-order the writes so similar items
                //are grouped together. So look for any other instances of the same class in the same write mode
                List<T> similar = new ArrayList<>();
                for (T other: copy) {
                    if (other.getClass() == first.getClass()
                        && other.getSaveMode() == first.getSaveMode()) {
                        similar.add(other);
                    }
                }

                copy.removeAll(similar);
                for (T other: similar) {
                    appendWriteParameters(other, s);
                    s.addBatch();
                }

                s.executeBatch();
            }

            LOG.trace("Executing {}", sqlLogging.toString());

            connection.commit();

        } catch (SQLException sqlEx) {
            LOG.error("Error in SQL {}", sqlLogging.toString());
            connection.rollback();
            throw sqlEx;
        } finally {
            DatabaseManager.closeConnection(connection);
        }
    }
    private <T extends DbAbstractTable> void appendWriteParameters(T entity, PreparedStatement s) throws Exception {

        HashMap<Field, Object> parameters = new HashMap<>();
        TableAdapter adapter = entity.getAdapter();
        adapter.writeForDb(entity, parameters);

        switch (entity.getSaveMode()) {
            case INSERT:
                appendInsertParameters(adapter, parameters, s);
                break;
            case UPDATE:
                appendUpdateParameters(adapter, parameters, s);
                break;
            case DELETE:
                appendDeleteParameters(adapter, parameters, s);
                break;
            default:
                throw new RuntimeException("Invalid save mode " + entity.getSaveMode());
        }
    }
    private void appendInsertParameters(TableAdapter adapter, HashMap<Field, Object> parameters, PreparedStatement s) throws Exception {

        //for insert statements we want to write out all non-identity values
        List<Field> fields = adapter.getColumnFields();
        int index = 1;
        for (Field field: fields) {

            //skip identity columns, as SQL Server assigns their values
            if (field.isAnnotationPresent(IdentityColumn.class)) {
                continue;
            }

            Object obj = parameters.get(field);
            index = appendPreparedStatementParameter(s, field.getType(), obj, index);
        }
    }
    private void appendUpdateParameters(TableAdapter adapter, HashMap<Field, Object> parameters, PreparedStatement s) throws Exception {

        //for update statements, we want to write out all non-identity values, but with the non-primary key columns FIRST
        List<Field> primaryKeyCols = adapter.getPrimaryKeyFields();

        List<Field> nonKeyCols = new ArrayList<>(adapter.getColumnFields());
        nonKeyCols.removeAll(primaryKeyCols);

        int index = 1;

        for (Field field: nonKeyCols) {

            //we can't update Identity columns, so skip them
            if (field.isAnnotationPresent(IdentityColumn.class)) {
                continue;
            }

            Object obj = parameters.get(field);
            index = appendPreparedStatementParameter(s, field.getType(), obj, index);
        }

        for (Field field: primaryKeyCols) {
            Object obj = parameters.get(field);
            index = appendPreparedStatementParameter(s, field.getType(), obj, index);
        }
    }
    private void appendDeleteParameters(TableAdapter adapter, HashMap<Field, Object> parameters, PreparedStatement s) throws Exception {

        //for delete statements, we only need the primary key values
        List<Field> fields = adapter.getPrimaryKeyFields();
        int index = 1;
        for (Field field: fields) {

            Object obj = parameters.get(field);
            index = appendPreparedStatementParameter(s, field.getType(), obj, index);
        }
    }

    private <T extends DbAbstractTable> String getParameterisedWriteSql(T entity) throws Exception {

        TableAdapter adapter = entity.getAdapter();

        //checked our hashMap caches for pre-prepared SQL
        String sql = null;
        switch (entity.getSaveMode()) {
            case INSERT:
                sql = parameterisedInsertSql.get(adapter);
                break;
            case UPDATE:
                sql = parameterisedUpdateSql.get(adapter);
                break;
            case DELETE:
                sql = parameterisedDeleteSql.get(adapter);
                break;
            default:
                throw new RuntimeException("Invalid save mode " + entity.getSaveMode());
        }

        //if not cached, create and cache SQL
        if (sql == null) {
            switch (entity.getSaveMode()) {
                case INSERT:
                    sql = getParameterisedInsertSql(adapter);
                    parameterisedInsertSql.put(adapter, sql);
                    break;
                case UPDATE:
                    sql = getParameterisedUpdateSql(adapter);
                    parameterisedUpdateSql.put(adapter, sql);
                    break;
                case DELETE:
                    sql = getParameterisedDeleteSql(adapter);
                    parameterisedDeleteSql.put(adapter, sql);
                    break;
                default:
                    throw new RuntimeException("Invalid save mode " + entity.getSaveMode());
            }
        }
        return sql;
    }
    private String getParameterisedInsertSql(TableAdapter adapter) {

        StringJoiner columnNames = new StringJoiner(", ");
        StringJoiner parameters = new StringJoiner(", ");

        List<Field> cols = adapter.getColumnFields();
        for (int i=0; i<cols.size(); i++) {
            Field field = cols.get(i);

            //identity columns are assigned by SQL Server, so skip them in the insert statement
            if (field.isAnnotationPresent(IdentityColumn.class)) {
                continue;
            }

            columnNames.add(TableAdapter.uppercaseFieldName(field));
            parameters.add("?");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        adapter.appendSchemaAndTableName(sb);
        sb.append("(");
        sb.append(columnNames.toString());
        sb.append(")");
        sb.append(" VALUES (");
        sb.append(parameters.toString());
        sb.append(")");
        return sb.toString();
    }
    private String getParameterisedUpdateSql(TableAdapter adapter) {

        //separate the full list of columns into primary key cols and the remainder
        List<Field> primaryKeyCols = adapter.getPrimaryKeyFields();

        List<Field> nonKeyCols = new ArrayList<>(adapter.getColumnFields());
        nonKeyCols.removeAll(primaryKeyCols);


        StringJoiner sets = new StringJoiner(", ");
        for (int i=0; i<nonKeyCols.size(); i++) {
            Field field = nonKeyCols.get(i);

            //skip any identity columns, as these shouldn't be updated
            if (field.isAnnotationPresent(IdentityColumn.class)) {
                continue;
            }

            String set = TableAdapter.uppercaseFieldName(field) + " = ?";
            sets.add(set);
        }

        StringJoiner wheres = new StringJoiner(" AND ");
        for (int i = 0; i < primaryKeyCols.size(); i++) {
            Field primaryKey = primaryKeyCols.get(i);

            String where = TableAdapter.uppercaseFieldName(primaryKey) + " = ?";
            wheres.add(where);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        adapter.appendSchemaAndTableName(sb);
        sb.append(" SET ");
        sb.append(sets.toString());
        sb.append(" WHERE ");
        sb.append(wheres.toString());
        return sb.toString();
    }
    private String getParameterisedDeleteSql(TableAdapter adapter) {

        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        adapter.appendSchemaAndTableName(sb);
        sb.append(" WHERE ");

        String[] primaryKeyCols = adapter.getPrimaryKeyColumns();
        for (int i=0; i<primaryKeyCols.length; i++) {
            String primaryKeyCol = primaryKeyCols[i];
            if (i > 0) {
                sb.append("AND ");
            }
            sb.append(primaryKeyCol);
            sb.append(" = ?");
        }

        return sb.toString();
    }
    /*public void writeEntities(List<DbAbstractTable> entities) throws Exception {
        if (entities.isEmpty()) {
            return;
        }

        LOG.trace("Writing {} entities to DB", entities.size());

        Connection connection = DatabaseManager.getConnection();
        Statement statement = connection.createStatement();

        StringJoiner sqlLogging = new StringJoiner("\r\n");

        for (DbAbstractTable entity : entities) {
            String sql = writeSql(entity);
            sqlLogging.add(sql);
            statement.addBatch(sql);
        }

        LOG.trace("Executing {}", sqlLogging.toString());

        try {
            statement.executeBatch();
            connection.commit();

        } catch (SQLException sqlEx) {

            LOG.error("Error in SQL {}", sqlLogging.toString());
            connection.rollback();
            //don't return the connection, since the problem maybe at the connection level
            throw sqlEx;
        } finally {
            DatabaseManager.closeConnection(connection);
        }

    }*/


    /*private static String writeSql(DbAbstractTable entity) throws Exception {
        switch (entity.getSaveMode()) {
            case INSERT:
                return writeInsertSql(entity);
            case UPDATE:
                return writeUpdateSql(entity);
            case DELETE:
                return writeDeleteSql(entity);
            default:
                throw new RuntimeException("Invalid save mode " + entity.getSaveMode());
        }
    }

    private static String writeInsertSql(DbAbstractTable entity) throws Exception {
        TableAdapter a = entity.getAdapter();

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        a.appendSchemaAndTableName(sb);
        sb.append(" VALUES (");

        ArrayList<Object> values = new ArrayList<Object>();
        a.writeForDb(entity, values, true);

        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);

            if (i > 0) {
                sb.append(", ");
            }

            String s = convertToString(value);
            sb.append(s);
        }

        sb.append(")");

        return sb.toString();
    }

    private static String writeUpdateSql(DbAbstractTable entity) throws Exception {
        TableAdapter a = entity.getAdapter();

        ArrayList<Object> values = new ArrayList<Object>();
        a.writeForDb(entity, values, false);

        String[] primaryKeyCols = a.getPrimaryKeyColumns();
        String[] cols = a.getColumns();

        List<String> nonKeyCols = new ArrayList<String>();
        HashMap<String, String> hmColValues = new HashMap<String, String>();

        for (int i = 0; i < cols.length; i++) {
            String col = cols[i];
            Object value = values.get(i);
            String s = convertToString(value);

            hmColValues.put(col, s);

            //see if a primary key column
            boolean isPrimaryKey = false;
            for (int j = 0; j < primaryKeyCols.length; j++) {
                String primaryKeyCol = primaryKeyCols[j];
                if (col.equalsIgnoreCase(primaryKeyCol)) {
                    isPrimaryKey = true;
                    break;
                }
            }
            if (!isPrimaryKey) {
                nonKeyCols.add(col);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        a.appendSchemaAndTableName(sb);
        sb.append(" SET ");

        for (int i = 0; i < nonKeyCols.size(); i++) {
            String nonKeyCol = nonKeyCols.get(i);
            String val = hmColValues.get(nonKeyCol);

            if (i > 0) {
                sb.append(", ");
            }

            sb.append(nonKeyCol);
            sb.append(" = ");
            sb.append(val);
        }

        sb.append(" WHERE ");

        for (int i = 0; i < primaryKeyCols.length; i++) {
            String primaryKeyCol = primaryKeyCols[i];
            String val = hmColValues.get(primaryKeyCol);

            if (i > 0) {
                sb.append("AND ");
            }

            sb.append(primaryKeyCol);
            sb.append(" = ");
            sb.append(val);
        }

        return sb.toString();
    }

    private static String writeDeleteSql(DbAbstractTable entity) throws Exception {
        TableAdapter a = entity.getAdapter();

        ArrayList<Object> values = new ArrayList<Object>();
        a.writeForDb(entity, values, false);

        String[] primaryKeyCols = a.getPrimaryKeyColumns();
        String[] cols = a.getColumns();

        HashMap<String, String> hmColValues = new HashMap<String, String>();

        for (int i = 0; i < cols.length; i++) {
            String col = cols[i];
            Object value = values.get(i);
            String s = convertToString(value);

            hmColValues.put(col, s);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        a.appendSchemaAndTableName(sb);
        sb.append(" WHERE ");

        for (int i = 0; i < primaryKeyCols.length; i++) {
            String primaryKeyCol = primaryKeyCols[i];
            String val = hmColValues.get(primaryKeyCol);

            if (i > 0) {
                sb.append("AND ");
            }

            sb.append(primaryKeyCol);
            sb.append(" = ");
            sb.append(val);
        }

        return sb.toString();
    }*/

    @Override
    public <T extends DbAbstractTable> T retrieveForPrimaryKeys(Class<T> type, Object... keys) throws Exception {
        TableAdapter a = type.newInstance().getAdapter();
        String[] primaryKeyCols = a.getPrimaryKeyColumns();
        if (primaryKeyCols.length != keys.length) {
            throw new RuntimeException("Primary keys length (" + primaryKeyCols.length + ")doesn't match keys length (" + keys.length + ")");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("WHERE ");

        for (int i = 0; i < primaryKeyCols.length; i++) {
            String primaryKeyCol = primaryKeyCols[i];
            if (i > 0) {
                sb.append(" AND ");
            }

            sb.append(primaryKeyCol);
            sb.append(" = ?");
        }

        String whereStatement = sb.toString();
        return retrieveOneForWherePreparedStatement(type, whereStatement, keys);
    }
/*    public DbAbstractTable retrieveForPrimaryKeys(TableAdapter a, Object... keys) throws Exception {
        String[] primaryKeyCols = a.getPrimaryKeyColumns();
        if (primaryKeyCols.length != keys.length) {
            throw new RuntimeException("Primary keys length (" + primaryKeyCols.length + ")doesn't match keys length (" + keys.length + ")");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("WHERE ");

        for (int i = 0; i < primaryKeyCols.length; i++) {
            String primaryKeyCol = primaryKeyCols[i];
            Object o = keys[i];
            String val = convertToString(o);

            if (i > 0) {
                sb.append(" AND ");
            }

            sb.append(primaryKeyCol);
            sb.append(" = ");
            sb.append(val);
        }

        String whereStatement = sb.toString();
        return retrieveSingleForWhere(a, whereStatement);
    }*/

    private static void appendPreparedStatementParameters(PreparedStatement s, Object... parameters) throws Exception {
        int index = 1;
        for (Object parameter: parameters) {
            Class type = parameter.getClass();
            index = appendPreparedStatementParameter(s, type, parameter, index);
        }
    }
    private static int appendPreparedStatementParameter(PreparedStatement s, Class<?> cls, Object o, int index) throws Exception {
        LOG.trace("Parameter {}: {}", index, o);

        if (cls == String.class) {
            if (o == null) {
                s.setNull(index++, Types.VARCHAR);
            } else {
                s.setString(index++, (String)o);
            }
        } else if (cls == Integer.class
                || cls == Integer.TYPE) {
            if (o == null) {
                s.setNull(index++, Types.INTEGER);
            } else {
                s.setInt(index++, ((Integer)o).intValue());
            }
        } else if (cls == UUID.class) {
            if (o == null) {
                s.setNull(index++, Types.VARCHAR);
            } else {
                s.setString(index++, ((UUID) o).toString());
            }
        } else if (cls == Boolean.class
                || cls == Boolean.TYPE) {
            if (o == null) {
                s.setNull(index++, Types.BOOLEAN);
            } else {
                s.setBoolean(index++, ((Boolean)o).booleanValue());
            }
        } else if (cls == Instant.class) {
            if (o == null) {
                s.setNull(index++, Types.TIMESTAMP);
            } else {
                s.setTimestamp(index++, Timestamp.from((Instant)o));
            }
        } else if (cls == DependencyType.class) {
            if (o == null) {
                s.setNull(index++, Types.INTEGER);
            } else {
                s.setInt(index++, ((DependencyType) o).getValue());
            }
        } else if (cls == DefinitionItemType.class) {
            if (o == null) {
                s.setNull(index++, Types.INTEGER);
            } else {
                s.setInt(index++, ((DefinitionItemType) o).getValue());
            }
        } else if (cls == ExecutionStatus.class) {
            if (o == null) {
                s.setNull(index++, Types.INTEGER);
            } else {
                s.setInt(index++, ((ExecutionStatus)o).getValue());
            }
        } else if (cls == ProcessorState.class) {
            if (o == null) {
                s.setNull(index++, Types.INTEGER);
            } else {
                s.setInt(index++, ((ProcessorState)o).getValue());
            }
        } else if (o instanceof List) { //use instanceof in case it's a sub-class of a List
            //if we've been supplied a list, then iterate through, adding each item in the list
            List v = (List)o;
            for (Object vo: v) {
                index = appendPreparedStatementParameter(s, vo.getClass(), vo, index);
            }
        } else {
            throw new RuntimeException("Unsupported entity for database " + o.getClass());
        }

        return index;
    }

    /**
     returns a parameterised string (?, ?, etc.) with the same number of tokens as the list size
     */
    private static String getParameterisedString(List v) {
        List<String> strs = new ArrayList<>();
        for (Object o: v) {
            strs.add("?");
        }
        return String.join(", ", strs);
    }



    /*private DbAbstractTable retrieveSingleForWhere(TableAdapter a, String whereStatement) throws Exception {
        List<DbAbstractTable> v = new ArrayList<DbAbstractTable>();
        retrieveForWhere(a, whereStatement, v);

        if (v.size() == 0) {
            return null;
        } else {
            return v.get(0);
        }
    }


    private void retrieveForWhere(TableAdapter a, String conditions, List results) throws Exception {
        retrieveForWhere(a, Integer.MAX_VALUE, conditions, results);
    }

    private void retrieveForWhere(TableAdapter a, int count, String conditions, List results) throws Exception {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT ");

        if (count < Integer.MAX_VALUE) {
            sb.append("TOP ");
            sb.append(count);
            sb.append(" ");
        }

        String[] cols = a.getColumns();
        for (int i = 0; i < cols.length; i++) {
            String col = cols[i];

            if (i > 0) {
                sb.append(", ");
            }

            sb.append(ALIAS);
            sb.append(".");
            sb.append(col);
        }

        sb.append(" FROM ");
        a.appendSchemaAndTableName(sb);
        sb.append(" ");
        sb.append(ALIAS);
        sb.append(" ");
        sb.append(conditions);

        String sql = sb.toString();

        Connection connection = DatabaseManager.getConnection();
        Statement s = connection.createStatement();
        try {
            LOG.trace("Executing {}", sql);
            s.execute(sql);

            ResultSet rs = s.getResultSet();

            ResultReader rr = new ResultReader(rs);

            while (rr.nextResult()) {
                DbAbstractTable entity = a.newEntity();
                a.readFromDb(entity, rr);
                results.add(entity);
            }

            rs.close();

        } catch (SQLException sqlEx) {
            LOG.error("Error with SQL {}", sql);
            throw sqlEx;
        } finally {
            DatabaseManager.closeConnection(connection);
        }
    }*/

    public <T extends DbAbstractTable> T retrieveOneForWherePreparedStatement(Class<T> type, String whereSql, Object... parameters) throws Exception {
        List<T> results = retrieveForWherePreparedStatement(type, 1, whereSql, parameters);
        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }
    private <T extends DbAbstractTable> List<T> retrieveForWherePreparedStatement(Class<T> type, String whereSql, Object... parameters) throws Exception {
        return retrieveForWherePreparedStatement(type, Integer.MAX_VALUE, whereSql, parameters);
    }
    private <T extends DbAbstractTable> List<T> retrieveForWherePreparedStatement(Class<T> type, int count, String whereSql, Object... parameters) throws Exception {
        TableAdapter a = type.newInstance().getAdapter();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");

        if (count < Integer.MAX_VALUE) {
            sb.append("TOP ");
            sb.append(count);
            sb.append(" ");
        }

        String[] cols = a.getColumns();
        for (int i = 0; i < cols.length; i++) {
            String col = cols[i];

            if (i > 0) {
                sb.append(", ");
            }

            sb.append(ALIAS);
            sb.append(".");
            sb.append(col);
        }

        sb.append(" FROM ");
        a.appendSchemaAndTableName(sb);
        sb.append(" ");
        sb.append(ALIAS);
        sb.append(" ");
        sb.append(whereSql);

        List<T> ret = new ArrayList<>();

        String sql = sb.toString();

        LOG.trace("Executing {}", sql);

        Connection connection = DatabaseManager.getConnection();
        PreparedStatement s = connection.prepareStatement(sql);
        appendPreparedStatementParameters(s, parameters);

        try {
            s.execute();

            ResultSet rs = s.getResultSet();
            ResultReader rr = new ResultReader(rs);

            while (rr.nextResult()) {
                T entity = type.newInstance();
                a.readFromDb(entity, rr);
                ret.add(entity);
            }

            rs.close();

            return ret;

        } catch (SQLException sqlEx) {
            LOG.error("Error with SQL {}", sql);
            throw sqlEx;
        } finally {
            DatabaseManager.closeConnection(connection);
        }
    }


    @Override
    public DbEndUser retrieveEndUserForEmail(String email) throws Exception {
        String where = "WHERE Email = ?";
        return retrieveOneForWherePreparedStatement(DbEndUser.class, where, email);

        /*String where = "WHERE Email = " + convertToString(email); //make sure to convert, to prevent SQL injection
        return (DbEndUser) retrieveSingleForWhere(new DbEndUser().getAdapter(), where);*/
    }

    @Override
    public List<DbEndUser> retrieveSuperUsers() throws Exception {
        String where = "WHERE IsSuperUser = ?";
        return retrieveForWherePreparedStatement(DbEndUser.class, where, Boolean.TRUE);

        /*List<DbEndUser> ret = new ArrayList<>();
        retrieveForWhere(new DbEndUser().getAdapter(), "WHERE IsSuperUser = 1", ret);
        return ret;*/
    }

    @Override
    public List<DbEndUser> retrieveEndUsersForUuids(List<UUID> uuids) throws Exception {
        if (uuids.isEmpty()) {
            return new ArrayList<>();
        }

        String where = "WHERE EndUserUuid IN (" + getParameterisedString(uuids) + ")";
        return retrieveForWherePreparedStatement(DbEndUser.class, where, uuids);
    }

    @Override
    public List<DbOrganisation> retrieveAllOrganisations() throws Exception {
        String where = "WHERE 1=1";
        return retrieveForWherePreparedStatement(DbOrganisation.class, where);

        /*List<DbOrganisation> ret = new ArrayList<DbOrganisation>();
        retrieveForWhere(new DbOrganisation().getAdapter(), "WHERE 1=1", ret);
        return ret;*/
    }

    @Override
    public List<DbEndUserEmailInvite> retrieveEndUserEmailInviteForUserNotCompleted(UUID userUuid) throws Exception {
        String where = "WHERE EndUserUuid = ?"
                + " AND DtCompleted IS NULL";
        return retrieveForWherePreparedStatement(DbEndUserEmailInvite.class, where, userUuid);

        /*List<DbEndUserEmailInvite> ret = new ArrayList<DbEndUserEmailInvite>();
        String where = "WHERE UserUuid = " + convertToString(userUuid)
                + " AND DtCompleted IS NULL";
        retrieveForWhere(new DbEndUserEmailInvite().getAdapter(), where, ret);
        return ret;*/
    }

    @Override
    public DbEndUserEmailInvite retrieveEndUserEmailInviteForToken(String token) throws Exception {
        String where = "WHERE UniqueToken = ?";
        return retrieveOneForWherePreparedStatement(DbEndUserEmailInvite.class, where, token);

        /*String where = "WHERE Token = " + convertToString(token); //make sure to convert, to prevent SQL injection
        return (DbEndUserEmailInvite) retrieveSingleForWhere(new DbEndUserEmailInvite().getAdapter(), where);*/
    }

    @Override
    public DbActiveItem retrieveActiveItemForItemUuid(UUID itemUuid) throws Exception {
        String where = "WHERE ItemUuid = ?";
        return retrieveOneForWherePreparedStatement(DbActiveItem.class, where, itemUuid);

        /*String where = "WHERE ItemUuid = " + convertToString(itemUuid);
        return (DbActiveItem) retrieveSingleForWhere(new DbActiveItem().getAdapter(), where);*/
    }

    @Override
    public List<DbActiveItem> retrieveActiveItemDependentItems(UUID organisationUuid, UUID itemUuid, DependencyType dependencyType) throws Exception {
        String where = "INNER JOIN Definition.ItemDependency d"
                + " ON d.DependentItemUuid = ?"
                + " AND d.DependencyTypeId = ?"
                + " AND d.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND d.AuditUuid = " + ALIAS + ".AuditUuid"
                + " WHERE " + ALIAS + ".OrganisationUuid = ?";
        return retrieveForWherePreparedStatement(DbActiveItem.class, where, itemUuid, dependencyType, organisationUuid);

        /*List<DbActiveItem> ret = new ArrayList<>();
        String where = "INNER JOIN Definition.ItemDependency d"
                + " ON d.DependentItemUuid = " + convertToString(itemUuid)
                + " AND d.DependencyTypeId = " + convertToString(dependencyType)
                + " AND d.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND d.AuditUuid = " + ALIAS + ".AuditUuid"
                + " WHERE " + ALIAS + ".OrganisationUuid = " + convertToString(organisationUuid);

        retrieveForWhere(new DbActiveItem().getAdapter(), where, ret);
        return ret;*/
    }

    @Override
    public List<DbActiveItem> retrieveActiveItemRecentItems(UUID userUuid, UUID organisationUuid, int count) throws Exception {
        String where = "INNER JOIN Definition.Item i"
                + " ON i.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND i.AuditUuid = " + ALIAS + ".AuditUuid"
                + " AND " + ALIAS + ".IsDeleted = 0"
                + " INNER JOIN Definition.Audit a"
                + " ON a.AuditUuid = i.AuditUuid"
                + " AND a.EndUserUuid = ?"
                + " WHERE " + ALIAS + ".ItemTypeId NOT IN (" + DefinitionItemType.LibraryFolder.getValue() + ", " + DefinitionItemType.ReportFolder.getValue() + ")"
                + " AND " + ALIAS + ".OrganisationUuid = ?"
                + " ORDER BY a.TimeStamp DESC";
        return retrieveForWherePreparedStatement(DbActiveItem.class, count, where, userUuid, organisationUuid);

        /*List<DbActiveItem> ret = new ArrayList<>();
        String where = "INNER JOIN Definition.Item i"
                + " ON i.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND i.AuditUuid = " + ALIAS + ".AuditUuid"
                + " AND " + ALIAS + ".IsDeleted = 0"
                + " INNER JOIN Definition.Audit a"
                + " ON a.AuditUuid = i.AuditUuid"
                + " AND a.EndUserUuid = " + convertToString(userUuid)
                + " WHERE " + ALIAS + ".ItemTypeId NOT IN (" + DefinitionItemType.LibraryFolder.getValue() + ", " + DefinitionItemType.ReportFolder.getValue() + ")"
                + " ORDER BY a.TimeStamp DESC";
        retrieveForWhere(new DbActiveItem().getAdapter(), count, where, ret);
        return ret;*/
    }

    @Override
    public DbEndUserPwd retrieveEndUserPwdForUserNotExpired(UUID endUserUuid) throws Exception {
        String where = "WHERE EndUserUuid = ?"
                + " AND DtExpired IS NULL";
        return retrieveOneForWherePreparedStatement(DbEndUserPwd.class, where, endUserUuid);

        /*String where = "WHERE EndUserUuid = " + convertToString(endUserUuid)
                + " AND DtExpired IS NULL";
        return (DbEndUserPwd) retrieveSingleForWhere(new DbEndUserPwd().getAdapter(), where);*/
    }

    @Override
    public List<DbOrganisationEndUserLink> retrieveOrganisationEndUserLinksForOrganisationNotExpired(UUID organisationUuid) throws Exception {
        String where = "WHERE OrganisationUuid = ?"
                + " AND DtExpired IS NULL";
        return retrieveForWherePreparedStatement(DbOrganisationEndUserLink.class, where, organisationUuid);

        /*List<DbOrganisationEndUserLink> ret = new ArrayList<DbOrganisationEndUserLink>();
        String where = "WHERE OrganisationUuid = " + convertToString(organisationUuid)
                + " AND DtExpired IS NULL";
        retrieveForWhere(new DbOrganisationEndUserLink().getAdapter(), where, ret);
        return ret;*/
    }

    @Override
    public List<DbOrganisationEndUserLink> retrieveOrganisationEndUserLinksForUserNotExpired(UUID endUserUuid) throws Exception {
        String where = "WHERE EndUserUuid = ?"
                + " AND DtExpired IS NULL";
        return retrieveForWherePreparedStatement(DbOrganisationEndUserLink.class, where, endUserUuid);

        /*List<DbOrganisationEndUserLink> ret = new ArrayList<DbOrganisationEndUserLink>();
        String where = "WHERE EndUserUuid = " + convertToString(endUserUuid)
                + " AND DtExpired IS NULL";
        retrieveForWhere(new DbOrganisationEndUserLink().getAdapter(), where, ret);
        return ret;*/
    }

    @Override
    public DbOrganisationEndUserLink retrieveOrganisationEndUserLinksForOrganisationEndUserNotExpired(UUID organisationUuid, UUID endUserUuid) throws Exception {
        String where = "WHERE OrganisationUuid = ?"
                + " AND EndUserUuid = ?"
                + " AND DtExpired IS NULL";
        return retrieveOneForWherePreparedStatement(DbOrganisationEndUserLink.class, where, organisationUuid, endUserUuid);

        /*String where = "WHERE OrganisationUuid = " + convertToString(organisationUuid)
                + " AND EndUserUuid = " + convertToString(endUserUuid)
                + " AND DtExpired IS NULL";
        return (DbOrganisationEndUserLink) retrieveSingleForWhere(new DbOrganisationEndUserLink().getAdapter(), where);*/
    }

    @Override
    public DbItem retrieveLatestItemForUuid(UUID itemUuid) throws Exception {
        String where = "INNER JOIN Definition.ActiveItem a"
                + " ON a.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND a.AuditUuid = " + ALIAS + ".AuditUuid"
                + " WHERE a.ItemUuid = ?";
        return retrieveOneForWherePreparedStatement(DbItem.class, where, itemUuid);

        /*String where = "INNER JOIN Definition.ActiveItem a"
                + " ON a.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND a.AuditUuid = " + ALIAS + ".AuditUuid"
                + " WHERE a.ItemUuid = " + convertToString(itemUuid);
        return (DbItem) retrieveSingleForWhere(new DbItem().getAdapter(), where);*/
    }

    @Override
    public DbOrganisation retrieveOrganisationForNameNationalId(String name, String nationalId) throws Exception {

        String where = "WHERE Name = ?"
                + " AND NationalId = ?";
        return retrieveOneForWherePreparedStatement(DbOrganisation.class, where, name, nationalId);

        /*String where = "WHERE Name = " + convertToString(name)
                + " AND NationalId = " + convertToString(nationalId);
        return (DbOrganisation) retrieveSingleForWhere(new DbOrganisation().getAdapter(), where);*/
    }

    @Override
    public List<DbItem> retrieveDependentItems(UUID itemUuid, DependencyType dependencyType) throws Exception {
        String where = "INNER JOIN Definition.ActiveItem a"
                + " ON a.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND a.AuditUuid = " + ALIAS + ".AuditUuid"
                + " AND a.IsDeleted = 0"
                + " INNER JOIN Definition.ItemDependency d"
                + " ON d.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND a.AuditUuid = " + ALIAS + ".AuditUuid"
                + " AND d.DependentItemUuid = ?"
                + " AND d.DependencyTypeId = ?"
                + " INNER JOIN Definition.ActiveItem ad"
                + " ON ad.ItemUuid = d.ItemUuid"
                + " AND ad.AuditUuid = d.AuditUuid"
                + " AND ad.IsDeleted = 0";
        return retrieveForWherePreparedStatement(DbItem.class, where, itemUuid, dependencyType);

        /*List<DbItem> ret = new ArrayList<>();
        String where = "INNER JOIN Definition.ActiveItem a"
                + " ON a.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND a.AuditUuid = " + ALIAS + ".AuditUuid"
                + " AND a.IsDeleted = 0"
                + " INNER JOIN Definition.ItemDependency d"
                + " ON d.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND a.AuditUuid = " + ALIAS + ".AuditUuid"
                + " AND d.DependentItemUuid = " + convertToString(itemUuid)
                + " AND d.DependencyTypeId = " + convertToString(dependencyType)
                + " INNER JOIN Definition.ActiveItem ad"
                + " ON ad.ItemUuid = d.ItemUuid"
                + " AND ad.AuditUuid = d.AuditUuid"
                + " AND ad.IsDeleted = 0";

        retrieveForWhere(new DbItem().getAdapter(), where, ret);
        return ret;*/
    }

    @Override
    public List<DbItem> retrieveNonDependentItems(UUID organisationUuid, DependencyType dependencyType, DefinitionItemType itemType) throws Exception {
        String where = "INNER JOIN Definition.ActiveItem a"
                + " ON a.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND a.AuditUuid = " + ALIAS + ".AuditUuid"
                + " AND a.ItemTypeId = ?"
                + " AND a.OrganisationUuid = ?"
                + " AND a.IsDeleted = 0"
                + " WHERE NOT EXISTS ("
                + "SELECT 1 FROM Definition.ItemDependency d"
                + " WHERE d.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND d.AuditUuid = " + ALIAS + ".AuditUuid"
                + " AND d.DependencyTypeId = ?"
                + ")";
        return retrieveForWherePreparedStatement(DbItem.class, where, itemType, organisationUuid, dependencyType);

        /*List<DbItem> ret = new ArrayList<DbItem>();
        String where = "INNER JOIN Definition.ActiveItem a"
                + " ON a.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND a.AuditUuid = " + ALIAS + ".AuditUuid"
                + " AND a.ItemTypeId = " + convertToString(itemType)
                + " AND a.OrganisationUuid = " + convertToString(organisationUuid)
                + " AND a.IsDeleted = 0"
                + " WHERE NOT EXISTS ("
                + "SELECT 1 FROM Definition.ItemDependency d"
                + " WHERE d.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND d.AuditUuid = " + ALIAS + ".AuditUuid"
                + " AND d.DependencyTypeId = " + convertToString(dependencyType)
                + ")";

        retrieveForWhere(new DbItem().getAdapter(), where, ret);
        return ret;*/
    }

    @Override
    public List<DbItem> retrieveItemsForActiveItems(List<DbActiveItem> activeItems) throws Exception {
        if (activeItems.isEmpty()) {
            return new ArrayList<>();
        }

        List<UUID> parameters = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append("WHERE ");

        for (int i=0; i<activeItems.size(); i++) {
            DbActiveItem activeItem = activeItems.get(i);
            UUID itemUuid = activeItem.getItemUuid();
            UUID auditUuid = activeItem.getAuditUuid();

            if (i > 0){
                sb.append(" OR ");
            }
            sb.append("(ItemUuid = ? AND AuditUuid = ?)");

            parameters.add(itemUuid);
            parameters.add(auditUuid);
        }
        String where = sb.toString();
        return retrieveForWherePreparedStatement(DbItem.class, where, parameters);

        /*List<DbItem> ret = new ArrayList<DbItem>();
        if (activeItems.isEmpty()) {
            return ret;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("WHERE ");

        for (int i=0; i<activeItems.size(); i++) {
            DbActiveItem activeItem = activeItems.get(i);
            UUID itemUuid = activeItem.getItemUuid();
            UUID auditUuid = activeItem.getAuditUuid();

            if (i > 0){
                sb.append(" OR ");
            }
            sb.append("(");
            sb.append("ItemUuid = ");
            sb.append(convertToString(itemUuid));
            sb.append(" AND AuditUuid = ");
            sb.append(convertToString(auditUuid));
            sb.append(")");
        }
        String where = sb.toString();

        retrieveForWhere(new DbItem().getAdapter(), where, ret);
        return ret;*/
    }

    @Override
    public List<DbItem> retrieveItemsForJob(UUID jobUuid) throws Exception {
        String where = "INNER JOIN Execution.JobContent c"
                + " ON c.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND c.AuditUuid = " + ALIAS + ".AuditUuid"
                + " AND c.JobUuid = ?";
        return retrieveForWherePreparedStatement(DbItem.class, where, jobUuid);
    }

    @Override
    public List<DbItem> retrieveLatestItemsForUuids(List<UUID> itemUuids) throws Exception {
        if (itemUuids.isEmpty()) {
            return new ArrayList<>();
        }

        String where = "INNER JOIN Definition.ActiveItem a"
                + " ON a.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND a.AuditUuid = " + ALIAS + ".AuditUuid"
                + " AND " + ALIAS + ".ItemUuid IN (" + getParameterisedString(itemUuids) + ")";
        return retrieveForWherePreparedStatement(DbItem.class, where, itemUuids);
    }

    @Override
    public int retrieveCountDependencies(UUID itemUuid, DependencyType dependencyType) throws Exception {
        String sql = "SELECT COUNT(1)"
                + " FROM Definition.ItemDependency d, Definition.ActiveItem a"
                + " WHERE d.DependentItemUuid = ?"
                + " AND d.DependencyTypeId = ?"
                + " AND a.ItemUuid = d.ItemUuid"
                + " AND a.AuditUuid = d.AuditUuid";
        return executeScalarQuery(sql, itemUuid, dependencyType);

        /*String sql = "SELECT COUNT(1)"
                + " FROM Definition.ItemDependency d, Definition.ActiveItem a"
                + " WHERE d.DependentItemUuid = " + convertToString(itemUuid)
                + " AND d.DependencyTypeId = " + convertToString(dependencyType)
                + " AND a.ItemUuid = d.ItemUuid"
                + " AND a.AuditUuid = d.AuditUuid";
        return executeScalarQuery(sql);*/
    }

    @Override
    public List<DbItemDependency> retrieveItemDependenciesForItem(UUID itemUuid, UUID auditUuid) throws Exception {
        String where = "WHERE ItemUuid = ?"
                + " AND AuditUuid = ?";
        return retrieveForWherePreparedStatement(DbItemDependency.class, where, itemUuid, auditUuid);

        /*List<DbItemDependency> ret = new ArrayList<DbItemDependency>();
        String where = "WHERE ItemUuid = " + convertToString(itemUuid)
                + " AND AuditUuid = " + convertToString(auditUuid);
        retrieveForWhere(new DbItemDependency().getAdapter(), where, ret);
        return ret;*/
    }

    @Override
    public List<DbItemDependency> retrieveItemDependenciesForItemType(UUID itemUuid, UUID auditUuid, DependencyType dependencyType) throws Exception {
        String where = "WHERE ItemUuid = ?"
                + " AND AuditUuid = ?"
                + " AND DependencyTypeId = ?";
        return retrieveForWherePreparedStatement(DbItemDependency.class, where, itemUuid, auditUuid, dependencyType);

        /*List<DbItemDependency> ret = new ArrayList<DbItemDependency>();
        String where = "WHERE ItemUuid = " + convertToString(itemUuid)
                + " AND AuditUuid = " + convertToString(auditUuid)
                + " AND DependencyTypeId = " + convertToString(dependencyType);
        retrieveForWhere(new DbItemDependency().getAdapter(), where, ret);
        return ret;*/
    }    

    @Override
    public List<DbItemDependency> retrieveItemDependenciesForDependentItem(UUID dependentItemUuid) throws Exception {
        String where = "INNER JOIN Definition.ActiveItem a"
                + " ON a.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND a.AuditUuid = " + ALIAS + ".AuditUuid"
                + " AND a.IsDeleted = 0"
                + " WHERE DependentItemUuid = ?";
        return retrieveForWherePreparedStatement(DbItemDependency.class, where, dependentItemUuid);

        /*List<DbItemDependency> ret = new ArrayList<DbItemDependency>();
        String where = "INNER JOIN Definition.ActiveItem a"
                + " ON a.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND a.AuditUuid = " + ALIAS + ".AuditUuid"
                + " AND a.IsDeleted = 0"
                + " WHERE DependentItemUuid = " + convertToString(dependentItemUuid);
        retrieveForWhere(new DbItemDependency().getAdapter(), where, ret);
        return ret;*/
    }


    @Override
    public List<DbItemDependency> retrieveItemDependenciesForDependentItemType(UUID dependentItemUuid, DependencyType dependencyType) throws Exception {
        String where = "INNER JOIN Definition.ActiveItem a"
                + " ON a.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND a.AuditUuid = " + ALIAS + ".AuditUuid"
                + " AND a.IsDeleted = 0"
                + " WHERE DependentItemUuid = ?"
                + " AND DependencyTypeId = ?";
        return retrieveForWherePreparedStatement(DbItemDependency.class, where, dependentItemUuid, dependencyType);

        /*List<DbItemDependency> ret = new ArrayList<>();
        String where = "INNER JOIN Definition.ActiveItem a"
                + " ON a.ItemUuid = " + ALIAS + ".ItemUuid"
                + " AND a.AuditUuid = " + ALIAS + ".AuditUuid"
                + " AND a.IsDeleted = 0"
                + " WHERE DependentItemUuid = " + convertToString(dependentItemUuid)
                + " AND DependencyTypeId = " + convertToString(dependencyType);
        retrieveForWhere(new DbItemDependency().getAdapter(), where, ret);
        return ret;*/
    }




    @Override
    public List<DbRequest> retrievePendingRequestsForItems(UUID organisationUuid, List<UUID> itemUuids) throws Exception {
        if (itemUuids.isEmpty()) {
            return new ArrayList<>();
        }

        String where = "WHERE JobReportUuid IS NULL"
                + " AND OrganisationUuid = ?"
                + " AND ReportUuid IN (" + getParameterisedString(itemUuids) + ")";
        return retrieveForWherePreparedStatement(DbRequest.class, where, organisationUuid, itemUuids);
    }

    @Override
    public List<DbRequest> retrievePendingRequests() throws Exception {
        String where = "WHERE JobReportUuid IS NULL";
        return retrieveForWherePreparedStatement(DbRequest.class, where);
    }

    @Override
    public List<DbRequest> retrieveRequestsForItem(UUID organisationUuid, UUID itemUuid, int count) throws Exception {
        String where = "WHERE OrganisationUuid = ?"
                + " AND ReportUuid = ?"
                + " ORDER BY TimeStamp DESC";
        return retrieveForWherePreparedStatement(DbRequest.class, count, where, organisationUuid, itemUuid);
    }

    @Override
    public List<DbJob> retrieveRecentJobs(int count) throws Exception {
        String where = "ORDER BY StartDateTime DESC";
        return retrieveForWherePreparedStatement(DbJob.class, count, where);

        /*List<DbJob> ret = new ArrayList<>();
        String where = "ORDER BY StartDateTime DESC";
        retrieveForWhere(new DbJob().getAdapter(), count, where, ret);
        return ret;*/
    }

    @Override
    public List<DbJob> retrieveJobsForStatus(ExecutionStatus status) throws Exception {
        String where = "WHERE StatusId = ?";
        return retrieveForWherePreparedStatement(DbJob.class, where, status);

        /*List<DbJob> ret = new ArrayList<>();
        String where = "WHERE StatusId = " + convertToString(status);
        retrieveForWhere(new DbJob().getAdapter(), where, ret);
        return ret;*/
    }

    @Override
    public List<DbJob> retrieveJobsForUuids(List<UUID> uuids) throws Exception {
        if (uuids.isEmpty()) {
            return new ArrayList<DbJob>();
        }
        String where = "WHERE JobUuid IN (" + getParameterisedString(uuids) + ")";
        return retrieveForWherePreparedStatement(DbJob.class, where, uuids);

        /*List<DbJob> ret = new ArrayList<>();
        if (uuids.isEmpty()) {
            return ret;
        }

        String where = "WHERE JobUuid IN (" + convertToString(uuids) + ")";
        retrieveForWhere(new DbJob().getAdapter(), where, ret);
        return ret;*/
    }

    @Override
    public List<DbJob> retrieveJobsForJobReportUuids(List<UUID> uuids) throws Exception {
        if (uuids.isEmpty()) {
            return new ArrayList<DbJob>();
        }

        String where = "INNER JOIN ON Execution.JobReport r "
                + " ON r.JobReportUuid IN (" + getParameterisedString(uuids) + ")"
                + " AND r.JobUuid = " + ALIAS + ".JobUuid";
        return retrieveForWherePreparedStatement(DbJob.class, where, uuids);
    }


    @Override
    public List<DbJobReport> retrieveJobReports(UUID organisationUuid, int count) throws Exception {
        String where = "INNER JOIN Execution.Job a"
                + " ON a.JobUuid = " + ALIAS + ".JobUuid"
                + " WHERE " + ALIAS + ".OrganisationUuid = ?"
                + " ORDER BY a.StartDateTime DESC";
        return retrieveForWherePreparedStatement(DbJobReport.class, count, where, organisationUuid);

        /*List<DbJobReport> ret = new ArrayList<>();
        String where = "INNER JOIN Execution.Job a"
                + " ON a.JobUuid = " + ALIAS + ".JobUuid"
                + " ORDER BY a.StartDateTime DESC";
        retrieveForWhere(new DbJobReport().getAdapter(), count, where, ret);
        return ret;*/
    }

    @Override
    public List<DbJobReport> retrieveJobReportsForJob(UUID jobUuid) throws Exception {
        String where = "WHERE JobUuid = ?";
        return retrieveForWherePreparedStatement(DbJobReport.class, where, jobUuid);

        /*List<DbJobReport> ret = new ArrayList<>();
        String where = "WHERE JobUuid = " + convertToString(jobUuid);
        retrieveForWhere(new DbJobReport().getAdapter(), where, ret);
        return ret;*/
    }

    @Override
    public List<DbJobReport> retrieveLatestJobReportsForItemUuids(UUID organisationUuid, List<UUID> itemUuids) throws Exception {
        if (itemUuids.isEmpty()) {
            return new ArrayList<DbJobReport>();
        }

        String where = "INNER JOIN Execution.Job j"
                + " ON j.JobUuid = " + ALIAS + ".JobUuid"
                + " WHERE OrganisationUuid = ?"
                + " AND ReportUuid IN (" + getParameterisedString(itemUuids) + ")"
                + " AND NOT EXISTS (SELECT 1 FROM Execution.JobReport laterJobReport, Execution.Job laterJob"
                + " WHERE laterJobReport.JobReportUuid = " + ALIAS + ".JobReportUuid"
                + " AND laterJob.JobUuid = laterJobReport.JobUuid"
                + " AND laterJob.StartDateTime > j.StartDateTime)";
        return retrieveForWherePreparedStatement(DbJobReport.class, where, organisationUuid, itemUuids);

        /*List<DbJobReport> ret = new ArrayList<>();
        if (itemUuids.isEmpty()) {
            return ret;
        }

        String where = "INNER JOIN Execution.Job j"
                + " ON j.JobUuid = " + ALIAS + ".JobUuid"
                + " WHERE OrganisationUuid = " + convertToString(organisationUuid)
                + " AND ReportUuid IN (" + convertToString(itemUuids) + ")"
                + " AND NOT EXISTS (SELECT 1 FROM Execution.JobReport laterJobReport, Execution.Job laterJob"
                + " WHERE laterJobReport.JobReportUuid = " + ALIAS + ".JobReportUuid"
                + " AND laterJob.JobUuid = laterJobReport.JobUuid"
                + " AND laterJob.StartDateTime > j.StartDateTime)";
        retrieveForWhere(new DbJobReport().getAdapter(), where, ret);
        return ret;*/
    }

    @Override
    public DbJobReport retrieveJobReportForJobAndReportAndParameters(UUID jobUuid, UUID reportUuid, String parameters) throws Exception {
        String where = "WHERE JobUuid = ?"
                + " AND ReportUuid = ?"
                + " AND Parameters = ?";
        return retrieveOneForWherePreparedStatement(DbJobReport.class, where, jobUuid, reportUuid, parameters);
    }

    @Override
    public List<DbJobReport> retrieveJobReportsForUuids(List<UUID> uuids) throws Exception {
        if (uuids.isEmpty()) {
            return new ArrayList<DbJobReport>();
        }

        String where = "WHERE JobReportUuid IN (" + getParameterisedString(uuids) + ")";
        return retrieveForWherePreparedStatement(DbJobReport.class, where, uuids);
    }

    @Override
    public List<DbJobReportItem> retrieveJobReportItemsForJobReport(UUID jobReportUuid) throws Exception {
        String where = "WHERE JobReportUuid = ?";
        return retrieveForWherePreparedStatement(DbJobReportItem.class, where, jobReportUuid);

        /*List<DbJobReportItem> ret = new ArrayList<>();
        String where = "WHERE JobReportUuid = " + convertToString(jobReportUuid);
        retrieveForWhere(new DbJobReportItem().getAdapter(), where, ret);
        return ret;*/
    }

    @Override
    public List<DbAudit> retrieveAuditsForUuids(List<UUID> uuids) throws Exception {
        if (uuids.isEmpty()) {
            return new ArrayList<DbAudit>();
        }

        String where = "WHERE AuditUuid IN (" + getParameterisedString(uuids) + ")";
        return retrieveForWherePreparedStatement(DbAudit.class, where, uuids);

        /*List<DbAudit> ret = new ArrayList<>();
        if (uuids.isEmpty()) {
            return ret;
        }

        String where = "WHERE AuditUuid IN (" + convertToString(uuids) + ")";
        retrieveForWhere(new DbAudit().getAdapter(), where, ret);
        return ret;*/
    }

    @Override
    public DbAudit retrieveLatestAudit() throws Exception {
        String where = "WHERE AuditVersion = "
                + "(SELECT MAX(AuditVersion)"
                + " FROM Definition.Audit)";
        return retrieveOneForWherePreparedStatement(DbAudit.class, where);

        /*String sql = "WHERE AuditVersion = "
                + "(SELECT MAX(AuditVersion)"
                + " FROM Definition.Audit)";
        return (DbAudit)retrieveSingleForWhere(new DbAudit().getAdapter(), sql);*/
    }

    @Override
    public List<DbJobContent> retrieveJobContentsForJob(UUID jobUuid) throws Exception {
        String where = "WHERE JobUuid = ?";
        return retrieveForWherePreparedStatement(DbJobContent.class, where, jobUuid);

        /*List<DbJobContent> ret = new ArrayList<>();
        String where = "WHERE JobUuid = " + convertToString(jobUuid);
        retrieveForWhere(new DbJobContent().getAdapter(), where, ret);
        return ret;*/
    }

    @Override
    public DbJobReportOrganisation retrieveJobReportOrganisationForJobReportAndOdsCode(UUID jobReportUuid, String odsCode) throws Exception {
        String where = "WHERE JobReportUuid = ?"
                + " AND OrganisationOdsCode = ?";
        return retrieveOneForWherePreparedStatement(DbJobReportOrganisation.class, where, jobReportUuid, odsCode);
    }

    @Override
    public DbJobReportItemOrganisation retrieveJobReportItemOrganisationForJobReportItemAndOdsCode(UUID jobReportItemUUid, String odsCode) throws Exception {
        String where = "WHERE JobReportItemUuid = ?"
                + " AND OrganisationOdsCode = ?";
        return retrieveOneForWherePreparedStatement(DbJobReportItemOrganisation.class, where, jobReportItemUUid, odsCode);

    }

    @Override
    public List<DbJobProcessorResult> retrieveJobProcessorResultsForJob(UUID jobUuid) throws Exception {
        String where = "WHERE JobUuid = ?";
        return retrieveForWherePreparedStatement(DbJobProcessorResult.class, where, jobUuid);

    }

    @Override
    public void deleteAllJobProcessorResults() throws Exception {
        String sql = "DELETE FROM Execution.JobProcessorResult";
        executeQueryNoResult(sql);
    }

    @Override
    public List<DbSourceOrganisationSet> retrieveAllOrganisationSets(UUID organisationUuid) throws Exception {
        String where = "WHERE OrganisationUuid = ?";
        return retrieveForWherePreparedStatement(DbSourceOrganisationSet.class, where, organisationUuid);
    }

    @Override
    public List<DbSourceOrganisationSet> retrieveOrganisationSetsForSearchTerm(UUID organisationUuid, String searchTerm) throws Exception {
        String where = "WHERE OrganisationUuid = ?"
                + " AND Name LIKE ?";
        return retrieveForWherePreparedStatement(DbSourceOrganisationSet.class, where, organisationUuid, searchTerm + "%");
    }

    @Override
    public List<DbSourceOrganisation> retrieveAllSourceOrganisations(boolean includeUnreferencedOnes) throws Exception {
        String where = "WHERE 1=1";
        if (!includeUnreferencedOnes) {
            where += " AND IsReferencedByData = 1";
        }
        return retrieveForWherePreparedStatement(DbSourceOrganisation.class, where);
    }

    @Override
    public List<DbSourceOrganisation> retrieveSourceOrganisationsForSearch(String searchTerm) throws Exception {
        String where = "WHERE (Name LIKE ? OR OdsCode LIKE ?)"
                + " AND IsReferencedByData = 1";
        return retrieveForWherePreparedStatement(DbSourceOrganisation.class, where, searchTerm + "%", searchTerm + "%");
    }

    @Override
    public List<DbSourceOrganisation> retrieveSourceOrganisationsForOdsCodes(List<String> odsCodes) throws Exception {
        if (odsCodes.isEmpty()) {
            return new ArrayList<>();
        }
        String where = "WHERE OdsCode IN (" + getParameterisedString(odsCodes) + ")";
        return retrieveForWherePreparedStatement(DbSourceOrganisation.class, where, odsCodes);
    }

    @Override
    public DbProcessorStatus retrieveCurrentProcessorStatus() throws Exception {
        String where = "WHERE 1=1";
        return retrieveOneForWherePreparedStatement(DbProcessorStatus.class, where);
    }

    @Override
    public void deleteCurrentProcessorStatus() throws Exception {
        String sql = "DELETE FROM Execution.ProcessorStatus";
        executeQueryNoResult(sql);
    }


}

