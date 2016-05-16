package org.endeavourhealth.core.transform.tpp.transforms;

import com.google.common.base.Strings;
import org.endeavourhealth.core.transform.common.TransformException;
import org.endeavourhealth.core.transform.fhir.Fhir;
import org.endeavourhealth.core.transform.fhir.FhirUris;
import org.endeavourhealth.core.transform.tpp.schema.*;
import org.endeavourhealth.core.transform.tpp.schema.Medication;
import org.hl7.fhir.instance.model.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

public class MedicationTransformer {

    public static void transform(List<Medication> medications, Event tppEvent, Encounter fhirEncounter, List<Resource> fhirResources) {
        for (Medication medication: medications) {
            transform(medication, tppEvent, fhirEncounter, fhirResources);
        }
    }

    public static void transform(Medication medication, Event tppEvent, Encounter fhirEncounter, List<Resource> fhirResources) {

        MedicationOrder fhirMedicationOrder = new MedicationOrder();
        fhirMedicationOrder.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_MEDICATION_ORDER));
        fhirResources.add(fhirMedicationOrder);

        MedicationType medicationType = medication.getMedicationType();

        Drug drug = medication.getDrug();
        fhirMedicationOrder.setMedication(convertMedication(drug));

        String dose = medication.getDose();
        fhirMedicationOrder.addDosageInstruction(new MedicationOrder.MedicationOrderDosageInstructionComponent().setText(dose));

        String quantity = medication.getQuantity();

        XMLGregorianCalendar startDate = medication.getStartDate();
        fhirMedicationOrder.setDateWritten(startDate.toGregorianCalendar().getTime());

        //TODO - check that a future end date is permissable in the FHIR medicationOrder
        XMLGregorianCalendar endDate = medication.getEndDate();
        fhirMedicationOrder.setDateEnded(endDate.toGregorianCalendar().getTime());

        MedicationEndReason endReason = medication.getEndReason();
        if (endReason != null) {
            fhirMedicationOrder.setReasonEnded(Fhir.createCodeableConcept(endReason.toString()));
        }

        Medication.RepeatIssue repeatIssue = medication.getRepeatIssue();
        List<String> linkedProblemUIDs = medication.getLinkedProblemUID();

        fhirMedicationOrder.setStatus(convertStatus(endDate, endReason));

        String patientId = Fhir.findPatientId(fhirResources);
        fhirMedicationOrder.setPatient(Fhir.createReference(ResourceType.Patient, patientId));

        String userName = tppEvent.getUserName();
        if (!Strings.isNullOrEmpty(userName)) {
            fhirMedicationOrder.setPrescriber(Fhir.createReference(ResourceType.Practitioner, userName));
        }

        if (fhirEncounter != null) {
            String encounterId = fhirEncounter.getId();
            fhirMedicationOrder.setEncounter(Fhir.createReference(ResourceType.Encounter, encounterId));
        }

        fhirMedicationOrder.setMedication(convertMedication(drug));

    }

    private static Type convertMedication(Drug tppDrug) {
        String productId = tppDrug.getProductID();
        DrugScheme scheme = tppDrug.getScheme();
        String fullName = tppDrug.getFullName();
        //pack ID isn't needed for this, since the quantity describes this too
        //BigInteger packId = tppDrug.getPackID();

        if (scheme == DrugScheme.DMD) {
            return Fhir.createCodeableConcept(FhirUris.CODE_SYSTEM_SNOMED_CT, fullName, productId);
        } else if (scheme == DrugScheme.MULTILEX) {
            throw new TransformException("Cannot support Multilex drug codes");
        } else {
            throw new TransformException("Unsupported drug scheme " + scheme);
        }
    }

    private static MedicationOrder.MedicationOrderStatus convertStatus(XMLGregorianCalendar endDate, MedicationEndReason endReason) {

        //if the endDate is in the future, the medication is active
        if (endDate.toGregorianCalendar().toInstant().isAfter(Instant.now())) {
            return MedicationOrder.MedicationOrderStatus.ACTIVE;
        } else {
            //the presence of an endReason means the medication was stopped early
            if (endReason != null) {
                return MedicationOrder.MedicationOrderStatus.STOPPED;
            } else {
                return MedicationOrder.MedicationOrderStatus.COMPLETED;
            }
        }
    }
}
