package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.IssueListType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.IssueType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicationLinkType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.hl7.fhir.instance.model.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public final class IssueTransformer
{
    public static void transform(MedicalRecordType medicalRecordType, List<Resource> resources, String patientUuid) throws TransformException {

        //got patients with null issue lists
        IssueListType issueList = medicalRecordType.getIssueList();
        if (issueList == null) {
            return;
        }

        for (IssueType issueType : issueList.getIssue()) {
            resources.add(transform(issueType, patientUuid));
        }

    }

    private static MedicationOrder transform(IssueType issueType, String patientGuid) throws TransformException
    {
        MedicationOrder medicationOrder = new MedicationOrder();
        medicationOrder.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_MEDICATION_ORDER));

        EmisOpenHelper.setUniqueId(medicationOrder, patientGuid, issueType.getGUID());

        medicationOrder.setPatient(EmisOpenHelper.createPatientReference(patientGuid));
        medicationOrder.setPrescriber(EmisOpenHelper.createPractitionerReference(issueType.getAuthorisedUserID().getGUID()));

        medicationOrder.setDateWritten(DateConverter.getDate(issueType.getAssignedDate()));
        medicationOrder.addDosageInstruction(getDosage(issueType));
        medicationOrder.setDispenseRequest(getDispenseRequest(issueType));
        medicationOrder.setMedication(CodeConverter.convert(issueType.getDrug().getPreparationID()));

        if (issueType.getMedicationLink() != null)
            medicationOrder.addExtension(getMedicationOrderAuthorisationExtension(issueType.getMedicationLink()));

        if (StringUtils.isNotBlank(issueType.getPharmacyText()))
            medicationOrder.addExtension(getPharmacyTextExtension(issueType.getPharmacyText()));

        if (issueType.getEstimatedCost() != null)
            medicationOrder.addExtension(getEstimatedCostExtension(issueType.getEstimatedCost()));

        if (issueType.getContraceptiveIssue() != null)
            medicationOrder.addExtension(getPrescribedAsContraceptionExtension(issueType.getContraceptiveIssue()));

        //TODO - shouldn't this be linked to a MedicationStatement? Can't see the link in the XML though...

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
                        .setUrl(FhirExtensionUri.QUANTITY_FREE_TEXT)
                        .setValue(new StringType(issueType.getQuantityRepresentation())));

        return new MedicationOrder.MedicationOrderDispenseRequestComponent()
                .setQuantity(simpleQuantity);

    }

    private static Extension getMedicationOrderAuthorisationExtension(MedicationLinkType medicationLinkType)
    {
        return new Extension()
                .setUrl(FhirExtensionUri.MEDICATION_ORDER_AUTHORISATION)
                .setValue(new StringType(medicationLinkType.getGUID()));
    }

    private static Extension getPharmacyTextExtension(String pharmacyText)
    {
        return new Extension()
                .setUrl(FhirExtensionUri.PHARMACY_TEXT)
                .setValue(new StringType(pharmacyText));
    }

    private static Extension getEstimatedCostExtension(Double estimatedCost)
    {
        return new Extension()
            .setUrl(FhirExtensionUri.MEDICATION_AUTHORISATION_QUANTITY)
            .setValue(new SimpleQuantity().setValue(BigDecimal.valueOf(estimatedCost)));
    }

    private static Extension getPrescribedAsContraceptionExtension(BigInteger contraceptiveIssue)
    {
        return new Extension()
                .setUrl(FhirExtensionUri.PRESCRIBED_AS_CONTRACEPTION)
                .setValue(new BooleanType((!contraceptiveIssue.equals(0))));
    }
}
