package org.endeavourhealth.transform.emis.emisopen.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.AddressType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.RegistrationType;
import org.endeavourhealth.transform.fhir.AddressConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.converters.DateConverter;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.endeavourhealth.transform.fhir.NameConverter;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;
import org.endeavourhealth.transform.emis.emisopen.transforms.converters.SexConverter;

import java.util.ArrayList;
import java.util.List;

public class PatientTransformer
{
    public static Patient transform(MedicalRecordType medicalRecord) throws TransformException
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

        if (target.getAddress() != null)
        {
            AddressType address = source.getAddress();

            if (address != null)
                target.addAddress(AddressConverter.createAddress(Address.AddressUse.HOME, address.getHouseNameFlat(), address.getStreet(), address.getVillage(), address.getTown(), address.getCounty(), address.getPostCode()));
        }

        for (ContactPoint contactPoint : createContacts(source))
            target.addTelecom(contactPoint);

        target.setManagingOrganization(ReferenceHelper.createReference(ResourceType.Organization, medicalRecord.getOriginator().getOrganisation().getGUID()));

        return target;
    }

    private static List<Identifier> transformIdentifiers(RegistrationType registrationType)
    {
        List<Identifier> identifiers = new ArrayList<>();

        if (StringUtils.isNotBlank(registrationType.getNhsNumber()))
        {
            Identifier identifier = new Identifier()
                    .setSystem(FhirUris.IDENTIFIER_SYSTEM_NHSNUMBER)
                    .setValue(registrationType.getNhsNumber());

            identifiers.add(identifier);
        }

        if (StringUtils.isNotBlank(registrationType.getCHINumber()))
        {
            Identifier identifier = new Identifier()
                    .setSystem(FhirUris.IDENTIFIER_SYSTEM_CHINUMBER)
                    .setValue(registrationType.getCHINumber());

            identifiers.add(identifier);
        }

        return identifiers;
    }

    private static List<ContactPoint> createContacts(RegistrationType registration) throws TransformException
    {
        List<ContactPoint> contactPoints = new ArrayList<>();

        if (StringUtils.isNotBlank(registration.getHomeTelephone()))
        {
            ContactPoint contact = new ContactPoint()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setUse(ContactPoint.ContactPointUse.HOME)
                    .setValue(registration.getHomeTelephone());
        }

        if (StringUtils.isNotBlank(registration.getWorkTelephone()))
        {
            ContactPoint contact = new ContactPoint()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setUse(ContactPoint.ContactPointUse.WORK)
                    .setValue(registration.getWorkTelephone());
        }

        if (StringUtils.isNotBlank(registration.getMobile()))
        {
            ContactPoint contact = new ContactPoint()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setUse(ContactPoint.ContactPointUse.MOBILE)
                    .setValue(registration.getMobile());
        }

        if (StringUtils.isNotBlank(registration.getEmail()))
        {
            ContactPoint contact = new ContactPoint()
                    .setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setUse(ContactPoint.ContactPointUse.HOME)
                    .setValue(registration.getMobile());
        }

        return contactPoints;
    }
}
