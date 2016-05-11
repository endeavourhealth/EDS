package org.endeavourhealth.core.transform.tpp.transforms;

import com.google.common.base.Strings;
import org.endeavourhealth.core.transform.common.TransformException;
import org.endeavourhealth.core.transform.fhir.Fhir;
import org.endeavourhealth.core.transform.fhir.FhirUris;
import org.endeavourhealth.core.transform.tpp.schema.*;
import org.endeavourhealth.core.transform.tpp.schema.Address;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.Patient;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.List;

public class DemographicTransformer {

    private static final String ENGLISH_MAIN_CODE_SNOMED = "315570003"; //Main spoken language English
    private static final String ENGLISH_MAIN_CODE_CTV3 = "XaG5t";
    private static final String ENGLISH_SECOND_CODE = "161140009";
    private static final String ENGLISH_SECOND_TERM = "English as a second language";

    public static void transform(Identity tppId, Demographics tppDemographics, List<Resource> fhirResources) {


        Patient fhirPatient = new Patient();
        fhirResources.add(fhirPatient);

        transformIdentity(fhirPatient, tppId);
        transformName(fhirPatient, tppDemographics);
        transformDob(fhirPatient, tppDemographics);
        transformGender(fhirPatient, tppDemographics);
        transformMaritalStatus(fhirPatient, tppDemographics);
        transformEthnicity(fhirPatient, tppDemographics);
        transformLanguage(fhirPatient, tppDemographics);
        transformAddress(fhirPatient, tppDemographics);
        transformCommunications(fhirPatient, tppDemographics);

/**




 @XmlElement(name = "UsualGPUserName")
 protected String usualGPUserName;
 @XmlElement(name = "CareStartDate")
 @XmlSchemaType(name = "date")
 @XmlElement(name = "CareEndDate")
 @XmlSchemaType(name = "date")
 protected XMLGregorianCalendar careStartDate;
 @XmlElement(name = "RegistrationType", required = true)
 protected String registrationType;
 */

    }

    private static void transformCommunications(Patient fhirPatient, Demographics tppDemographics) {

        String homeTel = tppDemographics.getHomeTelephone();
        if (!Strings.isNullOrEmpty(homeTel)) {
            ContactPoint contactPoint = Fhir.createContactPoint(ContactPoint.ContactPointSystem.PHONE,
                    ContactPoint.ContactPointUse.HOME, homeTel);
            fhirPatient.addTelecom(contactPoint);
        }

        String workTel = tppDemographics.getWorkTelephone();
        if (!Strings.isNullOrEmpty(workTel)) {
            ContactPoint contactPoint = Fhir.createContactPoint(ContactPoint.ContactPointSystem.PHONE,
                    ContactPoint.ContactPointUse.WORK, workTel);
            fhirPatient.addTelecom(contactPoint);
        }

        String mobTel = tppDemographics.getMobileTelephone();
        if (!Strings.isNullOrEmpty(mobTel)) {
            ContactPoint contactPoint = Fhir.createContactPoint(ContactPoint.ContactPointSystem.PHONE,
                    ContactPoint.ContactPointUse.MOBILE, mobTel);
            fhirPatient.addTelecom(contactPoint);
        }

        String altTel = tppDemographics.getAlternateTelephone();
        if (!Strings.isNullOrEmpty(altTel)) {
            //treat alternative number as a second home number
            ContactPoint contactPoint = Fhir.createContactPoint(ContactPoint.ContactPointSystem.PHONE,
                    ContactPoint.ContactPointUse.HOME, altTel);
            fhirPatient.addTelecom(contactPoint);
        }

        String email = tppDemographics.getEmailAddress();
        if (!Strings.isNullOrEmpty(email)) {
            //assume the email address is a home email, rather than work
            ContactPoint contactPoint = Fhir.createContactPoint(ContactPoint.ContactPointSystem.EMAIL,
                    ContactPoint.ContactPointUse.HOME, email);
            fhirPatient.addTelecom(contactPoint);
        }

        //TODO - store SMS consent in FHIR
    }

    private static void transformAddress(Patient fhirPatient, Demographics tppDemographics) {
        Address tppAddress = tppDemographics.getAddress();
        org.hl7.fhir.instance.model.Address fhirAddress = AddressTransformer.tranformHomeAddress(tppAddress);
        fhirPatient.addAddress(fhirAddress);
    }

    private static void transformLanguage(Patient fhirPatient, Demographics tppDemographics) {

        Code tppCode = tppDemographics.getMainLanguage();
        if (tppCode != null) {
            CodeableConcept fhirConcept = CodeTransformer.transform(tppCode);
            Patient.PatientCommunicationComponent fhirCommunication = fhirPatient.addCommunication();
            fhirCommunication.setLanguage(fhirConcept);
            fhirCommunication.setPreferred(true); //if it's the MAIN language, then assume it's preferred
        }

        EnglishSpeaker tppEnglish = tppDemographics.getEnglishSpeaker();
        if (tppEnglish == EnglishSpeaker.YES) {
            //if an english speaker, ensure the English code is in the communication part of the FHIR resource
            //unless their main language is already recorded as English
            if (tppCode == null
                || (tppCode.getScheme() == CodeScheme.CTV_3 && !tppCode.getCode().equals(ENGLISH_MAIN_CODE_CTV3))
                || (tppCode.getScheme() == CodeScheme.SNOMED && !tppCode.getCode().equals(ENGLISH_MAIN_CODE_SNOMED))) {

                CodeableConcept fhirConcept = Fhir.createCodeableConcept(FhirUris.CODE_SYSTEM_SNOMED_CT, ENGLISH_SECOND_CODE, ENGLISH_SECOND_TERM);
                Patient.PatientCommunicationComponent fhirCommunication = fhirPatient.addCommunication();
                fhirCommunication.setLanguage(fhirConcept);
            }

        } else if (tppEnglish == EnglishSpeaker.NO
                || tppEnglish == EnglishSpeaker.UNKNOWN){
            //if not an English speaker or unkown, then don't add any data to the FHIR resource
        } else {
            throw new TransformException("Unsupported value for English speaker " + tppEnglish);
        }
    }

    private static void transformEthnicity(Patient fhirPatient, Demographics tppDemographics) {

        //TODO - finish ethnicity
    }


    private static void transformMaritalStatus(Patient fhirPatient, Demographics tppDemographics) {
        Code tppCode = tppDemographics.getMaritalStatus();
        if (tppCode != null) {
            CodeableConcept fhirConcept = CodeTransformer.transform(tppCode);
            fhirPatient.setMaritalStatus(fhirConcept);
        }
    }

    private static void transformGender(Patient fhirPatient, Demographics tppDemographics) {

        //TPP doesn't distinguish between gender and sex, and FHIR only supports gender, so just copy sex->gender
        Sex tppSex = tppDemographics.getSex();
        fhirPatient.setGender(SexTransformer.transform(tppSex));
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

        HumanName fhirName = Fhir.createHumanName(HumanName.NameUse.OFFICIAL, title, firstName, middleNames, surname);
        fhirPatient.addName(fhirName);

        if (knownAs != null
                && !knownAs.equalsIgnoreCase(firstName)
                && !knownAs.equalsIgnoreCase(title + " " + firstName + " " + surname)) {
            fhirName = Fhir.createHumanName(HumanName.NameUse.NICKNAME, null, knownAs, null, surname);
            fhirPatient.addName(fhirName);
        }
    }

    private static void transformIdentity(Patient fhirPatient, Identity tppId) {

        //NHS number OR psudeo number will be provided
        String nhsNumber = tppId.getNHSNumber();
        if (nhsNumber != null) {
            Identifier fhirIdentifier = Fhir.createIdentifier(Identifier.IdentifierUse.OFFICIAL, nhsNumber, FhirUris.IDENTIFIER_SYSTEM_NHSNUMBER);
            fhirPatient.addIdentifier(fhirIdentifier);
        } else {
            //the pseudo number is unique to TPP only, so no point adding to FHIR
            //String pseudoNumber = tppId.getPseudoNumber();
        }

    }
}
