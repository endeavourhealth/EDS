package org.endeavourhealth.ui.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

public final class ResultReader {

    private static final int FIRST_COL = 1; //keep forgetting this, as it's not zero, so made a constant

    private ResultSet rs = null;
    private int currentCol = FIRST_COL;

    public ResultReader(ResultSet rs) {
        this.rs = rs;
    }

    public String readString() throws SQLException {
        return rs.getString(currentCol++);
    }

    public Integer readInt() throws SQLException {
        int ret = rs.getInt(currentCol++);
        if (rs.wasNull()) {
            return null;
        } else {
            return new Integer(ret);
        }
    }

    public UUID readUuid() throws SQLException {
        String uuidString = rs.getString(currentCol++);

        //UUID may be null
        return uuidString == null ? null : UUID.fromString(uuidString);
    }

    public boolean readBoolean() throws SQLException {
        return rs.getBoolean(currentCol++);
    }

    public Instant readDateTime() throws SQLException {
        Timestamp ts = rs.getTimestamp(currentCol++);
        if (ts == null) {
            return null;
        } else {
            return ts.toInstant();
        }
    }

    public boolean nextResult() throws SQLException {
        currentCol = FIRST_COL;
        return rs.next();
    }

}
