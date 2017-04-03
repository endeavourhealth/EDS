package org.endeavourhealth.transform.emis.emisopen.transforms.common;

import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.AddressType;
import org.hl7.fhir.instance.model.Address;

import static org.endeavourhealth.common.fhir.AddressConverter.createAddress;

public class AddressConverter
{
    public static Address convert(AddressType addressType, Address.AddressUse addressUse)
    {
        return createAddress(addressUse, addressType.getHouseNameFlat(), addressType.getStreet(), addressType.getVillage(), addressType.getTown(), addressType.getCounty(), addressType.getPostCode());
    }
}
