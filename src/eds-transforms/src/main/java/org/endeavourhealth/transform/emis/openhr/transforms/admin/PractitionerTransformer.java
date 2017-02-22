package org.endeavourhealth.transform.emis.openhr.transforms.admin;

import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.*;
import org.endeavourhealth.transform.emis.openhr.transforms.common.ContactPointConverter;
import org.endeavourhealth.transform.emis.openhr.transforms.common.OpenHRHelper;
import org.endeavourhealth.transform.emis.openhr.transforms.common.SexConverter;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.NameConverter;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

public class PractitionerTransformer
{
    public static List<Practitioner> transform(OpenHR001AdminDomain adminDomain) throws TransformException
    {
        ArrayList<Practitioner> practitioners = new ArrayList<>();

        for (OpenHR001UserInRole userInRole: adminDomain.getUserInRole())
            practitioners.add(createPractitioner(adminDomain, userInRole));

        return practitioners;
    }

    private static Practitioner createPractitioner(OpenHR001AdminDomain adminDomain, OpenHR001UserInRole userInRole) throws TransformException
    {
        OpenHRHelper.ensureDboNotDelete(userInRole);

        OpenHR001User user = getUser(adminDomain.getUser(), userInRole.getUser());
        OpenHR001Role role = getRole(adminDomain.getRole(), userInRole.getRole());
        OpenHR001Person person = getPerson(adminDomain.getPerson(), user.getUserPerson());

        Practitioner practitioner = new Practitioner();
        
        practitioner.setId(userInRole.getId());
        practitioner.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PRACTITIONER));

        for (Identifier identifier : convertIdentifiers(userInRole.getUserIdentifier()))
            practitioner.addIdentifier(identifier);

        practitioner.setName(NameConverter.convert(
                person.getForenames(),
                person.getSurname(),
                person.getTitle()));

        if (person.getSex() != null)
            practitioner.setGender(SexConverter.convertSexToFhir(person.getSex()));

        List<ContactPoint> telecoms = ContactPointConverter.convert(person.getContact());

        if (telecoms != null)
            telecoms.forEach(practitioner::addTelecom);

        Practitioner.PractitionerPractitionerRoleComponent practitionerRole =  practitioner.addPractitionerRole();
        practitionerRole.setManagingOrganization(ReferenceHelper.createReference(ResourceType.Organization, role.getOrganisation()));
        practitionerRole.setRole(new CodeableConcept().setText(role.getName() + ". " + role.getUserCategory().getDisplayName()));

        return practitioner;
    }

    private static OpenHR001User getUser(List<OpenHR001User> userList, String userId) throws TransformException
    {
        if (userList == null)
            throw new TransformException("User not found: " + userId);

        OpenHR001User user = userList.stream()
                .filter(u -> u.getId().equals(userId))
                .collect(StreamExtension.singleOrNullCollector());

        if (user == null)
            throw new TransformException("User not found: " + userId);

        OpenHRHelper.ensureDboNotDelete(user);

        return user;
    }

    private static OpenHR001Role getRole(List<OpenHR001Role> roleList, String roleId) throws TransformException
    {
        if (roleList == null)
            throw new TransformException("Role not found: " + roleId);

        // the following should be a singleOrNullCollector as a role with the same id should only occur once however OpenHR appears to duplicate roles
        OpenHR001Role role = roleList.stream()
                .filter(u -> u.getId().equals(roleId))
                .collect(StreamExtension.firstOrNullCollector());

        if (role == null)
            throw new TransformException("Role not found: " + roleId);

        OpenHRHelper.ensureDboNotDelete(role);

        return role;
    }

    private static OpenHR001Person getPerson(List<OpenHR001Person> sourcePeople, String personId) throws TransformException
    {
        if (sourcePeople == null)
            throw new TransformException("No AdminDomain.Person found.");

        OpenHR001Person person = sourcePeople.stream()
                .filter(p -> p.getId().equals(personId))
                .collect(StreamExtension.singleOrNullCollector());

        //if the person is there multiple times, then it will just throw a general exception.

        if (person == null)
            throw new TransformException("Person not found: " + personId);

        OpenHRHelper.ensureDboNotDelete(person);

        return person;
    }

    private static List<Identifier> convertIdentifiers(List<DtUserIdentifier> sourceIdentifiers) throws TransformException
    {
        List<Identifier> targetIdentifiers = new ArrayList<>();

        if (sourceIdentifiers != null)
        {
            for (DtUserIdentifier source : sourceIdentifiers)
            {
                targetIdentifiers.add(new Identifier()
                        .setSystem(convertIdentifierType(source.getIdentifierType()))
                        .setValue(source.getValue()));
            }
        }

        return targetIdentifiers;
    }

    private static String convertIdentifierType(VocUserIdentifierType openHRType) throws TransformException
    {
        switch (openHRType)
        {
            case GP: return FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER;
            case NAT: return FhirUri.IDENTIFIER_SYSTEM_DOCTOR_INDEX_NUMBER;
            case PRES: return FhirUri.IDENTIFIER_SYSTEM_GMP_PPD_CODE;
            default: throw new TransformException("VocUserIdentifierType not supported: " + openHRType.toString());
        }
    }
}
