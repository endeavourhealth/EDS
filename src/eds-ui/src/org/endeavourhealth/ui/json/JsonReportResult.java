package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonReportResult {

    private Integer populationCount = null;
    private List<JsonQueryResult> queryResults = null;

    public JsonReportResult() {}

    /**
     * gets/sets
     */
    public Integer getPopulationCount() {
        return populationCount;
    }

    public void setPopulationCount(Integer populationCount) {
        this.populationCount = populationCount;
    }

    public List<JsonQueryResult> getQueryResults() {
        return queryResults;
    }

    public void setQueryResults(List<JsonQueryResult> queryResults) {
        this.queryResults = queryResults;
    }
}
