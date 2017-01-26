package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;

import java.util.Date;

public class MedicationStatement extends AbstractEnterpriseCsvWriter {

    public MedicationStatement(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
        super(fileName, csvFormat, dateFormat, timeFormat);
    }

    public void writeDelete(int id) throws Exception {

        super.printRecord(OutputContainer.DELETE,
                "" + id);
    }

    public void writeUpsert(int id,
                            int organisationId,
                            int patientId,
                            Integer encounterId,
                            Integer practitionerId,
                            Date clinicalEffectiveDate,
                            Integer datePrecisionId,
                            Long dmdId,
                            Boolean isActive,
                            Date cancellationDate,
                            String dose,
                            Double quantityValue,
                            String quantityUnit,
                            int authorisationTypeId,
                            String originalTerm) throws Exception {

        super.printRecord(OutputContainer.UPSERT,
                "" + id,
                "" + organisationId,
                "" + patientId,
                convertInt(encounterId),
                convertInt(practitionerId),
                convertDate(clinicalEffectiveDate),
                convertInt(datePrecisionId),
                convertLong(dmdId),
                convertBoolean(isActive),
                convertDate(cancellationDate),
                dose,
                convertDouble(quantityValue),
                quantityUnit,
                "" + authorisationTypeId,
                originalTerm);
    }

    @Override
    public String[] getCsvHeaders() {
        return new String[] {
                "save_mode",
                "id",
                "organization_id",
                "patient_id",
                "encounter_id",
                "practitioner_id",
                "clinical_effective_date",
                "date_precision_id",
                "dmd_id",
                "is_active",
                "cancellation_date",
                "dose",
                "quantity_value",
                "quantity_unit",
                "medication_statement_authorisation_type_id",
                "original_term"
        };
    }

    @Override
    public Class[] getColumnTypes() {
        return new Class[] {
                String.class,
                Integer.TYPE,
                Integer.TYPE,
                Integer.TYPE,
                Integer.class,
                Integer.class,
                Date.class,
                Integer.class,
                Long.class,
                Boolean.class,
                Date.class,
                String.class,
                Double.class,
                String.class,
                Integer.TYPE,
                String.class
        };
    }
}
