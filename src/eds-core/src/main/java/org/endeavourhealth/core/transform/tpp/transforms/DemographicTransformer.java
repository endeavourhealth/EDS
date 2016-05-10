package org.endeavourhealth.core.transform.tpp.transforms;

import org.endeavourhealth.core.transform.FhirConstants;
import org.endeavourhealth.core.transform.tpp.schema.*;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.valuesets.V3MaritalStatus;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.List;

public class DemographicTransformer {

    public static void transform(Identity tppId, Demographics tppDemographics, List<Resource> fhirResources) {


        Patient fhirPatient = new Patient();
        fhirResources.add(fhirPatient);

        transformIdentity(fhirPatient, tppId);
        transformName(fhirPatient, tppDemographics);
        transformDob(fhirPatient, tppDemographics);
        transformMaritalStatus(fhirPatient, tppDemographics);
/**


 @XmlElement(name = "Ethnicity")
 protected Code ethnicity;
 @XmlElement(name = "MainLanguage")
 protected Code mainLanguage;
 @XmlElement(name = "EnglishSpeaker", required = true)
 protected String englishSpeaker;
 @XmlElement(name = "Address")
 protected Address address;
 @XmlElement(name = "HomeTelephone")
 protected String homeTelephone;
 @XmlElement(name = "WorkTelephone")
 protected String workTelephone;
 @XmlElement(name = "MobileTelephone")
 protected String mobileTelephone;
 @XmlElement(name = "AlternateTelephone")
 protected String alternateTelephone;
 @XmlElement(name = "EmailAddress")
 protected String emailAddress;
 @XmlElement(name = "SMSConsent", required = true)
 protected String smsConsent;
 @XmlElement(name = "UsualGPUserName")
 protected String usualGPUserName;
 @XmlElement(name = "CareStartDate")
 @XmlSchemaType(name = "date")
 protected XMLGregorianCalendar careStartDate;
 @XmlElement(name = "RegistrationType", required = true)
 protected String registrationType;
 */

    }

    private static void transformMaritalStatus(Patient fhirPatient, Demographics tppDemographics) {
        Code code = tppDemographics.getMaritalStatus();
        if (code != null) {

            CodeableConcept maritalStatus = new CodeableConcept();

            fhirPatient.setMaritalStatus(maritalStatus);
        }
    }

    private static void transformGender(Patient fhirPatient, Demographics tppDemographics) {

        //TPP doesn't distinguish between gender and sex, and FHIR only supports gender, so just copy sex->gender
        Sex sex = tppDemographics.getSex();
        if (sex == Sex.F) {
            fhirPatient.setGender(Enumerations.AdministrativeGender.FEMALE);
        } else if (sex == Sex.M) {
            fhirPatient.setGender(Enumerations.AdministrativeGender.MALE);
        } else {
            throw new RuntimeException("Unhandled sex value [" + sex + "]");
        }
    }

    private static void transformDob(Patient fhirPatient, Demographics tppDemographics) {

        XMLGregorianCalendar cal = tppDemographics.getDateOfBirth();
        Date dob = cal.toGregorianCalendar().getTime();

        fhirPatient.setBirthDate(dob);
    }

    private static void transformName(Patient fhirPatient, Demographics tppDemographics) {

        String title = tppDemographics.getTitle();
        String firstName = tppDemographics.getFirstName();
        String middleNames = tppDemographics.getMiddleNames();
        String surname = tppDemographics.getSurname();
        String knownAs = tppDemographics.getKnownAs();

        HumanName fhirName = fhirPatient.addName();
        fhirName.setUse(HumanName.NameUse.OFFICIAL);
        fhirName.addPrefix(title);
        fhirName.addGiven(firstName);
        fhirName.addFamily(surname);

        if (middleNames != null) {
            String[] arr = middleNames.split(" ");
            for (String s : arr) {
                fhirName.addGiven(s);
            }
        }

        if (knownAs != null) {
            HumanName fhirNickName = fhirPatient.addName();
            fhirNickName.setUse(HumanName.NameUse.NICKNAME);
            fhirNickName.addGiven(knownAs);
        }

    }

    private static void transformIdentity(Patient fhirPatient, Identity tppId) {

        Identifier fhirIdentifier = fhirPatient.addIdentifier();

        //NHS number OR psudeo number will be provided
        String nhsNumber = tppId.getNHSNumber();
        if (nhsNumber != null) {
            fhirIdentifier.setUse(Identifier.IdentifierUse.OFFICIAL);
            fhirIdentifier.setValue(nhsNumber);
            fhirIdentifier.setSystem(FhirConstants.NHS_NUMBER);
        } else {
            String pseudoNumber = tppId.getPseudoNumber();
            fhirIdentifier.setUse(Identifier.IdentifierUse.TEMP);
            fhirIdentifier.setValue(pseudoNumber);
        }
    }
}
