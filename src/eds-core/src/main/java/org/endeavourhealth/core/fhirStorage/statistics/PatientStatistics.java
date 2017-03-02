package org.endeavourhealth.core.fhirStorage.statistics;

public class PatientStatistics {
    private long totalCount;
    private long activeCount;
    private long deceasedCount;

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(long activeCount) {
        this.activeCount = activeCount;
    }

    public long getDeceasedCount() {
        return deceasedCount;
    }

    public void setDeceasedCount(long deceasedCount) {
        this.deceasedCount = deceasedCount;
    }
}
