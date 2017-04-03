package org.endeavourhealth.transform.emis.openhr.transforms.common;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.*;

import java.util.List;

public class OpenHRHelper
{
    public static void ensureDboNotDelete(DtDbo dbo) throws TransformException
    {
        ensureDboNotDelete(dbo.getUpdateMode());
    }

    public static void ensureDboNotDelete(VocUpdateMode updateMode) throws TransformException
    {
        if (updateMode == VocUpdateMode.DELETE)
            throw new TransformException("DBO type of Delete not supported");
    }

    public static OpenHR001Patient getPatient(OpenHR001AdminDomain adminDomain) throws TransformException
    {
        List<OpenHR001Patient> patients = adminDomain.getPatient();

        if (patients == null || patients.isEmpty())
            throw new TransformException("No AdminDomain.Patients found.");

        if (patients.size() != 1)
            throw new TransformException("Only single patient supported in AdminDomain.Patients.");

        OpenHR001Patient patient = patients.get(0);

        OpenHRHelper.ensureDboNotDelete(patient);

        return patient;
    }

    public static OpenHR001Person getPerson(List<OpenHR001Person> sourcePeople, String personId) throws TransformException
    {
        if (sourcePeople == null)
            throw new TransformException("No AdminDomain.Person found.");

        OpenHR001Person person = sourcePeople.stream()
                .filter(p -> p.getId().equals(personId))
                .collect(StreamExtension.singleOrNullCollector());

        //if the person is there multiple times, then it will just throw a general exception.

        if (person == null)
            try
            {
                throw new TransformException("Person not found: " + personId);
            } catch (TransformException e)
            {
                e.printStackTrace();
            }

        OpenHRHelper.ensureDboNotDelete(person);

        return person;
    }

    public static String getPatientOrganisationGuid(OpenHR001Patient patient) throws TransformException
    {
        if (patient == null)
            throw new TransformException("patient is null");

        if ((patient.getCaseloadPatient() == null) || (patient.getCaseloadPatient().size() == 0))
            throw new TransformException("caseloadPatient is null or empty");

        OpenHR001CaseloadPatient caseloadPatient = patient.getCaseloadPatient().get(0);

        String organisationGuid = caseloadPatient.getOrganisation();

        if (StringUtils.isBlank(organisationGuid))
            throw new TransformException("organisationGuid is null or empty");

        return organisationGuid;
    }
}
