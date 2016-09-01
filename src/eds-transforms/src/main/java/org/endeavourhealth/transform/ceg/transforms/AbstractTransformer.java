package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.Date;

public class AbstractTransformer {

    protected static Long transformPatientId(Reference reference) {
        String id = ReferenceHelper.getReferenceId(reference);

        //TODO: convert string to long

        return null;
    }

    protected static Long transformStaffId(Reference reference) {
        String id = ReferenceHelper.getReferenceId(reference);

        //TODO: convert string to long

        return null;
    }

    protected static Date transformDate(Type type) throws Exception {
        if (type instanceof Age) {
            throw new TransformException("Cannot transform Age to Date");

        } else if (type instanceof DateTimeType) {
            DateTimeType dt = (DateTimeType)type;
            return dt.getValue();

        } else if (type instanceof Period) {
            Period period = (Period)type;
            if (period.hasStart()) {
                return period.getStart();
            } else {
                return null;
            }

        } else if (type instanceof Range) {
            throw new TransformException("Cannot transform Range to Date");

        } else if (type instanceof StringType) {
            throw new TransformException("Cannot transform StringType to Date");

        } else {
            throw new TransformException("Unsupported type to convert to date " + type.getClass());
        }

    }
}
