package org.endeavourhealth.transform.ui.models.resources.clinicial;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.resources.admin.UIAppointment;
import org.endeavourhealth.transform.ui.models.resources.admin.UIOrganisation;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.types.UICodeableConcept;
import org.endeavourhealth.transform.ui.models.types.UIPeriod;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIEncounter extends UIResource<UIEncounter> {
    private String status;
    private UIAppointment appointment;
    private UIPractitioner performedBy;
    private UIPeriod period;
    private UIOrganisation serviceProvider;
    private UICodeableConcept encounterSource;
    private UIPractitioner recordedBy;
    private Date recordedDate;

    public UIAppointment getAppointment() {
        return appointment;
    }

    public UIEncounter setAppointment(UIAppointment appointment) {
        this.appointment = appointment;
        return this;
    }

    public UIOrganisation getServiceProvider() {
        return serviceProvider;
    }

    public UIEncounter setServiceProvider(UIOrganisation serviceProvider) {
        this.serviceProvider = serviceProvider;
        return this;
    }

    public UICodeableConcept getEncounterSource() {
        return encounterSource;
    }

    public UIEncounter setEncounterSource(UICodeableConcept encounterSource) {
        this.encounterSource = encounterSource;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public UIEncounter setStatus(String status) {
        this.status = status;
        return this;
    }

    public UIPractitioner getPerformedBy() {
        return performedBy;
    }

    public UIEncounter setPerformedBy(UIPractitioner performedBy) {
        this.performedBy = performedBy;
        return this;
    }

    public UIPeriod getPeriod() {
        return period;
    }

    public UIEncounter setPeriod(UIPeriod period) {
        this.period = period;
        return this;
    }

    public UIPractitioner getRecordedBy() {
        return recordedBy;
    }

    public UIEncounter setRecordedBy(UIPractitioner recordedBy) {
        this.recordedBy = recordedBy;
        return this;
    }

    public Date getRecordedDate() {
        return recordedDate;
    }

    public UIEncounter setRecordedDate(Date recordedDate) {
        this.recordedDate = recordedDate;
        return this;
    }
}
