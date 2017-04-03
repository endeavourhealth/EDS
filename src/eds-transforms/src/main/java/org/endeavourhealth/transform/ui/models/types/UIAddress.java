package org.endeavourhealth.transform.ui.models.types;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIAddress {
    private String line1;
    private String line2;
    private String line3;
    private String city;
    private String district;
    private String postalCode;

    public String getLine1() {
        return line1;
    }

    public UIAddress setLine1(String line1) {
        this.line1 = line1;
        return this;
    }

    public String getLine2() {
        return line2;
    }

    public UIAddress setLine2(String line2) {
        this.line2 = line2;
        return this;
    }

    public String getLine3() {
        return line3;
    }

    public UIAddress setLine3(String line3) {
        this.line3 = line3;
        return this;
    }

    public String getCity() {
        return city;
    }

    public UIAddress setCity(String city) {
        this.city = city;
        return this;
    }

    public String getDistrict() {
        return district;
    }

    public UIAddress setDistrict(String district) {
        this.district = district;
        return this;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public UIAddress setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }
}
