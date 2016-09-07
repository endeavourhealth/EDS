package org.endeavourhealth.core.fhirStorage.statistics;

public class ResourceStatistics {
    private final String resourceType;
    private long totalCount;
    private long deletedCount;

    public String getResourceName() {
        return resourceType;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(long deletedCount) {
        this.deletedCount = deletedCount;
    }

    public ResourceStatistics(String resourceType) {
        this.resourceType = resourceType;
    }
}
