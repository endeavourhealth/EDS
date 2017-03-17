package org.endeavourhealth.coreui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.core.mySQLDatabase.models.AddressEntity;
import org.endeavourhealth.core.mySQLDatabase.models.RegionEntity;

import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonMarker {
    private String name = null;
    private Double lat = null;
    private Double lng = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}