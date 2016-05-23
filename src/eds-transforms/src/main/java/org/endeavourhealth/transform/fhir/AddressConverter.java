package org.endeavourhealth.transform.fhir;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressConverter
{
    public static Address createAddress(Address.AddressUse addressUse, String houseNameFlatNumber, String street, String village, String town, String county, String postcode)
    {
        Address address = new Address();

        if (addressUse != null)
            if (addressUse != Address.AddressUse.NULL)
                address.setUse(addressUse);

        if (StringUtils.isNotBlank(houseNameFlatNumber))
            address.addLine(houseNameFlatNumber);

        if (StringUtils.isNotBlank(street))
            address.addLine(street);

        if (StringUtils.isNotBlank(village))
            address.addLine(village);

        if (StringUtils.isNotBlank(town))
            address.setCity(town);

        if (StringUtils.isNotBlank(county))
            address.setDistrict(county);

        if (StringUtils.isNotBlank(postcode))
            address.setPostalCode(postcode);

        List<String> lines = new ArrayList<>();
        lines.add(houseNameFlatNumber);
        lines.add(street);
        lines.add(village);
        lines.add(town);
        lines.add(county);
        lines.add(postcode);

        address.setText(createDisplayText(lines));

        return address;
    }

    public static String createDisplayText(List<String> lines)
    {
        List<String> trimmedLines = new ArrayList<>();

        for (String line : lines)
            if (StringUtils.isNotBlank(line))
                trimmedLines.add(line.trim());

        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (String trimmedLine : trimmedLines)
        {
            sb.append(trimmedLine);

            if (++count < lines.size())
                sb.append(", ");
        }

        return sb.toString();
    }
}
