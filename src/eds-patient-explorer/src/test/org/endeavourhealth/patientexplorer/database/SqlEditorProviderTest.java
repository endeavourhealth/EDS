package org.endeavourhealth.patientexplorer.database;

import org.endeavourhealth.core.data.config.ConfigManager;
import org.junit.Test;

import static org.junit.Assert.*;

public class SqlEditorProviderTest {
	@Test
	public void getTableData() throws Exception {
		ConfigManager.Initialize("eds-patient-explorer");
		SqlEditorProvider prov = new SqlEditorProvider();
		prov.getTableData();
	}

}