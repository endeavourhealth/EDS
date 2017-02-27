package org.endeavourhealth.transform.emis.emisopen.transforms.common;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.hl7.fhir.instance.model.Enumerations;

public class SexConverter {

    public static Enumerations.AdministrativeGender convertSex(String sex) throws TransformException {
        sex = sex.trim();
        sex = sex.toUpperCase();

        if (sex.equals("U")
                || sex.equals("UNKNOWN")) { //during testing with Emis, we saw at least one patient with sex UNKNOWN and not U
            return Enumerations.AdministrativeGender.UNKNOWN;

        } else if (sex.equals("M")) {
            return Enumerations.AdministrativeGender.MALE;

        } else if (sex.equals("F")) {
            return Enumerations.AdministrativeGender.FEMALE;

        } else if (sex.equals("I")) {
            return Enumerations.AdministrativeGender.OTHER;

        } else {
            throw new TransformException("Sex vocabulary of " + sex.toString());
        }
    }

    /*public static Enumerations.AdministrativeGender convertSex(String sex) throws TransformException
    {
        switch ((sex + "").trim().toUpperCase())
        {
            case "U": return Enumerations.AdministrativeGender.UNKNOWN;
            case "M": return Enumerations.AdministrativeGender.MALE;
            case "F": return Enumerations.AdministrativeGender.FEMALE;
            case "I": return Enumerations.AdministrativeGender.OTHER;
            default: throw new TransformException("Sex vocabulary of " + sex.toString());
        }
    }*/
}
