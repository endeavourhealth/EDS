package org.endeavourhealth.patientexplorer.database.models;

import java.util.ArrayList;
import java.util.List;

public class TableMetaEntity {
	private String name;
	private List<FieldMetaEntity> fields;

	public TableMetaEntity() {
		this.fields = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<FieldMetaEntity> getFields() {
		return fields;
	}

	public void setFields(List<FieldMetaEntity> fields) {
		this.fields = fields;
	}
}
