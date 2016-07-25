package org.endeavourhealth.transform.tpp.xml.transforms;

import com.google.common.base.Strings;
import org.hl7.fhir.instance.model.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressTransformer {

    public static Address tranformWorkAddress(org.endeavourhealth.transform.tpp.xml.schema.Address tppAddress) {
        return tranformAddress(tppAddress, Address.AddressUse.WORK);
    }

    public static Address tranformHomeAddress(org.endeavourhealth.transform.tpp.xml.schema.Address tppAddress) {
        return tranformAddress(tppAddress, Address.AddressUse.HOME);
    }
    public static Address tranformAddress(org.endeavourhealth.transform.tpp.xml.schema.Address tppAddress, Address.AddressUse use) {
        String houseName = tppAddress.getHouseName();
        String houseNum = tppAddress.getHouseNumber();
        String road = tppAddress.getRoad();
        String locality = tppAddress.getLocality();
        String town = tppAddress.getTown();
        String county = tppAddress.getCounty();
        String postcode = tppAddress.getPostcode();
        List<String> tokens = new ArrayList<>();

        Address ret = new Address();
        ret.setUse(use);
        ret.setType(Address.AddressType.BOTH); //assume all addresses are pbysical and postal

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
