package org.endeavourhealth.core.data.admin.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = "admin", name = "snomed_lookup")
public class SnomedLookup {

    @PartitionKey
    @Column(name = "concept_id")
    private String conceptId = null;

    @Column(name = "type_id")
    private String typeId = null;

    @Column(name = "term")
    private String term = null;

    public String getConceptId() {
        return conceptId;
    }

    public void setConceptId(String conceptId) {
        this.conceptId = conceptId;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }
}
