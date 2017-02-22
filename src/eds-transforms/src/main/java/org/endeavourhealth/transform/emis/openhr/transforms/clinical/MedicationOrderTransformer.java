package org.endeavourhealth.transform.emis.openhr.transforms.clinical;

import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001HealthDomain;
import org.endeavourhealth.transform.emis.openhr.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.openhr.transforms.common.DateConverter;
import org.endeavourhealth.transform.emis.openhr.transforms.common.EventEncounterMap;
import org.endeavourhealth.common.fhir.FhirUri;
import org.hl7.fhir.instance.model.*;

public class MedicationOrderTransformer implements ClinicalResourceTransformer
{
    public MedicationOrder transform(OpenHR001HealthDomain.Event source, OpenHR001HealthDomain healthDomain, EventEncounterMap eventEncounterMap) throws TransformException
    {
        MedicationOrder target = new MedicationOrder();

        target.setId(source.getId());
        target.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_MEDICATION_ORDER));

        target.setDateWrittenElement(DateConverter.convertPartialDateTimeToDateTimeType(source.getEffectiveTime()));

        target.setPatient(ReferenceHelper.createReference(ResourceType.Patient, source.getPatient()));
        target.setPrescriber(ReferenceHelper.createReference(ResourceType.Practitioner, source.getAuthorisingUserInRole()));

        target.addDosageInstruction(getDosage(source));

        Reference encounterReference = eventEncounterMap.getEncounterReference(source.getId());

        if (encounterReference != null)
            target.setEncounter(encounterReference);

        target.setMedication(CodeConverter.convertCode(source.getCode()));

        target.setDispenseRequest(getDispenseRequest(source));

        return target;
    }

    private MedicationOrder.MedicationOrderDosageInstructionComponent getDosage(OpenHR001HealthDomain.Event source)
    {
        return new MedicationOrder.MedicationOrderDosageInstructionComponent()
                .setText(source.getMedicationIssue().getDosage());
    }

    private MedicationOrder.MedicationOrderDispenseRequestComponent getDispenseRequest(OpenHR001HealthDomain.Event source)
    {
        SimpleQuantity quantity = new SimpleQuantity();
        quantity.setValue(source.getMedicationIssue().getQuantity());
        quantity.setUnit(source.getMedicationIssue().getQuantityUnit());

        return new MedicationOrder.MedicationOrderDispenseRequestComponent()
                .setQuantity(quantity);
    }
}