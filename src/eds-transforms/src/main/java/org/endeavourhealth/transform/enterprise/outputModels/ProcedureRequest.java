package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;

import java.util.Date;

public class ProcedureRequest extends AbstractEnterpriseCsvWriter {

    public ProcedureRequest(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
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
                            Long snomedConceptId,
                            int procedureRequestStatusId,
                            String originalCode,
                            String originalTerm) throws Exception {

        super.printRecord(OutputContainer.UPSERT,
                "" + id,
                "" + organisationId,
                "" + patientId,
                convertInt(encounterId),
                convertInt(practitionerId),
                convertDate(clinicalEffectiveDate),
                convertInt(datePrecisionId),
                convertLong(snomedConceptId),
                convertInt(procedureRequestStatusId),
                originalCode,
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
                "snomed_concept_id",
                "procedure_request_status_id",
                "original_code",
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
                Integer.TYPE,
                String.class,
                String.class
        };
    }
}
