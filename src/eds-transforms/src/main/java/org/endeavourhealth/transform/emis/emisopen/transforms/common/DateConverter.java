package org.endeavourhealth.transform.emis.emisopen.transforms.common;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.helpers.Transform;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.DtDatePart;
import org.endeavourhealth.transform.emis.openhr.schema.VocDatePart;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.TemporalPrecisionEnum;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateConverter
{
    public final static SimpleDateFormat EMISOPEN_DATEANDTIMEFORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public final static SimpleDateFormat EMISOPEN_DATEFORMAT = new SimpleDateFormat("dd/MM/yyyy");
    public final static SimpleDateFormat EMISOPEN_TIMEFORMAT = new SimpleDateFormat("HH:mm");

    public static Date getDateAndTime(String dateString, String timeString) throws TransformException
    {
        return getDateAndTime(dateString + " " + timeString);
    }

    public static Date getDateAndTime(String dateAndTimeString) throws TransformException
    {
        try
        {
            return DateConverter.EMISOPEN_DATEANDTIMEFORMAT.parse(dateAndTimeString);
        }
        catch (ParseException e)
        {
            throw new TransformException("Could not parse date", e);
        }
    }

    public static Date getDate(String dateString) throws TransformException
    {
        try
        {
            return DateConverter.EMISOPEN_DATEFORMAT.parse(dateString);
        }
        catch (ParseException e)
        {
            throw new TransformException("Could not parse date", e);
        }
    }

    public static Time getTime(String timeString) throws TransformException
    {
        try
        {
            return new Time(DateConverter.EMISOPEN_TIMEFORMAT.parse(timeString).getTime());
        }
        catch (ParseException e)
        {
            throw new TransformException("Could not parse time", e);
        }
    }

    public static DateTimeType convertPartialDateToDateTimeType(String dateString, String timeString, short datePart) throws TransformException
    {
        switch (datePart)
        {
            case 0:
            {
                if (StringUtils.isNotBlank(timeString))
                    return new DateTimeType(getDateAndTime(dateString, timeString), TemporalPrecisionEnum.SECOND);
                else
                    return new DateTimeType(getDateAndTime(dateString, timeString), TemporalPrecisionEnum.DAY);
            }
            case 1: return new DateTimeType(getDate("01/" + dateString), TemporalPrecisionEnum.MONTH);
            case 2: return new DateTimeType(getDate("01/01/" + dateString), TemporalPrecisionEnum.YEAR);
            case 3: return null;
            default: throw new NotImplementedException("Date part not recognised");
        }
    }

    public static Time addMinutesToTime(Date date, int minutes)
    {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minutes);
        return new Time(cal.getTime().getTime());
    }
}
