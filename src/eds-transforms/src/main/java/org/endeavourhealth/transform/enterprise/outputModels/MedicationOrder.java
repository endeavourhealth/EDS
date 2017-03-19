package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;

import java.math.BigDecimal;
import java.util.Date;

public class MedicationOrder extends AbstractEnterpriseCsvWriter {

    public MedicationOrder(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
        super(fileName, csvFormat, dateFormat, timeFormat);
    }

    public void writeDelete(long id) throws Exception {

        super.printRecord(OutputContainer.DELETE,
                "" + id);
    }

    public void writeUpsert(long id,
                            long organisationId,
                            long patientId,
                            long personId,
                            Long encounterId,
                            Long practitionerId,
                            Date clinicalEffectiveDate,
                            Integer datePrecisionId,
                            Long dmdId,
                            String dose,
                            BigDecimal quantityValue,
                            String quantityUnit,
                            Integer durationDays,
                            BigDecimal estimatedCost,
                            Long medicationStatementId,
                            String originalTerm) throws Exception {

        super.printRecord(OutputContainer.UPSERT,
                "" + id,
                "" + organisationId,
                "" + patientId,
                "" + personId,
                convertLong(encounterId),
                convertLong(practitionerId),
                convertDate(clinicalEffectiveDate),
                convertInt(datePrecisionId),
                convertLong(dmdId),
                dose,
                convertBigDecimal(quantityValue),
                quantityUnit,
                convertInt(durationDays),
                convertBigDecimal(estimatedCost),
                convertLong(medicationStatementId),
                originalTerm);
    }

    @Override
    public String[] getCsvHeaders() {
        return new String[] {
                "save_mode",
                "id",
                "organization_id",
                "patient_id",
                "person_id",
                "encounter_id",
                "practitioner_id",
                "clinical_effective_date",
                "date_precision_id",
                "dmd_id",
                "dose",
                "quantity_value",
                "quantity_unit",
                "duration_days",
                "estimated_cost",
                "medication_statement_id",
                "original_term"
        };
    }

    @Override
    public Class[] getColumnTypes() {
        return new Class[] {
                String.class,
                Long.TYPE,
                Long.TYPE,
                Long.TYPE,
                Long.TYPE,
                Long.class,
                Long.class,
                Date.class,
                Integer.class,
                Long.class,
                String.class,
                BigDecimal.class,
                String.class,
                Integer.class,
                BigDecimal.class,
                Long.class,
                String.class
        };
    }
}
