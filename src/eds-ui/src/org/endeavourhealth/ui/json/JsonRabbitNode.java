package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ser.std.InetAddressSerializer;
import org.endeavourhealth.core.data.admin.models.Service;

import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonRabbitNode {
    private String address = null;
    private Integer ping = null;

    public JsonRabbitNode() {
    }

    public JsonRabbitNode(String address, Integer ping) {
        this.address = address;
        this.ping = ping;
    }

    /**
     * gets/sets
     */

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPing() {
        return ping;
    }

    public void setPing(Integer ping) {
        this.ping = ping;
    }

}
