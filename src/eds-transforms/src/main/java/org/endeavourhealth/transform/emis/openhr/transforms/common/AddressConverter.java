package org.endeavourhealth.transform.emis.openhr.transforms.common;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.DtAddress;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001Person;
import org.endeavourhealth.transform.emis.openhr.schema.VocAddressType;
import org.hl7.fhir.instance.model.Address;

import java.util.ArrayList;
import java.util.List;

import static org.endeavourhealth.common.fhir.AddressConverter.createAddress;

public class AddressConverter
{
    public static List<Address> convert(List<DtAddress> addressList)
    {
        if (addressList == null || addressList.isEmpty())
            return null;

        List<Address> targetAddressList = new ArrayList<>();

        for (DtAddress address: addressList)
        {
            Address targetAddress = convert(address);

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

        Address target = createAddress(convertAddressType(source.getAddressType()), source.getHouseNameFlat(), source.getStreet(), source.getVillage(), source.getTown(), source.getCounty(), source.getPostCode());

        return target;
    }

    public static List<Address> convertFromPersonAddress(List<OpenHR001Person.Address> addressList) throws TransformException
    {
        if (addressList == null || addressList.isEmpty())
            return null;

        List<Address> targetAddressList = new ArrayList<>();

        for (OpenHR001Person.Address address: addressList)
        {
            OpenHRHelper.ensureDboNotDelete(address.getUpdateMode());

            Address targetAddress = convert(address);

            if (targetAddress != null)
                targetAddressList.add(targetAddress);
        }

        if (targetAddressList.isEmpty())
            return null;
        else
            return targetAddressList;
    }

    private static Address.AddressUse convertAddressType(VocAddressType sourceAddressType)
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
