package org.endeavourhealth.ui.querydocument;

import java.util.UUID;

public class QueryDocumentHelper {
    public static UUID parseMandatoryUuid(String uuidString) {
        if (uuidString == null)
            throw new IllegalArgumentException("UuidString cannot be null");
        else
            return UUID.fromString(uuidString);
    }

    public static UUID parseOptionalUuid(String uuidString) {
        if (uuidString == null)
            return null;
        else
            return UUID.fromString(uuidString);
    }
}
