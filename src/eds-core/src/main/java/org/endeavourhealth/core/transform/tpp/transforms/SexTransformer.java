package org.endeavourhealth.core.transform.tpp.transforms;

import org.endeavourhealth.core.transform.common.TransformException;
import org.endeavourhealth.core.transform.tpp.schema.Sex;
import org.hl7.fhir.instance.model.Enumerations;

public class SexTransformer {

    public static Enumerations.AdministrativeGender transform(Sex tppSex) {
        if (tppSex == Sex.F) {
            return Enumerations.AdministrativeGender.FEMALE;
        } else if (tppSex == Sex.M) {
            return  Enumerations.AdministrativeGender.MALE;
        } else {
            throw new TransformException("Unhandled sex value [" + tppSex + "]");
        }
    }
}
