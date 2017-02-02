package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;

import java.math.BigDecimal;
import java.util.Date;

public class MedicationOrder extends AbstractEnterpriseCsvWriter {

    public MedicationOrder(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
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
                            String dose,
                            BigDecimal quantityValue,
                            String quantityUnit,
                            Integer durationDays,
                            BigDecimal estimatedCost,
                            Integer medicationStatementId,
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
                dose,
                convertBigDecimal(quantityValue),
                quantityUnit,
                convertInt(durationDays),
                convertBigDecimal(estimatedCost),
                convertInt(medicationStatementId),
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
                Integer.TYPE,
                Integer.TYPE,
                Integer.TYPE,
                Integer.class,
                Integer.class,
                Date.class,
                Integer.class,
                Long.class,
                String.class,
                BigDecimal.class,
                String.class,
                Integer.class,
                BigDecimal.class,
                Integer.class,
                String.class
        };
    }
}
