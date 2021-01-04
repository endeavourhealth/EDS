package org.endeavourhealth.queuereader.routines;

import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class SD292 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD292.class);


    public static void testDates(String overridingLocaleStr) {
        try {

            List<String> strs = new ArrayList<>();


            strs.add("1983-01-02");
            strs.add("1983-01-04");
            strs.add("1983-12-28");

            TimeZone tz = TimeZone.getDefault();
            LOG.debug("Timezone = " + tz.getDisplayName());

            Locale locale = Locale.getDefault(Locale.Category.FORMAT);
            LOG.debug("DefaultLocale = " + locale.getDisplayName());

            Calendar cal = null;

            if (overridingLocaleStr != null) {
                Locale overridingLocale = LocaleUtils.toLocale(overridingLocaleStr);
                //Locale overridingLocale = Locale.forLanguageTag(overridingLocaleStr);
                LOG.debug("OverridingLocale = " + overridingLocale.getDisplayName());

                cal = Calendar.getInstance(overridingLocale);

            } else {
                cal = Calendar.getInstance(); //uses default locale
            }

            Calendar.getInstance();
            LOG.debug("Calendar = " + cal);

            for (String str: strs) {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dob = sdf.parse(str);

                cal.setTime(dob);
                int birthYear = cal.get(Calendar.YEAR);
                int birthMonth = cal.get(Calendar.MONTH) + 1; //Java month is zero-indexed
                int birthWeek = cal.get(Calendar.WEEK_OF_YEAR);

                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                String dayOfWeekStr = "";
                switch (dayOfWeek) {
                    case Calendar.MONDAY: dayOfWeekStr = "mon"; break;
                    case Calendar.TUESDAY: dayOfWeekStr = "tue"; break;
                    case Calendar.WEDNESDAY: dayOfWeekStr = "wed"; break;
                    case Calendar.THURSDAY: dayOfWeekStr = "thu"; break;
                    case Calendar.FRIDAY: dayOfWeekStr = "fri"; break;
                    case Calendar.SATURDAY: dayOfWeekStr = "sat"; break;
                    case Calendar.SUNDAY: dayOfWeekStr = "sun"; break;
                }

                LOG.debug("" + str + " -> year " + birthYear + " month " + birthMonth + " week " + birthWeek + " day " + dayOfWeekStr);
            }

        } catch (Throwable ex) {
            LOG.error("", ex);
        }
    }
}
