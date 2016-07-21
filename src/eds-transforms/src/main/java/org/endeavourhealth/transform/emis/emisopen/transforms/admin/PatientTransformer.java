package org.endeavourhealth.transform.emis.emisopen.transforms.admin;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.RegistrationType;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.hl7.fhir.instance.model.*;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.SexConverter;

import java.util.ArrayList;
import java.util.List;

public class PatientTransformer
{
    public static Patient transform(MedicalRecordType medicalRecord, String organisationGuid) throws TransformException
    {
        RegistrationType source = medicalRecord.getRegistration();

        if (source == null)
            throw new TransformException("Registration element is null");

        Patient target = new Patient();

        if (StringUtils.isBlank(source.getGUID()))
            throw new TransformException("Patient GUID is empty");

        target.setId(source.getGUID());

        for (Identifier identifier : transformIdentifiers(source))
            target.addIdentifier(identifier);

        List<HumanName> names = NameConverter.convert(
                source.getTitle(),
                source.getFirstNames(),
                source.getFamilyName(),
                source.getCallingName(),
                "",
                source.getPreviousNames());

        for (HumanName name : names)
            target.addName(name);

        target.setGender(SexConverter.convertSex(source.getSex()));

        target.setBirthDate(DateConverter.getDate(source.getDateOfBirth()));

        if (source.getDateOfDeath() != null)
            target.setDeceased(new DateTimeType(DateConverter.getDate(source.getDateOfDeath())));

        if (source.getAddress() != null)
            target.addAddress(org.endeavourhealth.transform.emis.emisopen.transforms.common.AddressConverter.convert(source.getAddress(), Address.AddressUse.HOME));

        for (ContactPoint contactPoint : createContacts(source))
            target.addTelecom(contactPoint);

        target.setManagingOrganization(ReferenceHelper.createReference(ResourceType.Organization, organisationGuid));

        return target;
    }

    private static List<Identifier> transformIdentifiers(RegistrationType registrationType)
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
    }
}
