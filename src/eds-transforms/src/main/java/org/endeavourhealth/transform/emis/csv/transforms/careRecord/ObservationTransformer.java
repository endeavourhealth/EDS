package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Appointment_Slot;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Observation;
import org.endeavourhealth.transform.emis.csv.transforms.coding.Metadata;
import org.hl7.fhir.instance.model.*;

import java.util.List;
import java.util.Map;

public class ObservationTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, Metadata metadata, Map<String, List<Resource>> fhirResources) throws Exception {

        CareRecord_Observation parser = new CareRecord_Observation(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createResource(parser, fhirResources);
            }
        } finally {
            parser.close();
        }
    }

    private static void createResource(CareRecord_Observation observationParser, Map<String, List<Resource>> fhirResources) throws Exception {

        //since we're not processing deltas, just ignore deleted obs
        if (observationParser.getDeleted()) {
            return;
        }

        //do not store confidential data in EDS
        if (observationParser.getIsConfidential()) {
            return;
        }

        String type = observationParser.getObservationType();


        /**
         *
         public UUID getObservationGuid() {
         return super.getUniqueIdentifier(0);
         }
         public UUID getParentOvercastionGuid() {
         return super.getUniqueIdentifier(1);
         }
         public UUID getPatientGuid() {
         return super.getUniqueIdentifier(2);
         }
         public UUID getOrganisationGuid() {
         return super.getUniqueIdentifier(3);
         }
         public Date getEffectiveDate() throws TransformException {
         return super.getDate(4);
         }
         public String getEffectiveDatePrecision() {
         return super.getString(5);
         }
         public Date getEnteredDateTime() throws TransformException {
         return super.getDateTime(6, 7);
         }
         public UUID getClinicianUserInRoleGuid() {
         return super.getUniqueIdentifier(8);
         }
         public UUID getEnteredByUserInRoleGuid() {
         return super.getUniqueIdentifier(9);
         }
         public Long getCodeId() {
         return super.getLong(10);
         }
         public UUID getProblemUGuid() {
         return super.getUniqueIdentifier(11);
         }
         public UUID getConsultationGuid() {
         return super.getUniqueIdentifier(12);
         }
         public Double getValue() {
         return super.getDouble(13);
         }
         public Double getNumericRangeLow() {
         return super.getDouble(14);
         }
         public Double getNumericRangeHigh() {
         return super.getDouble(15);
         }
         public String getNumericUnit() {
         return super.getString(16);
         }
         public String getObservationType() {
         return super.getString(17);
         }
         public String getAssociatedText() {
         return super.getString(18);
         }
         public UUID getDocumentGuid() {
         return super.getUniqueIdentifier(21);
         }
         */
    }

   /* private static FamilyMemberHistory createFamilyMemberHistory(CareRecord_Observation observationParser) throws Exception {

    }

    private static AllergyIntolerance createAllergyIntolerace(CareRecord_Observation observationParser) throws Exception {

    }


    private static Condition createCondition(CareRecord_Observation observationParser) throws Exception {

    }

    private static Observation createObservation(CareRecord_Observation observationParser) throws Exception {

    }

    private static Immunization createImmunuzation(CareRecord_Observation observationParser) throws Exception {

    }*/
}
