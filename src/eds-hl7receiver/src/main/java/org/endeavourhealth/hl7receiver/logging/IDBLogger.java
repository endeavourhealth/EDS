package org.endeavourhealth.hl7receiver.logging;

import org.endeavourhealth.hl7receiver.model.db.DbErrorIdentifier;
import org.endeavourhealth.utilities.postgres.PgStoredProcException;

public interface IDBLogger {
    DbErrorIdentifier logError(String exception, String method, String message) throws PgStoredProcException;
}
