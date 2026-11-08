package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonQueueReaderCommand {

    private List<String> hostNames;
    private String command;

    public JsonQueueReaderCommand() {
    }

    public List<String> getHostNames() {
        return hostNames;
    }

    public void setHostNames(List<String> hostNames) {
        this.hostNames = hostNames;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
