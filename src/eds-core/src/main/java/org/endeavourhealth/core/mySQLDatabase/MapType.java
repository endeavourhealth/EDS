package org.endeavourhealth.core.mySQLDatabase;


public enum MapType {
    SERVICE((short)0),
    ORGANISATION((short)1),
    REGION((short)2),
    DATASHARINGAGREEMENT((short)3),
    DATAFLOW((short)4),
    DATAPROCESSINGAGREEMENT((short)5),
    COHORT((short)6);

    private Short mapType;

    MapType(short mapType) {
        this.mapType = mapType;
    }

    public Short getMapType() {
        return mapType;
    }
}
