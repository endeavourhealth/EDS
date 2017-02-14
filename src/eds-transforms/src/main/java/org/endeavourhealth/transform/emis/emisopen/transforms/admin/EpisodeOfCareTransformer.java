package org.endeavourhealth.transform.emis.emisopen.transforms.admin;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.RegistrationType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.PeriodHelper;
import org.hl7.fhir.instance.model.EpisodeOfCare;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Period;
import org.hl7.fhir.instance.model.Resource;

import java.util.Date;
import java.util.List;

public class EpisodeOfCareTransformer {

    public static void transform(MedicalRecordType medicalRecord, List<Resource> resources, String organisationGuid, String patientGuid) throws TransformException
    {
        RegistrationType source = medicalRecord.getRegistration();

        if (source == null)
            throw new TransformException("Registration element is null");

        EpisodeOfCare target = new EpisodeOfCare();
        target.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_EPISODE_OF_CARE));

        EmisOpenHelper.setUniqueId(target, patientGuid, null);

        target.setPatient(EmisOpenHelper.createPatientReference(patientGuid));
        target.setManagingOrganization(EmisOpenHelper.createOrganisationReference(organisationGuid));

        if (source.getUsualGpID() != null) {
            String usualGpGuid = source.getUsualGpID().getGUID();
            if (!Strings.isNullOrEmpty(usualGpGuid)) {
                target.setCareManager(EmisOpenHelper.createPractitionerReference(usualGpGuid));
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
