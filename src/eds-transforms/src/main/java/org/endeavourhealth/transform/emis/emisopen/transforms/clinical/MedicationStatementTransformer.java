package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicationListType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicationType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public final class MedicationStatementTransformer
{
    public static void transform(MedicalRecordType medicalRecordType, List<Resource> resources) throws TransformException {

        MedicationListType medicationList = medicalRecordType.getMedicationList();
        if (medicationList == null) {
            return;
        }

        for (MedicationType medicationType : medicationList.getMedication()) {
            resources.add(transform(medicationType, medicalRecordType.getRegistration().getGUID()));
        }

    }

    private static MedicationStatement transform(MedicationType medicationType, String patientUuid) throws TransformException
    {
        MedicationStatement medicationStatement = new MedicationStatement();
        medicationStatement.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_MEDICATION_AUTHORISATION));

        medicationStatement.setId(medicationType.getGUID());
        medicationStatement.setPatient(ReferenceHelper.createReference(ResourceType.Patient, patientUuid));

        medicationStatement.setInformationSource(ReferenceHelper.createReference(ResourceType.Practitioner, medicationType.getAuthorisedUserID().getGUID()));
        medicationStatement.setDateAsserted(DateConverter.getDate(medicationType.getAssignedDate()));

        medicationStatement.setMedication(CodeConverter.convert(medicationType.getDrug().getPreparationID()));

        medicationStatement.addDosage(getDosage(medicationType));

        medicationStatement.addExtension(getQuantityExtension(medicationType));

        if (medicationType.getAuthorisedIssue() != null)
            medicationStatement.addExtension(getNumberOfRepeatsAllowedExtension(medicationType.getAuthorisedIssue()));

        if (medicationType.getIssueCount() != null)
            medicationStatement.addExtension(getNumberOfRepeatsIssuedExtension(medicationType.getIssueCount()));

        if (StringUtils.isNotBlank(medicationType.getPharmacyText()))
            medicationStatement.addExtension(getPharmacyTextExtension(medicationType.getPharmacyText()));

        if (StringUtils.isNotBlank(medicationType.getDateLastIssue()))
            medicationStatement.addExtension(getMostRecentIssueDateExtension(medicationType.getDateLastIssue()));

        if (medicationType.getContraceptiveIssue() != null)
            medicationStatement.addExtension(getPrescribedAsContraceptionExtension(medicationType.getContraceptiveIssue()));

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
                        .setUrl(FhirExtensionUri.QUANTITY_FREE_TEXT)
                        .setValue(new StringType(medicationType.getQuantityRepresentation())));

        return new Extension()
                .setUrl(FhirExtensionUri.MEDICATION_AUTHORISATION_QUANTITY)
                .setValue(simpleQuantity);
    }

    private static Extension getNumberOfRepeatsAllowedExtension(BigInteger authorisedIssue)
    {
        return new Extension()
                .setUrl(FhirExtensionUri.MEDICATION_AUTHORISATION_NUMBER_OF_REPEATS_ALLOWED)
                .setValue(new IntegerType(authorisedIssue.intValue()));
    }

    private static Extension getNumberOfRepeatsIssuedExtension(BigInteger issueCount)
    {
        return new Extension()
                .setUrl(FhirExtensionUri.MEDICATION_AUTHORISATION_NUMBER_OF_REPEATS_ISSUED)
                .setValue(new IntegerType(issueCount.intValue()));
    }

    private static Extension getPharmacyTextExtension(String pharmacyText)
    {
        return new Extension()
                .setUrl(FhirExtensionUri.PHARMACY_TEXT)
                .setValue(new StringType(pharmacyText));
    }

    private static Extension getMostRecentIssueDateExtension(String dateLastIssue) throws TransformException
    {
        return new Extension()
                .setUrl(FhirExtensionUri.MEDICATION_AUTHORISATION_MOST_RECENT_ISSUE_DATE)
                .setValue(new DateType(DateConverter.getDate(dateLastIssue)));
    }

    private static Extension getPrescribedAsContraceptionExtension(BigInteger contraceptiveIssue)
    {
        return new Extension()
                .setUrl(FhirExtensionUri.PRESCRIBED_AS_CONTRACEPTION)
                .setValue(new BooleanType((!contraceptiveIssue.equals(0))));
    }
}
