package org.endeavourhealth.transform.ui.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JsonEncounter {
    private UUID patientId;
    private String status;
    private List<JsonEncounterParticipant> participants = new ArrayList<>();
    private JsonPeriod period;
    private List<JsonCode> reason = new ArrayList<>();

    public UUID getPatientId() {
        return patientId;
    }

    public JsonEncounter setPatientId(UUID patientId) {
        this.patientId = patientId;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public JsonEncounter setStatus(String status) {
        this.status = status;
        return this;
    }

    public List<JsonEncounterParticipant> getParticipants() {
        return participants;
    }

    public JsonEncounter setParticipants(List<JsonEncounterParticipant> participants) {
        this.participants = participants;
        return this;
    }

    public JsonPeriod getPeriod() {
        return period;
    }

    public JsonEncounter setPeriod(JsonPeriod period) {
        this.period = period;
        return this;
    }

    public List<JsonCode> getReason() {
        return reason;
    }

    public JsonEncounter setReason(List<JsonCode> reason) {
        this.reason = reason;
        return this;
    }
}
