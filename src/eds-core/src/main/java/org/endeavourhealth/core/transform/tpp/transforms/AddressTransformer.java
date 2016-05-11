package org.endeavourhealth.core.transform.tpp.transforms;

import com.google.common.base.Strings;
import org.hl7.fhir.instance.model.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressTransformer {

    public static Address tranformHomeAddress(org.endeavourhealth.core.transform.tpp.schema.Address tppAddress) {

        String houseName = tppAddress.getHouseName();
        String houseNum = tppAddress.getHouseNumber();
        String road = tppAddress.getRoad();
        String locality = tppAddress.getLocality();
        String town = tppAddress.getTown();
        String county = tppAddress.getCounty();
        String postcode = tppAddress.getPostcode();
        List<String> tokens = new ArrayList<>();

        Address ret = new Address();
        ret.setUse(Address.AddressUse.HOME); //address type isn't provided, but assume it's the home address
        ret.setType(Address.AddressType.BOTH);

        if (!Strings.isNullOrEmpty(houseName)) {
            ret.addLine(houseName);
            tokens.add(houseName);
        }

        if (!Strings.isNullOrEmpty(houseNum) && !Strings.isNullOrEmpty(road)) {
            String s = houseNum + " " + road;
            ret.addLine(s);
            tokens.add(s);
        } else if (!Strings.isNullOrEmpty(road)) {
            ret.addLine(road);
            tokens.add(road);
        }

        if (!Strings.isNullOrEmpty(locality)) {
            ret.addLine(locality);
            tokens.add(locality);
        }

        if (!Strings.isNullOrEmpty(town)) {
            ret.setCity(town);
            tokens.add(town);
        }

        if (!Strings.isNullOrEmpty(county)) {
            ret.setDistrict(county);
            tokens.add(county);
        }

        if (!Strings.isNullOrEmpty(postcode)) {
            ret.setPostalCode(postcode);
            tokens.add(postcode);
        }

        ret.setText(String.join(" ", tokens));

        return ret;
    }
}
