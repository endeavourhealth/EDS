package org.endeavourhealth.transform.emis.emisopen.transforms.admin;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.RegistrationType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.PeriodHelper;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;

public class EpisodeOfCareTransformer {

    public static void transform(List<Resource> resources, MedicalRecordType medicalRecord, String organisationGuid, String patientGuid) throws TransformException
    {
        RegistrationType source = medicalRecord.getRegistration();

        if (source == null)
            throw new TransformException("Registration element is null");

        EpisodeOfCare target = new EpisodeOfCare();
        target.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_EPISODE_OF_CARE));

        target.setId(patientGuid);
        target.setPatient(ReferenceHelper.createReference(ResourceType.Patient, patientGuid));
        target.setManagingOrganization(ReferenceHelper.createReference(ResourceType.Organization, organisationGuid));

        if (source.getUsualGpID() != null) {
            String usualGpGuid = source.getUsualGpID().getGUID();
            if (!Strings.isNullOrEmpty(usualGpGuid)) {
                target.setCareManager(ReferenceHelper.createReference(ResourceType.Practitioner, usualGpGuid));
            }
        }

        Date dateAdded = DateConverter.getDate(source.getDateAdded());
        Date deducted = DateConverter.getDate(source.getDeductedDate());
        Period fhirPeriod = PeriodHelper.createPeriod(dateAdded, deducted);
        target.setPeriod(fhirPeriod);

        boolean active = PeriodHelper.isActive(fhirPeriod);
        if (active) {
            target.setStatus(EpisodeOfCare.EpisodeOfCareStatus.ACTIVE);
        } else {
            target.setStatus(EpisodeOfCare.EpisodeOfCareStatus.FINISHED);
        }

        resources.add(target);
    }
}
