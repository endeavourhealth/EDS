package org.endeavourhealth.transform.ui.helpers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateHelper {
    public static String format(Date date) {
        if (date == null)
            return null;

        return format(toLocalDate(date));
    }
    public static String format(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
    }
    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    public static String getCuiDob(Date birthDate) {
        LocalDate localDate = DateHelper.toLocalDate(birthDate);
        java.time.Period timespan = localDate.until(LocalDate.now());
        return DateHelper.format(localDate) + " (" + Integer.toString(timespan.getYears()) + "y " + Integer.toString(timespan.getMonths()) + "m)";
    }
}
