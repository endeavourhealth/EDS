package org.endeavourhealth.patientexplorer.database;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.config.ConfigManager;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.QueryDocument;
import org.endeavourhealth.core.xml.QueryDocumentSerializer;
import org.endeavourhealth.patientexplorer.database.models.ConceptEntity;
import org.endeavourhealth.patientexplorer.models.JsonPractitioner;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeFactory;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class CountReportProvider {
	private static final Logger LOG = LoggerFactory.getLogger(CountReportProvider.class);

	private Connection _conn = null;

	public LibraryItem runReport(UUID reportUuid, UUID organisationUuid, Map<String,String> reportParams) throws Exception {
		ServiceRepository svcRepo = new ServiceRepository();
		Service svc = svcRepo.getById(organisationUuid);
		String odsCode = svc.getLocalIdentifier();

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
			PreparedStatement statement = null;

			// Clear old results
			String tablename = "rep_" + reportUuid.toString().replace('-','_');
			dropReportTable(conn, tablename);

			// Build query
			createReportTable(reportParams, odsCode, countReport, conn, tablename);

			int rowCount = getReportItemCount(countReport, conn, statement, tablename);

			// Update library item
			countReport.getCountReport().setCount(rowCount);
			countReport.getCountReport().setStatus("Completed");
			saveCountReport(repository, item, countReport);

		} catch (SQLException e) {
			LOG.error("Error running report", e);
			countReport.getCountReport().setStatus("Error");
			saveCountReport(repository, item, countReport);
			throw e;
		}

		return countReport;
	}

	private int getReportItemCount(LibraryItem countReport, Connection conn, PreparedStatement statement, String tablename) throws SQLException {
		try {
			// Get results count
			LOG.debug("Getting result count");
			statement = conn.prepareStatement("SELECT COUNT(*) FROM " + tablename);
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			int rowCount = resultSet.getInt(1);
			LOG.debug("Rows Affected : " + rowCount);
			return rowCount;
		} finally {
			statement.close();
		}
	}

	private void  createReportTable(Map<String, String> reportParams, String odsCode, LibraryItem countReport, Connection conn, String tablename) throws SQLException {
		String query = "SELECT :RunDate as run_date, p.id as internal_patient_id, p.nhs_number";
		if (!countReport.getCountReport().getFields().equals(""))
			query += "," + countReport.getCountReport().getFields();

		query += " FROM patient p JOIN episode_of_care eoc ON eoc.patient_id = p.id ";
		query += " JOIN organization org ON org.id = p.organization_id ";
		if (countReport.getCountReport().getTables() != null)
			query += countReport.getCountReport().getTables();

		query += " WHERE eoc.date_registered < :RunDate AND (eoc.date_registered_end IS NULL OR eoc.date_registered_end >= :RunDate) ";
		query += " AND org.ods_code = '" + odsCode + "' ";
		if (countReport.getCountReport().getQuery() != null)
			query += countReport.getCountReport().getQuery();

		query = "CREATE TABLE " + tablename + " AS " + query;

		// Replace parameters
		for (Map.Entry<String, String> entry : reportParams.entrySet()) {
			query = query.replace(":" + entry.getKey(), entry.getValue());
		}

		LOG.debug("Executing query " + query);

		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement(query);
			statement.execute();
		} finally {
			if (statement != null)
				statement.close();
		}
	}

	private void dropReportTable(Connection conn, String tablename) throws SQLException {
		LOG.debug("Clearing table " + tablename);
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement("drop table " + tablename);
			statement.execute();
		} catch (PSQLException p) {
			// Ignore - table does not exist
		} finally {
			if (statement != null)
				statement.close();
		}
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
		try {
			ResultSet resultSet = statement.executeQuery();

			StringBuffer row = new StringBuffer();
			for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
				if (i > 1)
					row.append(",");
				row.append(resultSet.getMetaData().getColumnLabel(i));
			}
			result.add(row.toString());

			while (resultSet.next()) {
				row.setLength(0);
				row.append(resultSet.getString(1));

				for (int i = 2; i <= resultSet.getMetaData().getColumnCount(); i++) {
					row.append("," + resultSet.getString(i));
				}

				result.add(row.toString());
			}
		} finally {
			statement.close();
		}

		return result;
	}

	public List<ConceptEntity> getEncounterTypes() throws Exception {
		Connection conn = getConnection();

		PreparedStatement statement = conn.prepareStatement("SELECT DISTINCT snomed_concept_id, original_term FROM encounter");
		try {
			ResultSet resultSet = statement.executeQuery();
			return buildConceptEntityList(resultSet);
		} finally {
			statement.close();
		}
	}

	public List<ConceptEntity> getReferralTypes() throws Exception {
		Connection conn = getConnection();

		PreparedStatement statement = conn.prepareStatement("SELECT id, value FROM referral_request_type");
		try {
			ResultSet resultSet = statement.executeQuery();
			return buildConceptEntityList(resultSet);
		} finally {
			statement.close();
		}
	}

	public List<ConceptEntity> getReferralPriorities() throws Exception {
		Connection conn = getConnection();

		PreparedStatement statement = conn.prepareStatement("SELECT id, value FROM referral_request_priority");
		try {
			ResultSet resultSet = statement.executeQuery();
			return buildConceptEntityList(resultSet);
		} finally {
			statement.close();
		}
	}

	public List<JsonPractitioner> searchPractitioner(String searchData, UUID organisationUuid) throws Exception {
		ServiceRepository svcRepo = new ServiceRepository();
		Service svc = svcRepo.getById(organisationUuid);
		String odsCode = svc.getLocalIdentifier();

		List<JsonPractitioner> result = new ArrayList<>();
		Connection conn = getConnection();

		PreparedStatement statement = conn.prepareStatement("SELECT p.id, p.name FROM practitioner p JOIN organization o ON o.id = p.organization_id WHERE o.ods_code = ? AND p.name LIKE ?");

		try {
			statement.setString(1, odsCode);
			statement.setString(2, "%" + searchData + "%");
			ResultSet resultSet = statement.executeQuery();

			while (resultSet.next()) {
				JsonPractitioner practitioner = new JsonPractitioner();
				practitioner.setId(resultSet.getInt(1));
				practitioner.setName(resultSet.getString(2));
				result.add(practitioner);
			}
		} finally {
			statement.close();
		}

		return result;
	}

	private List<ConceptEntity> buildConceptEntityList(ResultSet resultSet) throws SQLException {
		List<ConceptEntity> result = new ArrayList<>();

		while (resultSet.next()) {
			ConceptEntity concept = new ConceptEntity();
			concept.setCode(resultSet.getString(1));
			if (resultSet.getString(2) == null)
				concept.setDisplay("[NULL]");
			else
				concept.setDisplay(resultSet.getString(2));
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
