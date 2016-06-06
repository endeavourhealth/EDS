package org.endeavourhealth.transform.emis.csv.transforms.prescribing;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Prescribing_DrugRecord;
import org.endeavourhealth.transform.emis.csv.schema.Prescribing_IssueRecord;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;
import java.util.Map;

public class IssueRecordTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, Map<String, List<Resource>> fhirResources) throws Exception {

        Prescribing_IssueRecord parser = new Prescribing_IssueRecord(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createProblem(parser, fhirResources);
            }
        } finally {
            parser.close();
        }
    }

    private static void createProblem(Prescribing_IssueRecord issueRecordParser, Map<String, List<Resource>> fhirResources) throws Exception {

        //not processing deltas, so just skip any deleted data
        if (issueRecordParser.getDeleted()) {
            return;
        }

        //EDS shouldn't be storing confidential data
        if (issueRecordParser.getIsConfidential()) {
            return;
        }


/**
 *  public UUID getIssueRecordGuid() {
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
 public Date getEffectiveDateTime() throws TransformException {
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
 public Double getQuantity() {
 return super.getDouble(10);
 }
 public Integer getCourseDurationInDays() {
 return super.getInt(11);
 }
 public Double getEstimatedNhsCost() {
 return super.getDouble(12);
 }
 public UUID getProblemObservationGuid() {
 return super.getUniqueIdentifier(13);
 }
 public String getDosage() {
 return super.getString(14);
 }
 public String getQuantityUnit() {
 return super.getString(15);
 }
 public UUID getDrugRecordGuid() {
 return super.getUniqueIdentifier(16);
 }

 */

    }
}
