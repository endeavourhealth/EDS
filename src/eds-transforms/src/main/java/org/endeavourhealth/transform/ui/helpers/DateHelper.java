package org.endeavourhealth.transform.ui.helpers;

import org.endeavourhealth.transform.ui.models.types.UIDate;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.DateType;
import org.hl7.fhir.instance.model.TemporalPrecisionEnum;

import java.util.Date;

public class DateHelper {
    public static UIDate convert(Date date) {
        if (date == null)
            return getUnknownDate();
        else
            return new UIDate()
                .setDate(date)
                .setPrecision(getStringValue(TemporalPrecisionEnum.MILLI));
    }

    public static UIDate convert(DateTimeType dateTimeType) {
        if (dateTimeType == null)
            return null;

        return new UIDate()
                .setDate(dateTimeType.getValue())
                .setPrecision(getStringValue(dateTimeType.getPrecision()));
    }

    public static UIDate convert(DateType dateType) {
        if (dateType == null)
            return null;

        return new UIDate()
                .setDate(dateType.getValue())
                .setPrecision(getStringValue(dateType.getPrecision()));
    }

    public static UIDate getUnknownDate() {
        return new UIDate()
                .setDate(null)
                .setPrecision("unknown");
    }

    private static String getStringValue(TemporalPrecisionEnum temporalPrecisionEnum) {
        switch (temporalPrecisionEnum)
        {
            case YEAR: return "year";
            case MONTH: return "month";
            case DAY: return "date";
            case MINUTE: return "minute";
            case SECOND: return "second";
            case MILLI: return "millisecond";
            default: return null;
        }
    }
}
