package org.endeavourhealth.transform.emis.openhr.transforms;

import org.endeavourhealth.transform.common.ReferenceHelper;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001HealthDomain;
import org.endeavourhealth.transform.emis.openhr.schema.VocDrugStatus;
import org.endeavourhealth.transform.emis.openhr.transforms.converters.CodeConverter;
import org.endeavourhealth.transform.emis.openhr.transforms.helpers.EventEncounterMap;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.hl7.fhir.instance.model.*;

public class MedicationStatementTransformer implements ClinicalResourceTransformer
{
    public MedicationStatement transform(OpenHR001HealthDomain.Event source, OpenHR001HealthDomain healthDomain, EventEncounterMap eventEncounterMap) throws TransformException
    {
        MedicationStatement target = new MedicationStatement();

        target.setId(source.getId());
        target.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_MEDICATION_AUTHORISATION));

        target.setPatient(ReferenceHelper.createReference(ResourceType.Patient, source.getPatient()));

        if (source.getEnteredByUserInRole() != null)
            target.setInformationSource(ReferenceHelper.createReference(ResourceType.Practitioner, source.getEnteredByUserInRole()));

        target.setStatus(getStatus(source.getMedication().getDrugStatus()));

        target.setMedication(CodeConverter.convertCode(source.getCode()));

        return target;
    }

    private static MedicationStatement.MedicationStatementStatus getStatus(VocDrugStatus drugStatus)
    {
        switch (drugStatus)
        {
            case A: return MedicationStatement.MedicationStatementStatus.ACTIVE;
            case N: return MedicationStatement.MedicationStatementStatus.ENTEREDINERROR;
            case C:
            default: return MedicationStatement.MedicationStatementStatus.COMPLETED;
        }
    }
}
