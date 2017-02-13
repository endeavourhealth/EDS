package org.endeavourhealth.transform.emis.emisopen.transforms.admin;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.RegistrationType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.SexConverter;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.schema.EthnicCategory;
import org.endeavourhealth.transform.fhir.schema.MaritalStatus;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class PatientTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(PatientTransformer.class);

    public static void transform(MedicalRecordType medicalRecord, List<Resource> resources, String organisationGuid, String patientGuid) throws TransformException
    {
        RegistrationType source = medicalRecord.getRegistration();

        if (source == null)
            throw new TransformException("Registration element is null");

        Patient target = new Patient();
        target.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PATIENT));

        EmisOpenHelper.setUniqueId(target, patientGuid, null);

        transformIdentifiers(target, source);


        List<HumanName> names = NameConverter.convert(
                source.getTitle(),
                source.getFirstNames(),
                source.getFamilyName(),
                source.getCallingName(),
                "",
                source.getPreviousNames());

        for (HumanName name : names) {
            target.addName(name);
        }

        target.setGender(SexConverter.convertSex(source.getSex()));

        target.setBirthDate(DateConverter.getDate(source.getDateOfBirth()));

        if (source.getDateOfDeath() != null)
            target.setDeceased(new DateTimeType(DateConverter.getDate(source.getDateOfDeath())));

        if (source.getAddress() != null) {
            target.addAddress(org.endeavourhealth.transform.emis.emisopen.transforms.common.AddressConverter.convert(source.getAddress(), Address.AddressUse.HOME));
        }

        String residentialInstituteCode = source.getResidentialInstitute();
        if (!Strings.isNullOrEmpty(residentialInstituteCode)) {
            target.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PATIENT_RESIDENTIAL_INSTITUTE_CODE, new StringType(residentialInstituteCode)));
        }

        createContacts(target, source);

        target.setManagingOrganization(EmisOpenHelper.createOrganisationReference(organisationGuid));

        if (source.getUsualGpID() != null) {
            String usualGpGuid = source.getUsualGpID().getGUID();
            if (!Strings.isNullOrEmpty(usualGpGuid)) {
                target.addCareProvider(EmisOpenHelper.createPractitionerReference(usualGpGuid));
            }
        }

        String ethnicCode = source.getEthnicCode();
        if (!Strings.isNullOrEmpty(ethnicCode)) {
            CodeableConcept codeableConcept = null;
            try {
                EthnicCategory fhirEthnicity = EthnicCategory.fromCode(ethnicCode);
                codeableConcept = CodeableConceptHelper.createCodeableConcept(fhirEthnicity);
            } catch (IllegalArgumentException ex) {
                LOG.info("Unmapped ethnic code " + ethnicCode);
                codeableConcept = CodeableConceptHelper.createCodeableConcept(ethnicCode);
            }
            target.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PATIENT_ETHNICITY, codeableConcept));
        }

        String maritalStatus = source.getMaritalStatus();
        if (!Strings.isNullOrEmpty(maritalStatus)) {
            CodeableConcept codeableConcept = null;
            try {
                MaritalStatus fhirMaritalStatus = MaritalStatus.fromCode(ethnicCode);
                codeableConcept = CodeableConceptHelper.createCodeableConcept(fhirMaritalStatus);
            } catch (IllegalArgumentException ex) {
                LOG.info("Unmapped marital status " + maritalStatus);
                codeableConcept = CodeableConceptHelper.createCodeableConcept(maritalStatus);
            }
            target.setMaritalStatus(codeableConcept);
        }

        Date dateAdded = DateConverter.getDate(source.getDateAdded());
        Date deducted = DateConverter.getDate(source.getDeductedDate());
        Period fhirPeriod = PeriodHelper.createPeriod(dateAdded, deducted);

        boolean active = PeriodHelper.isActive(fhirPeriod);
        target.setActive(active);

        resources.add(target);
    }

    private static void transformIdentifiers(Patient fhirPatient, RegistrationType registrationType) {

        fhirPatient.addIdentifier(IdentifierHelper.createNhsNumberIdentifier(registrationType.getNhsNumber()));

        fhirPatient.addIdentifier(IdentifierHelper.createIdentifier(Identifier.IdentifierUse.SECONDARY, FhirUri.IDENTIFIER_SYSTEM_EMIS_PATIENT_GUID, registrationType.getGUID()));
        fhirPatient.addIdentifier(IdentifierHelper.createIdentifier(Identifier.IdentifierUse.SECONDARY, FhirUri.IDENTIFIER_SYSTEM_CHINUMBER, registrationType.getCHINumber()));
    }

    private static void createContacts(Patient fhirPatient, RegistrationType registration) throws TransformException {

        ContactPoint contactPoint = ContactPointHelper.create(ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.HOME, registration.getHomeTelephone());
        fhirPatient.addTelecom(contactPoint);

        contactPoint = ContactPointHelper.create(ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.WORK, registration.getWorkTelephone());
        fhirPatient.addTelecom(contactPoint);

        contactPoint = ContactPointHelper.create(ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.MOBILE, registration.getMobile());
        fhirPatient.addTelecom(contactPoint);

        contactPoint = ContactPointHelper.create(ContactPoint.ContactPointSystem.EMAIL, ContactPoint.ContactPointUse.HOME, registration.getEmail());
        fhirPatient.addTelecom(contactPoint);

    }

    /*private static List<Identifier> transformIdentifiers(RegistrationType registrationType)
    {
        List<Identifier> identifiers = new ArrayList<>();

        if (StringUtils.isNotBlank(registrationType.getNhsNumber()))
        {
            Identifier identifier = new Identifier()
                    .setSystem(FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER)
                    .setValue(registrationType.getNhsNumber());

            identifiers.add(identifier);
        }

        if (StringUtils.isNotBlank(registrationType.getCHINumber()))
        {
            Identifier identifier = new Identifier()
                    .setSystem(FhirUri.IDENTIFIER_SYSTEM_CHINUMBER)
                    .setValue(registrationType.getCHINumber());

            identifiers.add(identifier);
        }

        return identifiers;
    }

    private static List<ContactPoint> createContacts(RegistrationType registration) throws TransformException
    {
        List<ContactPoint> contactPoints = new ArrayList<>();

        if (StringUtils.isNotBlank(registration.getHomeTelephone()))
            contactPoints.add(ContactPointCreater.create(registration.getHomeTelephone(),  ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.HOME));

        if (StringUtils.isNotBlank(registration.getWorkTelephone()))
            contactPoints.add(ContactPointCreater.create(registration.getWorkTelephone(),  ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.WORK));

        if (StringUtils.isNotBlank(registration.getMobile()))
            contactPoints.add(ContactPointCreater.create(registration.getMobile(),  ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.MOBILE));

        if (StringUtils.isNotBlank(registration.getEmail()))
            contactPoints.add(ContactPointCreater.create(registration.getEmail(),  ContactPoint.ContactPointSystem.EMAIL, ContactPoint.ContactPointUse.HOME));

        return contactPoints;
    }*/
}
