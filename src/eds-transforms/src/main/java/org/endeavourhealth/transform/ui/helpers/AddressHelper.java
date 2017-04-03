package org.endeavourhealth.transform.ui.helpers;

import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.transform.ui.models.types.UIAddress;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.StringType;

import java.util.List;

public class AddressHelper {
    public static UIAddress getHomeAddress(List<Address> addresses) {
        if (addresses == null)
            return null;

        Address homeAddress = addresses
                .stream()
                .filter(t -> (t.getUse() != null) && (t.getUse() == Address.AddressUse.HOME))
                .filter(t -> (t.getPeriod() == null) || (t.getPeriod().getEnd() == null))
                .collect(StreamExtension.firstOrNullCollector());

        if (homeAddress == null) {
            homeAddress =
                    addresses
                    .stream()
                    .filter(t -> t.getUse() == null)
                    .filter(t -> (t.getPeriod() == null) || t.getPeriod().getEnd() == null)
                    .collect(StreamExtension.firstOrNullCollector());
        }

        if (homeAddress == null)
            return null;

        return new UIAddress()
                .setLine1(getLine(homeAddress.getLine(), 0))
                .setLine2(getLine(homeAddress.getLine(), 1))
                .setLine3(getLine(homeAddress.getLine(), 2))
                .setDistrict(homeAddress.getDistrict())
                .setCity(homeAddress.getCity())
                .setPostalCode(homeAddress.getPostalCode());
    }

    private static String getLine(List<StringType> lines, int lineNumber) {
        if (lines == null)
            return "";

        if (lineNumber >= lines.size())
            return "";

        return lines.get(lineNumber).getValueNotNull();
    }
}
