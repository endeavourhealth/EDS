package org.endeavourhealth.patientexplorer.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonReportParams {

	private JsonReportParamOptions runDate = null;
	private JsonReportParamOptions snomedCode = null;
	private JsonReportParamOptions originalCode = null;
	private JsonReportParamOptions valueMin = null;
	private JsonReportParamOptions valueMax = null;

	public JsonReportParams() {
	}

	public JsonReportParamOptions getRunDate() {
		return runDate;
	}

	public void setRunDate(JsonReportParamOptions runDate) {
		this.runDate = runDate;
	}

	public JsonReportParamOptions getSnomedCode() {
		return snomedCode;
	}

	public void setSnomedCode(JsonReportParamOptions snomedCode) {
		this.snomedCode = snomedCode;
	}

	public JsonReportParamOptions getOriginalCode() {
		return originalCode;
	}

	public void setOriginalCode(JsonReportParamOptions originalCode) {
		this.originalCode = originalCode;
	}

	public JsonReportParamOptions getValueMin() {
		return valueMin;
	}

	public void setValueMin(JsonReportParamOptions valueMin) {
		this.valueMin = valueMin;
	}

	public JsonReportParamOptions getValueMax() {
		return valueMax;
	}

	public void setValueMax(JsonReportParamOptions valueMax) {
		this.valueMax = valueMax;
	}
}
