package org.endeavourhealth.querytool;

public class Query {

    private String outputFileName;
    private String storedProcedure;

    public Query() {
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public String getStoredProcedure() {
        return storedProcedure;
    }

    public void setStoredProcedure(String storedProcedure) {
        this.storedProcedure = storedProcedure;
    }
}
