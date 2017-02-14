package org.endeavourhealth.patientexplorer.database;

import org.endeavourhealth.patientexplorer.database.models.FieldMetaEntity;
import org.endeavourhealth.patientexplorer.database.models.TableMetaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlEditorProvider {
	private static final Logger LOG = LoggerFactory.getLogger(SqlEditorProvider.class);

	public List<List<String>> runQuery(String sql) throws Exception {
		Connection conn = EnterpriseLiteDb.getConnection();
		PreparedStatement statement = conn.prepareStatement(sql);

		return SqlUtils.getStatementResultsAsCSV(statement);
	}

	public List<TableMetaEntity> getTableData() throws Exception {
		LOG.debug("Fetching enterprise-lite table data");
		List<TableMetaEntity> tables = new ArrayList<>();

		Connection conn = EnterpriseLiteDb.getConnection();
		DatabaseMetaData meta = conn.getMetaData();

		ResultSet tableResultSet = meta.getTables(null, "public", "%", new String[]{"TABLE"});
		buildTableFieldData(tables, meta, tableResultSet);
		tableResultSet.close();

		tableResultSet = meta.getTables(null, "workspace", "%", new String[]{"TABLE"});
		buildTableFieldData(tables, meta, tableResultSet);
		tableResultSet.close();

		return tables;
	}

	private void buildTableFieldData(List<TableMetaEntity> tables, DatabaseMetaData meta, ResultSet tableResultSet) throws SQLException {
		while (tableResultSet.next()) {
			String schemaName = tableResultSet.getString(2);
			String tableName = tableResultSet.getString(3);

			// Exclude enterprise lite reporting tables
			if (!tableName.startsWith("rep_")) {
				TableMetaEntity table = new TableMetaEntity();
				table.setName(schemaName + "." + tableName);

				ResultSet fieldResultSet = meta.getColumns(null, schemaName, tableName, "%");
				while (fieldResultSet.next()) {
					FieldMetaEntity field = new FieldMetaEntity();
					field.setName(fieldResultSet.getString(4));
					table.getFields().add(field);
				}
				tables.add(table);
			}
		}
	}

}
