package org.endeavourhealth.transform.emis.openhr.transforms.converters;

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
}
