package org.endeavourhealth.patientexplorer.database;

import org.endeavourhealth.common.config.ConfigManager;
import org.junit.Test;

public class SqlEditorProviderTest {
	@Test
	public void getTableData() throws Exception {
		ConfigManager.Initialize("eds-patient-explorer");
		SqlEditorProvider prov = new SqlEditorProvider();
		prov.getTableData();
	}

}