package org.endeavourhealth.hl7test.transforms.framework;

import ca.uhn.hl7v2.model.primitive.CommonTS;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

public class Helpers {
    public static LocalDateTime parseTS(String value) throws ParseException {
        try {
            CommonTS ts = new CommonTS(value);

            return LocalDateTime.ofInstant(ts.getValueAsDate().toInstant(), ZoneId.systemDefault());
        } catch (Exception e) {
            throw new ParseException("Error parsing TS", e);
        }
    }

    public static Integer parseInteger(String value) throws ParseException {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            throw new ParseException("Error parsing integer", e);
        }
    }

    public static <T extends Object> T getSafely(List<T> list, int index) {
        if (list == null)
            return null;

        if ((index >= 0) && (index <= (list.size() - 1)))
            return list.get(index);

        return null;
    }

    public static List<String> split(String str, String seperator) {
        if (str == null)
            return null;

        // fix anomoly with StringUtils.splitByWholeSeparatorPreserveAllTokens
        //
        // if split("|", "|") == { "", "" }
        // then split("", "|") should == { "" }
        //
        // (as per c#)
        //
        if (str.equals(""))
            return Arrays.asList(new String[] { "" });

        return Arrays.asList(StringUtils.splitByWholeSeparatorPreserveAllTokens(str, seperator));
    }
}
