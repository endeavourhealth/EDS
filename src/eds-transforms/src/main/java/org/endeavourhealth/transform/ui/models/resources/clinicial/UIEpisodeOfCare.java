package org.endeavourhealth.transform.ui.models.resources.clinicial;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.resources.admin.UIOrganisation;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPatient;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.types.UIPeriod;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIEpisodeOfCare extends UIResource<UIEpisodeOfCare> {
	private String status;
	private UIPatient patient;
	private UIOrganisation managingOrganisation;
	private UIPeriod period;
	private UIPractitioner careManager;

	public String getStatus() {
		return status;
	}

	public UIEpisodeOfCare setStatus(String status) {
		this.status = status;
		return this;
	}

	public UIPatient getPatient() {
		return patient;
	}

	public UIEpisodeOfCare setPatient(UIPatient patient) {
		this.patient = patient;
		return this;
	}

	public UIOrganisation getManagingOrganisation() {
		return managingOrganisation;
	}

	public UIEpisodeOfCare setManagingOrganisation(UIOrganisation managingOrganisation) {
		this.managingOrganisation = managingOrganisation;
		return this;
	}

	public UIPeriod getPeriod() {
		return period;
	}

	public UIEpisodeOfCare setPeriod(UIPeriod period) {
		this.period = period;
		return this;
	}

	public UIPractitioner getCareManager() {
		return careManager;
	}

	public UIEpisodeOfCare setCareManager(UIPractitioner careManager) {
		this.careManager = careManager;
		return this;
	}
}
