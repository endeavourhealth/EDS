package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.IssueType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MedicationOrderTransformer
{
    public static List<Resource> transform(MedicalRecordType medicalRecordType) throws TransformException
    {
        List<Resource> resource = new ArrayList<>();

        for (IssueType issueType : medicalRecordType.getIssueList().getIssue())
            resource.add(transform(issueType, medicalRecordType.getRegistration().getGUID()));

        return resource;
    }

    private static MedicationOrder transform(IssueType issueType, String patientUuid) throws TransformException
    {
        MedicationOrder medicationOrder = new MedicationOrder();
        medicationOrder.setId(issueType.getGUID());
        medicationOrder.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_MEDICATION_ORDER));

        medicationOrder.setPatient(ReferenceHelper.createReference(ResourceType.Patient, patientUuid));
        medicationOrder.setPrescriber(ReferenceHelper.createReference(ResourceType.Practitioner, issueType.getAuthorisedUserID().getGUID()));

        medicationOrder.setDateWritten(DateConverter.getDate(issueType.getAssignedDate()));
        medicationOrder.addDosageInstruction(getDosage(issueType));
        medicationOrder.setDispenseRequest(getDispenseRequest(issueType));
        medicationOrder.setMedication(CodeConverter.convert(issueType.getDrug().getPreparationID()));

        if (StringUtils.isNotBlank(issueType.getPharmacyText()))
        {
            medicationOrder.addExtension(new Extension()
                    .setUrl(FhirExtensionUri.PHARMACY_TEXT)
                    .setValue(new StringType(issueType.getPharmacyText())));
        }

        if (issueType.getEstimatedCost() != null)
        {
            medicationOrder.addExtension(new Extension()
                .setUrl(FhirExtensionUri.MEDICATION_AUTHORISATION_QUANTITY)
                .setValue(new SimpleQuantity().setValue(BigDecimal.valueOf(issueType.getEstimatedCost()))));
        }

        return medicationOrder;
    }

    private static MedicationOrder.MedicationOrderDosageInstructionComponent getDosage(IssueType issueType)
    {
        return new MedicationOrder.MedicationOrderDosageInstructionComponent()
                .setText(issueType.getDosage());
    }

    private static MedicationOrder.MedicationOrderDispenseRequestComponent getDispenseRequest(IssueType issueType)
    {
        SimpleQuantity simpleQuantity = (SimpleQuantity)new SimpleQuantity()
                .setValue(BigDecimal.valueOf(issueType.getQuantity()))
                .setUnit(issueType.getQuantityUnits())
                .addExtension(new Extension()
                        .setUrl(FhirExtensionUri.MEDICATION_QUANTITY_TEXT)
                        .setValue(new StringType(issueType.getQuantityRepresentation())));

        return new MedicationOrder.MedicationOrderDispenseRequestComponent()
                .setQuantity(simpleQuantity);

    }
}
