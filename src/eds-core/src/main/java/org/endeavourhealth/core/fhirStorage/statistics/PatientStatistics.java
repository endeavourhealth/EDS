package org.endeavourhealth.core.fhirStorage.statistics;

public class PatientStatistics {
    private int totalCount;
    private int activeCount;
    private int deceasedCount;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public int getDeceasedCount() {
        return deceasedCount;
    }

    public void setDeceasedCount(int deceasedCount) {
        this.deceasedCount = deceasedCount;
    }
}
