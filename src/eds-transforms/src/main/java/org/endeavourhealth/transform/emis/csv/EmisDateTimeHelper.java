package org.endeavourhealth.transform.emis.csv;

import org.endeavourhealth.transform.emis.openhr.schema.VocDatePart;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.DateType;
import org.hl7.fhir.instance.model.TemporalPrecisionEnum;

import java.util.Date;

public class EmisDateTimeHelper {


    public static DateTimeType createDateTimeType(Date date, String precision) throws Exception {

        if (date == null) {
            return null;
        }

        //the precision String matches the precisions used in the other EMIS extract
        VocDatePart vocPrecision = VocDatePart.fromValue(precision);
        if (vocPrecision == null) {
            throw new IllegalArgumentException("Unsupported consultation precision [" + precision + "]");
        }

        switch (vocPrecision) {
            case U:
                return null;
            case Y:
                return new DateTimeType(date, TemporalPrecisionEnum.YEAR);
            case YM:
                return new DateTimeType(date, TemporalPrecisionEnum.MONTH);
            case YMD:
                return new DateTimeType(date, TemporalPrecisionEnum.DAY);
            case YMDT:
                return new DateTimeType(date, TemporalPrecisionEnum.MINUTE);
            default:
                throw new IllegalArgumentException("Unknown date precision [" + vocPrecision + "]");
        }
    }

    public static DateType createDateType(Date date, String precision) throws Exception {
        if (date == null) {
            return null;
        }

        VocDatePart vocPrecision = VocDatePart.fromValue(precision);
        if (vocPrecision == null) {
            throw new IllegalArgumentException("Unsupported consultation precision [" + precision + "]");
        }

        switch (vocPrecision) {
            case U:
                return null;
            case Y:
                return new DateType(date, TemporalPrecisionEnum.YEAR);
            case YM:
                return new DateType(date, TemporalPrecisionEnum.MONTH);
            case YMD:
            case YMDT:
                return new DateType(date, TemporalPrecisionEnum.DAY);
            default:
                throw new IllegalArgumentException("Unhandled date precision [" + vocPrecision + "]");
        }
    }
}
