package org.endeavourhealth.hl7receiver.logging;

import org.endeavourhealth.core.postgres.PgStoredProcException;
import org.endeavourhealth.hl7receiver.model.db.DbErrorIdentifier;

public interface IDBDigestLogger {
    void logErrorDigest(String logClass, String logMethod, String logMessage, String exception) throws PgStoredProcException;
}
