package org.endeavourhealth.ui.database;

import java.util.List;

/**
 * Base class for all DB entities, containing common methods for saving and retrieving. All actual
 * persistence is done in the TableAdapter class
 */
public abstract class DbAbstractTable {

    private TableSaveMode saveMode = null;

    public abstract TableAdapter getAdapter();

    public void writeToDb() throws Exception {
        DatabaseManager.db().writeEntity(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DbAbstractTable) {
            DbAbstractTable other = (DbAbstractTable) o;
            try {
                List<Object> otherKeys = other.getAdapter().getPrimaryKeys(other);
                List<Object> ourKeys = getAdapter().getPrimaryKeys(this);
                return ourKeys.equals(otherKeys);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
        return super.equals(o);
    }

    public void assignPrimaryUUid() throws Exception {
        getAdapter().assignPrimaryKeys(this);
        saveMode = TableSaveMode.INSERT;
    }

    /**
     * get/sets
     */
    public TableSaveMode getSaveMode() throws Exception {

        if (!getAdapter().hasPrimaryKeysSet(this)) {
            //if we have no primary UUID, then generate one and go into insert mode
            assignPrimaryUUid();
        } else if (saveMode == null) {
            //if we have a UUID, but no explicity set save mode, then assume an update
            saveMode = TableSaveMode.UPDATE;
        }

        return saveMode;
    }

    public void setSaveMode(TableSaveMode saveMode) {
        this.saveMode = saveMode;
    }
}

