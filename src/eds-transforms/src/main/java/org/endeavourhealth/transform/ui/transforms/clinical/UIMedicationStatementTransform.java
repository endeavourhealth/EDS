package org.endeavourhealth.transform.ui.transforms.clinical;

import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.transform.common.exceptions.TransformRuntimeException;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.ui.helpers.*;
import org.endeavourhealth.transform.ui.helpers.ExtensionHelper;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIMedication;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIMedicationStatement;
import org.endeavourhealth.transform.ui.models.types.UICode;
import org.endeavourhealth.transform.ui.models.types.UIDate;
import org.endeavourhealth.transform.ui.models.types.UIQuantity;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class UIMedicationStatementTransform extends UIClinicalTransform<MedicationStatement, UIMedicationStatement> {
    @Override
    public List<UIMedicationStatement> transform(List<MedicationStatement> resources, ReferencedResources referencedResources) {
        return resources
                .stream()
                .map(t -> transform(t, referencedResources))
                .collect(Collectors.toList());
    }

    private UIMedicationStatement transform(MedicationStatement medicationStatement, ReferencedResources referencedResources) {
        return new UIMedicationStatement()
            .setId(medicationStatement.getId())
			.setDateAuthorised(getDateAsserted(medicationStatement)) //the asserted date is more relevant
            //.setDateAuthorised(getRecordedDateExtensionValue(medicationStatement))
            .setPrescriber(getRecordedByExtensionValue(medicationStatement, referencedResources))
            .setMedication(getMedication(medicationStatement, referencedResources))
						.setDosage(getDosage(medicationStatement))
						.setStatus(medicationStatement.getStatus().getDisplay())
						.setAuthorisedQuantity(getAuthorisedQty(medicationStatement))
						.setRepeatsAllowed(getRepeatsAllowed(medicationStatement))
						.setRepeatsIssued(getRepeatsIssued(medicationStatement))
						.setMostRecentIssue(getMostRecentIssue(medicationStatement))
						.setAuthorisationType(getAuthorisationType(medicationStatement));
    }

	private static UIDate getDateAsserted(MedicationStatement medicationStatement) {
		Date asserted = medicationStatement.getDateAsserted();
		return DateHelper.convert(asserted);
	}

	@Override
    public List<Reference> getReferences(List<MedicationStatement> resources) {
        try {
					return StreamExtension.concat(
							resources
									.stream()
									.filter(t -> hasMedicationReference(t))
									.map(t -> getMedicationReference(t)),
							resources
									.stream()
									.map(t -> getRecordedByExtensionValue(t))
									.filter(t -> (t != null)))
							.collect(Collectors.toList());
				} catch (Exception e) {
            throw new TransformRuntimeException(e);
        }
    }

    private static boolean hasMedicationReference(MedicationStatement medicationStatement) {
    	try {
    		return medicationStatement.hasMedicationReference();
			}
			catch (Exception e) {
    		return false;
			}
		}

		private static Reference getMedicationReference(MedicationStatement medicationStatement) {
    	try {
    		return medicationStatement.getMedicationReference();
			}
			catch (Exception e) {
    		return null;
			}
		}

    private static UIMedication getMedication(MedicationStatement medicationStatement, ReferencedResources referencedResources) {
        try {
            if (medicationStatement.hasMedicationCodeableConcept())
                return new UIMedication()
										.setCode(CodeHelper.convert(medicationStatement.getMedicationCodeableConcept()));
            if (medicationStatement.hasMedicationReference())
                return referencedResources.getUIMedication(medicationStatement.getMedicationReference());
            return null;
        } catch (Exception e) {
            return null;
        }
    }

		private static String getDosage(MedicationStatement medicationStatement) {
    	if (medicationStatement.hasDosage() && medicationStatement.getDosage().size() > 0)
    		return medicationStatement.getDosage().get(0).getText();

    	return null;
		}

		private static UIQuantity getAuthorisedQty(MedicationStatement medicationStatement) {
			Quantity quantity = ExtensionHelper.getExtensionValue(medicationStatement, FhirExtensionUri.MEDICATION_AUTHORISATION_QUANTITY, Quantity.class);
			return QuantityHelper.convert(quantity);
		}

		private static Integer getRepeatsAllowed(MedicationStatement medicationStatement) {
			IntegerType intType = ExtensionHelper.getExtensionValue(medicationStatement, FhirExtensionUri.MEDICATION_AUTHORISATION_NUMBER_OF_REPEATS_ALLOWED, IntegerType.class);
			if (intType != null)
				return intType.getValue();
			return null;
		}

	private static Integer getRepeatsIssued(MedicationStatement medicationStatement) {
		IntegerType intType = ExtensionHelper.getExtensionValue(medicationStatement, FhirExtensionUri.MEDICATION_AUTHORISATION_NUMBER_OF_REPEATS_ISSUED, IntegerType.class);
		if (intType != null)
			return intType.getValue();
		return null;
	}

	private static UIDate getMostRecentIssue(MedicationStatement medicationStatement) {
		//adding support for both date and datetime types, which have been used
		try {
			//DateType is the correct value type
			DateType issueDate = ExtensionHelper.getExtensionValue(medicationStatement, FhirExtensionUri.MEDICATION_AUTHORISATION_MOST_RECENT_ISSUE_DATE, DateType.class);
			return DateHelper.convert(issueDate);

		} catch (TransformRuntimeException ex) {
			//but there are some records that incorrectly have a DateTimeType
			DateTimeType issueDateTime = ExtensionHelper.getExtensionValue(medicationStatement, FhirExtensionUri.MEDICATION_AUTHORISATION_MOST_RECENT_ISSUE_DATE, DateTimeType.class);
			return DateHelper.convert(issueDateTime);
		}
    	/*DateTimeType issueDate = ExtensionHelper.getExtensionValue(medicationStatement, FhirExtensionUri.MEDICATION_AUTHORISATION_MOST_RECENT_ISSUE_DATE, DateTimeType.class);
    	return DateHelper.convert(issueDate);*/
	}

	private static UICode getAuthorisationType(MedicationStatement medicationStatement) {
    	Coding authType = ExtensionHelper.getExtensionValue(medicationStatement, FhirExtensionUri.MEDICATION_AUTHORISATION_TYPE, Coding.class);

		//the EMIS Open transformer doesn't set this
		if (authType == null) {
			return new UICode()
					.setCode("")
					.setDisplay("")
					.setSystem("");
		}

    	return CodeHelper.convert(authType);
	}
}
