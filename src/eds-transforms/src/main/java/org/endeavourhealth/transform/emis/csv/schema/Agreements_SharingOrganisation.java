package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;

import java.util.Date;
import java.util.UUID;

public class Agreements_SharingOrganisation extends AbstractCsvTransformer {

    public Agreements_SharingOrganisation(CSVParser csvReader, String dateFormat, String timeFormat) {
        super(csvReader, dateFormat, timeFormat);
    }

    public String getOrganisationGuid() {
        return super.getString(0);
    }
    public boolean getIsActivated() {
        return super.getBoolean(1);
    }
    public boolean getDisabled() {
        return super.getBoolean(2);
    }
    public boolean getDeleted() {
        return super.getBoolean(3);
    }
    public Date getLastModifiedDate() throws TransformException {
        return super.getDate(4);
    }
}
