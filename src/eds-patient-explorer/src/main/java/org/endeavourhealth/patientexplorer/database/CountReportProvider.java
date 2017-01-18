package org.endeavourhealth.patientexplorer.database;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.data.config.ConfigManager;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.QueryDocument;
import org.endeavourhealth.core.xml.QueryDocumentSerializer;
import org.endeavourhealth.patientexplorer.database.models.ConceptEntity;
import org.endeavourhealth.patientexplorer.models.JsonConcept;
import org.endeavourhealth.patientexplorer.models.JsonReportParams;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CountReportProvider {
	private static final Logger LOG = LoggerFactory.getLogger(CountReportProvider.class);

	private Connection _conn = null;

	public LibraryItem runReport(UUID reportUuid, Map<String,String> reportParams) throws Exception {
		LOG.trace("GettingLibraryItem for UUID {}", reportUuid);
		LibraryRepository repository = new LibraryRepository();
		ActiveItem activeItem = repository.getActiveItemByItemId(reportUuid);
		Item item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());
		LibraryItem countReport = QueryDocumentSerializer.readLibraryItemFromXml(item.getXmlContent());

		// Parse the run date from the report params
		java.util.Date rundate = new SimpleDateFormat("dd/MM/yyyy").parse(reportParams.get("RunDate").replace("'",""));
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(rundate);
		countReport.getCountReport().setLastRun(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));

		countReport.getCountReport().setStatus("Running...");
		saveCountReport(repository, item, countReport);
		try {

			Connection conn = getConnection();
			PreparedStatement statement;

			// Clear old results
			String tablename = "rep_" + reportUuid.toString().replace('-','_');
			LOG.debug("Clearing table " + tablename);
			try {
				statement = conn.prepareStatement("drop table " + tablename);
				statement.execute();
			} catch (PSQLException p) {
				// Ignore - table does not exist
			}

			// Build query
			String query = "SELECT :RunDate as run_date, p.id as internal_patient_id, p.nhs_number";
			if (!countReport.getCountReport().getFields().equals(""))
				query += "," + countReport.getCountReport().getFields();

			query += " FROM patient p JOIN episode_of_care eoc ON eoc.patient_id = p.id ";
			if (countReport.getCountReport().getTables() != null)
				query += countReport.getCountReport().getTables();

			query += " WHERE eoc.date_registered < :RunDate AND (eoc.date_registered_end IS NULL OR eoc.date_registered_end >= :RunDate) ";
			if (countReport.getCountReport().getQuery() != null)
				query += countReport.getCountReport().getQuery();

			query = "CREATE TABLE " + tablename + " AS " + query;

			// Replace parameters
			for(String key : reportParams.keySet()) {
				query = query.replace(":" + key, reportParams.get(key));
			}

			LOG.debug("Executing query " + query);

			statement = conn.prepareStatement(query);
			statement.execute();

			// Get results count
			LOG.debug("Getting result count");
			statement = conn.prepareStatement("SELECT COUNT(*) FROM " + tablename);
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			int rowCount = resultSet.getInt(1);
			LOG.debug("Rows Affected : " + rowCount);
			statement.close();

			// Update library item
			countReport.getCountReport().setCount(rowCount);
			countReport.getCountReport().setStatus("Completed");
			saveCountReport(repository, item, countReport);
		} catch (Exception e) {
			LOG.error("Error running report", e);
			countReport.getCountReport().setStatus("Error");
			saveCountReport(repository, item, countReport);
			throw e;
		}

		return countReport;
	}

	public List<String> getNHSExport(UUID reportUuid) throws Exception {
		return getResults(reportUuid, "run_date, internal_patient_id, nhs_number");
	}

	public List<String> getDataExport(UUID reportUuid) throws Exception {
		return getResults(reportUuid, "*");
	}

	private List<String> getResults(UUID reportUuid, String fields) throws Exception {
		List<String> result = new ArrayList<>();
		String tablename = "rep_" + reportUuid.toString().replace('-','_');

		Connection conn = getConnection();
		PreparedStatement statement = conn.prepareStatement("SELECT "+fields+" FROM " + tablename);
		ResultSet resultSet = statement.executeQuery();

		String row = "";
		for(int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
			if (i > 1)
				row += ",";
			row += resultSet.getMetaData().getColumnLabel(i);
		}
		result.add(row);

		while (resultSet.next()) {
			row = resultSet.getString(1);

			for(int i = 2; i <= resultSet.getMetaData().getColumnCount(); i++) {
				row += "," + resultSet.getString(i);
			}

			result.add(row);
		}
		statement.close();

		return result;
	}

	public List<ConceptEntity> getEncounterTypes() throws Exception {
		List<ConceptEntity> result = new ArrayList<>();
		Connection conn = getConnection();

		PreparedStatement statement = conn.prepareStatement("SELECT DISTINCT snomed_concept_id FROM encounter");
		ResultSet resultSet = statement.executeQuery();

		while (resultSet.next()) {
			ConceptEntity concept = new ConceptEntity();
			concept.setCode(resultSet.getString(1));
			concept.setDisplay("Loading...");
			result.add(concept);
		}

		return result;
	}

	private void saveCountReport(LibraryRepository repository, Item item, LibraryItem libraryItem){
		LOG.debug("Saving count report " + libraryItem.getUuid());
		QueryDocument doc = new QueryDocument();
		doc.getLibraryItem().add(libraryItem);
		item.setXmlContent(QueryDocumentSerializer.writeToXml(doc));
		List<Object> toSave = new ArrayList<>();
		toSave.add(item);
		repository.save(toSave);
	}

	private Connection getConnection() throws SQLException, IOException {
		if (_conn == null) {
			JsonNode config = ConfigManager.getConfigurationAsJson("postgres", "enterprise");
			String url = config.get("url").asText();
			String username = config.get("username").asText();
			String password = config.get("password").asText();

			_conn = DriverManager.getConnection(url, username, password);
		}
		return _conn;
	}
}
