package org.endeavourhealth.transform.emis.emisopen.transforms.common;

import org.endeavourhealth.transform.common.TransformException;
import org.hl7.fhir.instance.model.Enumerations;

public class SexConverter
{
    public static Enumerations.AdministrativeGender convertSex(String sex) throws TransformException
    {
        switch ((sex + "").trim().toUpperCase())
        {
            case "U": return Enumerations.AdministrativeGender.UNKNOWN;
            case "M": return Enumerations.AdministrativeGender.MALE;
            case "F": return Enumerations.AdministrativeGender.FEMALE;
            case "I": return Enumerations.AdministrativeGender.OTHER;
            default: throw new TransformException("Sex vocabulary of " + sex.toString());
        }
    }
}
