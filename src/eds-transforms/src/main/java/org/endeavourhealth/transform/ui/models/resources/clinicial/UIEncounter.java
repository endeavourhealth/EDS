package org.endeavourhealth.transform.ui.models.resources.clinicial;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.resources.admin.UIAppointment;
import org.endeavourhealth.transform.ui.models.resources.admin.UILocation;
import org.endeavourhealth.transform.ui.models.resources.admin.UIOrganisation;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.types.UICodeableConcept;
import org.endeavourhealth.transform.ui.models.types.UIDate;
import org.endeavourhealth.transform.ui.models.types.UIPeriod;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIEncounter extends UIResource<UIEncounter> {
    private String status;
    private String class_;
    private List<UICodeableConcept> types;
    private UIAppointment appointment;
    private UIPractitioner performedBy;
    private UIPeriod period;
    private UIOrganisation serviceProvider;
    private UICodeableConcept encounterSource;
    private UIPractitioner recordedBy;
    private UIDate recordedDate;
    private List<UICodeableConcept> reason;
    private UILocation location;

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

    public String getClass_() { return class_; }

    public UIEncounter setClass_(String class_) {
    	this.class_ = class_;
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

    public UIDate getRecordedDate() {
        return recordedDate;
    }

    public UIEncounter setRecordedDate(UIDate recordedDate) {
        this.recordedDate = recordedDate;
        return this;
    }

    public List<UICodeableConcept> getReason() {
        return reason;
    }

    public UIEncounter setReason(List<UICodeableConcept> reason) {
        this.reason = reason;
        return this;
    }

	public List<UICodeableConcept> getTypes() {
		return types;
	}

	public UIEncounter setTypes(List<UICodeableConcept> types) {
		this.types = types;
		return this;
	}

	public UILocation getLocation() {
		return location;
	}

	public UIEncounter setLocation(UILocation location) {
		this.location = location;
		return this;
	}
}
