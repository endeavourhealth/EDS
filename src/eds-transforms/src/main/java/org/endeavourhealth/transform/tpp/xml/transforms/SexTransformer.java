package org.endeavourhealth.transform.tpp.xml.transforms;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.tpp.xml.schema.Sex;
import org.hl7.fhir.instance.model.Enumerations;

public class SexTransformer {

    public static Enumerations.AdministrativeGender transform(Sex tppSex) throws TransformException {
        if (tppSex == Sex.F) {
            return Enumerations.AdministrativeGender.FEMALE;
        } else if (tppSex == Sex.M) {
            return  Enumerations.AdministrativeGender.MALE;
        } else {
            throw new TransformException("Unhandled sex value [" + tppSex + "]");
        }
    }
}
