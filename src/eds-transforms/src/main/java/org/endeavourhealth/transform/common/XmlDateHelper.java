package org.endeavourhealth.transform.common;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;

public class XmlDateHelper {

    public static Date convertDate(XMLGregorianCalendar xmlDate) {
        return xmlDate.toGregorianCalendar().getTime();
    }
}
