package org.endeavourhealth.transform.emis.openhr.transforms;

import org.endeavourhealth.transform.common.ReferenceHelper;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001AdminDomain;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001Patient;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001Person;
import org.endeavourhealth.transform.emis.openhr.transforms.helpers.OpenHRHelper;
import org.endeavourhealth.transform.emis.openhr.transforms.converters.DateConverter;
import org.endeavourhealth.transform.emis.openhr.transforms.converters.IdentifierConverter;
import org.endeavourhealth.transform.emis.openhr.transforms.converters.NameConverter;
import org.endeavourhealth.transform.emis.openhr.transforms.converters.SexConverter;
import org.hl7.fhir.instance.model.*;

import java.util.List;

public class PersonTransformer
{
    public static Person transform(OpenHR001AdminDomain adminDomain) throws TransformException
    {
        OpenHR001Patient sourcePatient = OpenHRHelper.getPatient(adminDomain);
        OpenHR001Person sourcePerson = OpenHRHelper.getPerson(adminDomain.getPerson(), sourcePatient.getId());

        Person targetPerson = new Person();

        targetPerson.setId(sourcePatient.getId());

        List<Identifier> identifiers = IdentifierConverter.convert(sourcePatient.getPatientIdentifier());

        if (identifiers != null)
            for (Identifier identifier : identifiers)
                targetPerson.addIdentifier(identifier);

        List<HumanName> names = NameConverter.convertName(sourcePerson);

        for (HumanName name : names)
            targetPerson.addName(name);

        targetPerson.setGender(SexConverter.convertSex(sourcePerson.getSex()));
        targetPerson.setBirthDate(DateConverter.toDate(sourcePerson.getBirthDate()));

        Person.PersonLinkComponent link = targetPerson.addLink();
        link.setTarget(ReferenceHelper.createReference(ResourceType.Patient, sourcePatient.getId()));

        return targetPerson;
    }
}
