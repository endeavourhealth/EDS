package org.endeavourhealth.transform.emis.openhr.transforms.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.DtAddress;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001Person;
import org.endeavourhealth.transform.emis.openhr.schema.VocAddressType;
import org.endeavourhealth.transform.emis.openhr.transforms.helpers.OpenHRHelper;
import org.hl7.fhir.instance.model.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressConverter
{
    public static List<Address> convert(List<DtAddress> addressList)
    {
        if (addressList == null || addressList.isEmpty())
            return null;

        List<Address> targetAddressList = new ArrayList<>();

        for (DtAddress address: addressList)
        {
            Address targetAddress = convertAddress(address);

            if (targetAddress != null)
                targetAddressList.add(targetAddress);
        }

        if (targetAddressList.isEmpty())
            return null;
        else
            return targetAddressList;
    }

    public static List<Address> convertFromPersonAddress(List<OpenHR001Person.Address> addressList) throws TransformException
    {
        if (addressList == null || addressList.isEmpty())
            return null;

        List<Address> targetAddressList = new ArrayList<>();

        for (OpenHR001Person.Address address: addressList)
        {
            OpenHRHelper.ensureDboNotDelete(address.getUpdateMode());

            Address targetAddress = convertAddress(address);

            if (targetAddress != null)
                targetAddressList.add(targetAddress);
        }

        if (targetAddressList.isEmpty())
            return null;
        else
            return targetAddressList;
    }

    public static Address convert(DtAddress source)
    {
        if (source == null)
            return null;

        return convertAddress(source);
    }

    private static Address convertAddress(DtAddress source)
    {
        Address.AddressUse use = ConvertAddressType(source.getAddressType());

        Address target = new Address();

        if ((use != null) && (use != Address.AddressUse.NULL))
            target.setUse(use);

        target.setText(createDisplayText(source));

        addLines(source, target);

        if (StringUtils.isNotBlank(source.getTown()))
            target.setCity(source.getTown());

        if (StringUtils.isNotBlank(source.getCounty()))
            target.setDistrict(source.getCounty());

        if (StringUtils.isNotBlank(source.getPostCode()))
            target.setPostalCode(source.getPostCode());

        return target;
    }

    private static String createDisplayText(DtAddress source)
    {
        StringBuilder sb = new StringBuilder();

        if (StringUtils.isNotBlank(source.getHouseNameFlat()))
        {
            sb.append(source.getHouseNameFlat());
            sb.append(", ");
        }

        if (StringUtils.isNotBlank(source.getStreet()))
        {
            sb.append(source.getStreet());
            sb.append(", ");
        }

        if (StringUtils.isNotBlank(source.getVillage()))
        {
            sb.append(source.getVillage());
            sb.append(", ");
        }

        if (StringUtils.isNotBlank(source.getTown()))
        {
            sb.append(source.getTown());
            sb.append(", ");
        }

        if (StringUtils.isNotBlank(source.getCounty()))
        {
            sb.append(source.getCounty());
            sb.append(", ");
        }

        if (StringUtils.isNotBlank(source.getPostCode()))
            sb.append(source.getPostCode());

        return sb.toString();
    }

    private static void addLines(DtAddress source, Address target)
    {
        if (StringUtils.isNotBlank(source.getHouseNameFlat()))
            target.addLine(source.getHouseNameFlat());

        if (StringUtils.isNotBlank(source.getStreet()))
            target.addLine(source.getStreet());

        if (StringUtils.isNotBlank(source.getVillage()))
            target.addLine(source.getVillage());
    }

    private static Address.AddressUse ConvertAddressType(VocAddressType sourceAddressType)
    {
        if (sourceAddressType == null)
            return Address.AddressUse.NULL;

        switch (sourceAddressType)
        {
            case H:
                return Address.AddressUse.HOME;
            case W:
                return Address.AddressUse.WORK;
            case TMP:
                return Address.AddressUse.TEMP;
            case L:
                return Address.AddressUse.OLD;
            default:
                return Address.AddressUse.NULL;
        }
    }
}
