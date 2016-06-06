package org.endeavourhealth.transform.emis.csv.transforms.prescribing;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Problem;
import org.endeavourhealth.transform.emis.csv.schema.Prescribing_DrugRecord;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;
import java.util.Map;

public class DrugRecordTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, Map<String, List<Resource>> fhirResources) throws Exception {

        Prescribing_DrugRecord parser = new Prescribing_DrugRecord(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createProblem(parser, fhirResources);
            }
        } finally {
            parser.close();
        }
    }

    private static void createProblem(Prescribing_DrugRecord drugRecordParser, Map<String, List<Resource>> fhirResources) throws Exception {

        //not processing deltas, so just skip any deleted data
        if (drugRecordParser.getDeleted()) {
            return;
        }

        //EDS shouldn't be storing confidential data
        if (drugRecordParser.getIsConfidential()) {
            return;
        }

        /**
         * public UUID getDrugRecordGuid() {
         return super.getUniqueIdentifier(0);
         }
         public UUID getPatientGuid() {
         return super.getUniqueIdentifier(1);
         }
         public UUID getOrganisationGuid() {
         return super.getUniqueIdentifier(2);
         }
         public Date getEffectiveDate() throws TransformException {
         return super.getDate(3);
         }
         public String getEffectiveDatePrecision() {
         return super.getString(4);
         }
         public Date getEnteredDateTime() throws TransformException {
         return super.getDateTime(5, 6);
         }
         public UUID getClinicianUserInRoleGuid() {
         return super.getUniqueIdentifier(7);
         }
         public UUID getEnteredByUserInRoleGuid() {
         return super.getUniqueIdentifier(8);
         }
         public Long getCodeId() {
         return super.getLong(9);
         }
         public String getDosage() {
         return super.getString(10);
         }
         public Double getQuantity() {
         return super.getDouble(11);
         }
         public String getQuantityUnit() {
         return super.getString(12);
         }
         public UUID getProblemObservationGuid() {
         return super.getUniqueIdentifier(13);
         }
         public String getPrescriptionType() {
         return super.getString(14);
         }
         public boolean getIsActive() {
         return super.getBoolean(15);
         }
         public Date getCancellationDate() throws TransformException {
         return super.getDate(16);
         }
         public Integer getNumberOfIssues() {
         return super.getInt(17);
         }
         public Integer getNumberOfIssuesAuthorised() {
         return super.getInt(18);
         }

         */
    }

}