package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;

import java.util.Date;

public class ReferralRequest extends AbstractEnterpriseCsvWriter {

    public ReferralRequest(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
        super(fileName, csvFormat, dateFormat, timeFormat);
    }

    public void writeDelete(int id) throws Exception {

        super.printRecord(OutputContainer.DELETE,
                "" + id);
    }

    public void writeUpsert(int id,
                            int organizationId,
                            int patientId,
                            Integer encounterId,
                            Integer practitionerId,
                            Date clinicalEffectiveDate,
                            Integer datePrecisionId,
                            Long snomedConceptId,
                            Integer requesterOrganizationId,
                            Integer recipientOrganizationId,
                            Integer priorityId,
                            Integer typeId,
                            String mode,
                            Boolean outgoing,
                            String originalCode,
                            String originalTerm) throws Exception {

        super.printRecord(OutputContainer.UPSERT,
                "" + id,
                "" + organizationId,
                "" + patientId,
                convertInt(encounterId),
                convertInt(practitionerId),
                convertDate(clinicalEffectiveDate),
                convertInt(datePrecisionId),
                convertLong(snomedConceptId),
                convertInt(requesterOrganizationId),
                convertInt(recipientOrganizationId),
                convertInt(priorityId),
                convertInt(typeId),
                mode,
                convertBoolean(outgoing),
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
                "requester_organization_id",
                "recipient_organization_id",
                "priority_id",
                "type_id",
                "mode",
                "outgoing_referral",
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
                Integer.class,
                Integer.class,
                Integer.class,
                Integer.class,
                String.class,
                Boolean.class,
                String.class,
                String.class
        };
    }
}
