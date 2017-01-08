package org.endeavourhealth.transform.emis.openhr.transforms.common;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.VocSex;
import org.hl7.fhir.instance.model.Enumerations;

public class SexConverter
{
    public static Enumerations.AdministrativeGender convertSexToFhir(VocSex sex) throws TransformException
    {
        switch (sex)
        {
            case U:
                return Enumerations.AdministrativeGender.UNKNOWN;
            case M:
                return Enumerations.AdministrativeGender.MALE;
            case F:
                return Enumerations.AdministrativeGender.FEMALE;
            case I:
                return Enumerations.AdministrativeGender.OTHER;
            default:
                throw new TransformException("Sex vocabulary of " + sex.toString());
        }
    }

    public static VocSex convertSexFromFhir(Enumerations.AdministrativeGender sex) throws TransformException
    {
        switch (sex)
        {
            case UNKNOWN:
                return VocSex.U;
            case MALE:
                return VocSex.M;
            case FEMALE:
                return VocSex.F;
            case OTHER:
                return VocSex.I;
            default:
                throw new TransformException("Unsupported FHIR sex " + sex);
        }
    }
}
