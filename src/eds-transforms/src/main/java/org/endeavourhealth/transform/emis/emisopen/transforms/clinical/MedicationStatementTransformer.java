package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicationType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MedicationStatementTransformer
{
    public static List<Resource> transform(MedicalRecordType medicalRecordType) throws TransformException
    {
        List<Resource> resource = new ArrayList<>();

        for (MedicationType medicationType : medicalRecordType.getMedicationList().getMedication())
            resource.add(transform(medicationType, medicalRecordType.getRegistration().getGUID()));

        return resource;
    }

    private static MedicationStatement transform(MedicationType medicationType, String patientUuid) throws TransformException
    {
        MedicationStatement medicationStatement = new MedicationStatement();
        medicationStatement.setId(medicationType.getGUID());
        medicationStatement.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_MEDICATION_AUTHORISATION));

        medicationStatement.setPatient(ReferenceHelper.createReference(ResourceType.Patient, patientUuid));

        medicationStatement.setInformationSource(ReferenceHelper.createReference(ResourceType.Practitioner, medicationType.getAuthorisedUserID().getGUID()));
        medicationStatement.setDateAsserted(DateConverter.getDate(medicationType.getAssignedDate()));

        medicationStatement.setMedication(CodeConverter.convert(medicationType.getDrug().getPreparationID()));

        medicationStatement.addDosage(getDosage(medicationType));

        medicationStatement.addExtension(getQuantityExtension(medicationType));

        return medicationStatement;
    }

    private static MedicationStatement.MedicationStatementDosageComponent getDosage(MedicationType medicationType)
    {
        return new MedicationStatement.MedicationStatementDosageComponent()
                .setText(medicationType.getDosage());
    }

    private static Extension getQuantityExtension(MedicationType medicationType)
    {
        SimpleQuantity simpleQuantity = (SimpleQuantity)new SimpleQuantity()
                .setValue(BigDecimal.valueOf(medicationType.getQuantity()))
                .setUnit(medicationType.getQuantityUnits())
                .addExtension(new Extension()
                        .setUrl(FhirExtensionUri.MEDICATION_QUANTITY_TEXT)
                        .setValue(new StringType(medicationType.getQuantityRepresentation())));

        return new Extension()
                .setUrl(FhirExtensionUri.MEDICATION_AUTHORISATION_QUANTITY)
                .setValue(simpleQuantity);
    }
}
