package org.endeavourhealth.transform.fhir;

import org.hl7.fhir.instance.model.EpisodeOfCare;
import org.hl7.fhir.instance.model.Period;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;

public class PeriodHelper {

    public static Period createPeriod(Date start, Date end) {
        Period fhirPeriod = new Period();
        fhirPeriod.setStart(start);
        fhirPeriod.setEnd(end);
        return fhirPeriod;
    }

    public static boolean isActive(Period period) {
        return (period.getEnd() == null
                || period.getEnd().after(new Date()));
    }

    public static Period createPeriod(XMLGregorianCalendar xmlStart, XMLGregorianCalendar xmlEnd) {
        Date start = null;
        if (xmlStart != null) {
            start = xmlStart.toGregorianCalendar().getTime();
        }
        Date end = null;
        if (xmlEnd != null) {
            end = xmlEnd.toGregorianCalendar().getTime();
        }
        return createPeriod(start, end);
    }
}
