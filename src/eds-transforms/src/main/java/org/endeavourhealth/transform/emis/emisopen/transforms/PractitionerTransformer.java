package org.endeavourhealth.transform.emis.emisopen.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.PersonType;
import org.endeavourhealth.transform.emis.emisopen.transforms.converters.AddressConverter;
import org.endeavourhealth.transform.fhir.ContactPointCreater;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.endeavourhealth.transform.fhir.NameConverter;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

public class PractitionerTransformer
{
    public static List<Resource> transform(MedicalRecordType medicalRecordType, String organisationGuid) throws TransformException
    {
        List<Resource> resources = new ArrayList<>();

        for (PersonType personType : medicalRecordType.getPeopleList().getPerson())
            resources.add(createPractitioner(personType, organisationGuid));

        return resources;
    }

    private static Practitioner createPractitioner(PersonType personType, String organisationGuid) throws TransformException
    {
        Practitioner practitioner = new Practitioner();

        practitioner.setId(personType.getGUID());
        practitioner.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_PRACTITIONER));

        List<Identifier> identifiers = createIdentifiers(personType.getGmcCode().toString(), personType.getNationalCode(), personType.getPrescriptionCode());

        for (Identifier identifier : identifiers)
            practitioner.addIdentifier(identifier);

        practitioner.setName(NameConverter.convert(
                personType.getFirstNames(),
                personType.getLastName(),
                personType.getTitle()));

        if (personType.getAddress() != null)
            AddressConverter.convert(personType.getAddress(), Address.AddressUse.WORK);

        List<ContactPoint> contactPoints = ContactPointCreater.createWorkContactPoints(personType.getTelephone1(), personType.getTelephone2(), personType.getFax(), personType.getEmail());

        for (ContactPoint contactPoint : contactPoints)
            practitioner.addTelecom(contactPoint);

        Practitioner.PractitionerPractitionerRoleComponent practitionerPractitionerRoleComponent = new Practitioner.PractitionerPractitionerRoleComponent();
        practitionerPractitionerRoleComponent.setManagingOrganization(ReferenceHelper.createReference(ResourceType.Organization, organisationGuid));
        practitionerPractitionerRoleComponent.setRole(new CodeableConcept().setText(personType.getCategory().getDescription()));



        return practitioner;
    }

    private static List<Identifier> createIdentifiers(String gmcNumber, String doctorIndexNumber, String gmpPpdCode)
    {
        List<Identifier> identifiers = new ArrayList<>();

        if (StringUtils.isNotBlank(gmcNumber))
        {
            identifiers.add(new Identifier()
                            .setSystem(FhirUris.IDENTIFIER_SYSTEM_GMC_NUMBER)
                            .setValue(gmcNumber));
        }

        if (StringUtils.isNotBlank(doctorIndexNumber))
        {
            identifiers.add(new Identifier()
                    .setSystem(FhirUris.IDENTIFIER_SYSTEM_DOCTOR_INDEX_NUMBER)
                    .setValue(doctorIndexNumber));
        }

        if (StringUtils.isNotBlank(gmpPpdCode))
        {
            identifiers.add(new Identifier()
                    .setSystem(FhirUris.IDENTIFIER_SYSTEM_GMP_PPD_CODE)
                    .setValue(gmpPpdCode));
        }

        return identifiers;
    }
}
