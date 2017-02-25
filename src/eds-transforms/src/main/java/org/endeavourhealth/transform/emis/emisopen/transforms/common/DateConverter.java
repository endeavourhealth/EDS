package org.endeavourhealth.transform.emis.emisopen.transforms.common;

import com.google.common.base.Strings;
import org.apache.commons.lang3.NotImplementedException;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.TemporalPrecisionEnum;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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

    public static Date getDate(String dateString) throws TransformException {

        if (Strings.isNullOrEmpty(dateString)) {
            return null;
        }

        try {
            return DateConverter.EMISOPEN_DATEFORMAT.parse(dateString);

        } catch (ParseException e) {
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

    public static DateTimeType convertPartialDateToDateTimeType(String dateString, String timeString, Byte datePart) throws TransformException {
        //in some events (notably referrals, we have a null date part, so look at the strings to see what it should be
        if (datePart == null) {
            if (Strings.isNullOrEmpty(dateString)) {
                return null;
            } else {
                return convertPartialDateToDateTimeType(dateString, timeString, (short)0);
            }

        } else {
            return convertPartialDateToDateTimeType(dateString, timeString, datePart.shortValue());
        }
    }

    public static DateTimeType convertPartialDateToDateTimeType(String dateString, String timeString, Short datePart) throws TransformException {
        //in some events (notably referrals, we have a null date part, so look at the strings to see what it should be
        if (datePart == null) {
            if (Strings.isNullOrEmpty(dateString)) {
                return null;
            } else {
                return convertPartialDateToDateTimeType(dateString, timeString, (short)0);
            }

        } else {
            return convertPartialDateToDateTimeType(dateString, timeString, datePart.shortValue());
        }
    }

    private static DateTimeType convertPartialDateToDateTimeType(String dateString, String timeString, short datePart) throws TransformException {

        switch (datePart) {
            case 0:
            case 4:
                if (Strings.isNullOrEmpty(timeString)) {
                    return new DateTimeType(getDate(dateString), TemporalPrecisionEnum.DAY);
                } else {
                    return new DateTimeType(getDateAndTime(dateString, timeString), TemporalPrecisionEnum.SECOND);
                }
            case 1:
                return new DateTimeType(getDate("01/" + dateString), TemporalPrecisionEnum.MONTH);
            case 2:
                return new DateTimeType(getDate("01/01/" + dateString), TemporalPrecisionEnum.YEAR);
            case 3:
                return null;
            default:
                //actually log out what part isn't recognised
                throw new NotImplementedException("Date part not recognised " + datePart + " with dateString " + dateString + " and timeString " + timeString);
                //throw new NotImplementedException("Date part not recognised");
        }
    }

    /*public static DateTimeType convertPartialDateToDateTimeType(String dateString, String timeString, short datePart) throws TransformException {

        switch (datePart)
        {
            case 0:
            case 4:
                if (Strings.isNullOrEmpty(timeString)) {
                    return new DateTimeType(getDate(dateString), TemporalPrecisionEnum.DAY);
                } else {
                    return new DateTimeType(getDateAndTime(dateString, timeString), TemporalPrecisionEnum.SECOND);
                }
            case 1:
                return new DateTimeType(getDate("01/" + dateString), TemporalPrecisionEnum.MONTH);
            case 2:
                return new DateTimeType(getDate("01/01/" + dateString), TemporalPrecisionEnum.YEAR);
            case 3:
                return null;
            default:
                //actually log out what part isn't recognised
                throw new NotImplementedException("Date part not recognised " + datePart + " with dateString " + dateString + " and timeString " + timeString);
                //throw new NotImplementedException("Date part not recognised");
        }
    }*/

    public static Time addMinutesToTime(Date date, int minutes)
    {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minutes);
        return new Time(cal.getTime().getTime());
    }
}
