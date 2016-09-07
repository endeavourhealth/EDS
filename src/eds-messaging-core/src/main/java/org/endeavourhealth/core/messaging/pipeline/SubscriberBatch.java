package org.endeavourhealth.core.messaging.pipeline;

import org.endeavourhealth.core.xml.QueryDocument.TechnicalInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SubscriberBatch {
	private TechnicalInterface technicalInterface;
	private UUID outputMessageId;
	private List<String> endpoints;

	public SubscriberBatch() {
		endpoints = new ArrayList<>();
	}


	public List<String> getEndpoints() {
		return endpoints;
	}

	public UUID getOutputMessageId() {
		return outputMessageId;
	}

	public void setOutputMessageId(UUID outputMessageId) {
		this.outputMessageId = outputMessageId;
	}

	public TechnicalInterface getTechnicalInterface() {
		return technicalInterface;
	}

	public void setTechnicalInterface(TechnicalInterface technicalInterface) {
		this.technicalInterface = technicalInterface;
	}
}
