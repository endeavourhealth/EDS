package org.endeavourhealth.hl7receiver.logging;

import org.endeavourhealth.core.postgres.PgStoredProcException;
import org.endeavourhealth.hl7receiver.model.db.DbErrorIdentifier;

public interface IDBLogger {
    DbErrorIdentifier logError(String exception, String method, String message) throws PgStoredProcException;
}
