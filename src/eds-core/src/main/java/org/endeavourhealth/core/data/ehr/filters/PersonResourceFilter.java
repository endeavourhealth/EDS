package org.endeavourhealth.core.data.ehr.filters;

import com.datastax.driver.core.BoundStatement;
import org.endeavourhealth.core.data.PreparedStatementCache;

public interface PersonResourceFilter {
    BoundStatement toStatement(PreparedStatementCache statementCache, String keyspace, String table);
}
