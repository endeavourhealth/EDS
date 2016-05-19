package org.endeavourhealth.transform.emis.openhr.transforms;

import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001AdminDomain;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001Patient;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001Person;
import org.endeavourhealth.transform.emis.openhr.transforms.helpers.OpenHRHelper;
import org.endeavourhealth.transform.emis.openhr.transforms.converters.*;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.Enumerations.AdministrativeGender;

import java.util.List;

public class PatientTransformer
{
    public static Patient transform(OpenHR001AdminDomain adminDomain) throws TransformException
    {
        OpenHR001Patient sourcePatient = OpenHRHelper.getPatient(adminDomain);
        OpenHR001Person sourcePerson = OpenHRHelper.getPerson(adminDomain.getPerson(), sourcePatient.getId());

        Patient targetPatient = new Patient();

        targetPatient.setId(sourcePatient.getId());

        List<Identifier> identifiers = IdentifierConverter.convert(sourcePatient.getPatientIdentifier());

        if (identifiers != null)
            identifiers.forEach(targetPatient::addIdentifier);

        List<HumanName> names = NameConverter.convertName(sourcePerson);

        if (names != null)
            names.forEach(targetPatient::addName);

        List<ContactPoint> telecoms = ContactPointConverter.convert(sourcePerson.getContact());

        if (telecoms != null)
            telecoms.forEach(targetPatient::addTelecom);

        AdministrativeGender gender = SexConverter.convertSex(sourcePerson.getSex());
        targetPatient.setGender(gender);

        targetPatient.setBirthDate(DateConverter.toDate(sourcePerson.getBirthDate()));

        if (sourcePatient.getDateOfDeath() != null)
            targetPatient.setDeceased(new DateTimeType(DateConverter.toDate(sourcePatient.getDateOfDeath())));

        List<Address> addressList = AddressConverter.convertFromPersonAddress(sourcePerson.getAddress());

        if (addressList != null)
            addressList.forEach(targetPatient::addAddress);

        String organisationGuid = OpenHRHelper.getPatientOrganisationGuid(sourcePatient);

        targetPatient.setManagingOrganization(ReferenceHelper.createReference(ResourceType.Organization, organisationGuid));

        return targetPatient;
    }
}
