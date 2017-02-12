package org.endeavourhealth.transform.hl7v2.specific.homerton;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.transform.hl7v2.parser.Component;
import org.endeavourhealth.transform.hl7v2.parser.Field;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.Segment;
import org.endeavourhealth.transform.hl7v2.parser.messages.AdtMessage;
import org.endeavourhealth.transform.hl7v2.parser.segments.SegmentName;

import java.util.List;

public class HomertonPreTransform {
    public static AdtMessage preTransform(AdtMessage sourceMessage) throws ParseException {
        Validate.notNull(sourceMessage);

        // remove all fields with only "" in them
        removeEmptyDoubleQuotes(sourceMessage);

        // fix PD1
        if (sourceMessage.hasPd1Segment())
            fixPd1(sourceMessage.getSegment(SegmentName.PD1));

        return sourceMessage;
    }

    private static void removeEmptyDoubleQuotes(AdtMessage sourceMessage) {
        List<Component> components = sourceMessage.getAllComponents();

        for (Component component : components)
            if (component.getAsString().equals("\"\""))
                component.setAsString("");
    }

    /*
        PD1.4 incorrectly contains both primary care organisation and doctor
        It should only contain the doctor.
        The organisation should be carried in PD1.3.
        This method moves the organisational fields from PD1.4 to PD1.3.

        Homerton's PD1.3:

        1          2       3         5           6       7            8        9.1      9.2      9.3          14.1       14.2
        DoctorCode^Surname^Forename^^PhoneNumber^OdsCode^PracticeName^Address1^Address2&Address3&Address4^^^^^PctOdsCode&ShaOdsCode

        Examples:

        G3339325^SMITH^A^^1937573848^B86010^DR SR LIGHTFOOT & PARTNERS^Church View Surgery^School Lane&&LS22 5BQ^^^^^Q12&5HJ
        G3426500^LYLE^ROBERT^^020 89867111^F84003^LOWER CLAPTON GROUP PRACTICE^Lower Clapton Health Ctr.^36 Lower Clapton Road&London&E5 0PD^^^^^Q06&5C3
     */

    private static void fixPd1(Segment pd1Segment) {

            Field field = pd1Segment.getField(4);

            String odsCode = field.getComponentAsString(6);
            String practiceName = field.getComponentAsString(7);
            String address1 = field.getComponentAsString(8);
            String address2 = field.getComponentAsString(9);
            String parentOds = field.getComponentAsString(14);

    }
}
