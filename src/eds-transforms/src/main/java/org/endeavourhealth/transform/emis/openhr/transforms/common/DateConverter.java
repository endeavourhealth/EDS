package org.endeavourhealth.transform.emis.openhr.transforms.common;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.DtDatePart;
import org.endeavourhealth.transform.emis.openhr.schema.VocDatePart;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.DateType;
import org.hl7.fhir.instance.model.TemporalPrecisionEnum;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;

public class DateConverter
{
    public static Date toDate(XMLGregorianCalendar value)
    {
        if (value == null)
            return null;

        return value.toGregorianCalendar().getTime();
    }

    public static DateTimeType convertPartialDateTimeToDateTimeType(DtDatePart source) throws TransformException
    {
        if (source == null)
            throw new TransformException("Invalid DateTime");

        if (source.getDatepart() == VocDatePart.U)
            return null;

        switch (source.getDatepart())
        {
            case Y: return new DateTimeType(toDate(source.getValue()), TemporalPrecisionEnum.YEAR);
            case YM: return new DateTimeType(toDate(source.getValue()), TemporalPrecisionEnum.MONTH);
            case YMD: return new DateTimeType(toDate(source.getValue()), TemporalPrecisionEnum.DAY);
            case YMDT: return new DateTimeType(toDate(source.getValue()), TemporalPrecisionEnum.SECOND);
            default: throw new TransformException("Date part not supported: " + source.getDatepart().toString());
        }
    }

    public static DateType convertPartialDateTimeToDateType(DtDatePart source) throws TransformException
    {
        if (source == null)
            throw new TransformException("Invalid DateTime");

        if (source.getDatepart() == VocDatePart.U)
            return null;

        switch (source.getDatepart())
        {
            case Y: return new DateType(toDate(source.getValue()), TemporalPrecisionEnum.YEAR);
            case YM: return new DateType(toDate(source.getValue()), TemporalPrecisionEnum.MONTH);
            case YMD:
            case YMDT: return new DateType(toDate(source.getValue()), TemporalPrecisionEnum.DAY);
            default: throw new TransformException("Date part not supported: " + source.getDatepart().toString());
        }
    }

}
