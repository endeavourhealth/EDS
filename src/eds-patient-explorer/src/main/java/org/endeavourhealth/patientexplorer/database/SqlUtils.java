package org.endeavourhealth.patientexplorer.database;

import org.apache.commons.lang3.StringUtils;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SqlUtils {
	public static List<List<String>> getStatementResultsAsCSV(PreparedStatement statement) throws SQLException {
		List<List<String>> result = new ArrayList<>();
		try {
			if (statement.execute()) {
				ResultSet resultSet = statement.getResultSet();
				List<String> row = new ArrayList<>();
				for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
					row.add(resultSet.getMetaData().getColumnLabel(i));
				}
				result.add(row);

				while (resultSet.next()) {
					row = new ArrayList<>();
					for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
						row.add(resultSet.getString(i));
					}

					result.add(row);
				}
			}
		} finally {
			statement.close();
		}
		return result;
	}

	public static String getCSVAsString(List<List<String>> csv) {
		List<String> rows = new ArrayList<>();
		for(List<String> row : csv) {
			rows.add(StringUtils.join(row.toArray(), ','));
		}
		String ret = StringUtils.join(rows, '\n');

		return ret;
	}

	public static Date sqlDateFromString(String dateString) throws ParseException {
		dateString = dateString.replace("'","");
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		Date sqlDate = new Date(df.parse(dateString).getTime());
		return sqlDate;
	}
}
