package org.endeavourhealth.queuereader.routines;

import OpenPseudonymiser.Crypto;
import com.fasterxml.jackson.databind.JsonNode;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.database.rdbms.enterprise.EnterpriseConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Thread.sleep;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

public abstract class COVIDVaccineEvents extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(COVIDVaccineEvents.class);

    private static Hashtable<String, String> NHSNumbers = new Hashtable<String, String>();

    public static void Start(String gpconfigName, String hospitalconfigName, String hospitalOrganizationId, String sourceFile, String u, String p) throws Exception
    {
        Scanner s = new Scanner(System.in);
        boolean quit = false;

        System.out.println("bartsvss_nel will get truncated!");
        String yn = "";

        while (!quit) {
            System.out.println("Continue (y/n)");
            yn = s.nextLine();
            if (yn.toLowerCase().equals("n") || yn.toLowerCase().equals("y")) {quit = true;}
        }

        if (yn.toLowerCase().equals("n")) return;

        System.out.println("running GetPseudoExtract");
        GetPseudoExtract(gpconfigName, sourceFile);

        System.out.println("running LoadFilev2");
        LoadFilev2("db_subscriber:"+gpconfigName, "bartsvss_nel", "/tmp/"+gpconfigName+".txt", u, p);
    }

    private static void GetPseudoExtract(String gpconfigName, String sourceFile) throws Exception {
        Hashtable<String, String> pseudoNhsNumbers = new Hashtable<String, String>();

        String pseudoNhsNumber;

        // load source file
        BufferedReader csvReader = new BufferedReader(new FileReader(sourceFile));
        while ((pseudoNhsNumber = csvReader.readLine()) != null) {
            pseudoNhsNumbers.put(pseudoNhsNumber, "");
            System.out.println(pseudoNhsNumber);
        }
        csvReader.close();

        String tFile = "/tmp/" + gpconfigName + ".txt";
        FileWriter tabWriter = new FileWriter(tFile);

        String base64Salt = ConfigManager.getConfiguration("barts-salt", "vacc-events");
        byte[] saltBytes = Base64.getDecoder().decode(base64Salt);

        List<EnterpriseConnector.ConnectionWrapper> hospdb_connectionWrappers = EnterpriseConnector.openSubscriberConnections(gpconfigName);
        EnterpriseConnector.ConnectionWrapper hospdb_connectionWrapper = hospdb_connectionWrappers.get(0);

        Connection subscriberConnection = hospdb_connectionWrapper.getConnection();

        String sql = "select id, nhs_number from person";
        System.out.println(sql);

        PreparedStatement ps = subscriberConnection.prepareStatement(sql);

        ResultSet rs = ps.executeQuery();

        Integer id = 1;

        while (rs.next()) {
            String nhs_number = rs.getString("nhs_number");
            String person_id = rs.getString("id");

            if (nhs_number == null) continue;

            pseudoNhsNumber = EncryptNHSNumbers(nhs_number, saltBytes);

            // does this pseudo_id exist in the source file?
            if (!pseudoNhsNumbers.containsKey(pseudoNhsNumber)) continue;

            System.out.println(pseudoNhsNumber);

            tabWriter.append(id + "\t" + person_id + "\t" + nhs_number + "\t" + pseudoNhsNumber + "\n");
            tabWriter.flush();

            id++;
        }

        tabWriter.close();
        ps.close();
    }

    private static boolean ValidateTable(String Schema, String Table, Connection connection)  throws SQLException {

        String q="select * from information_schema.tables where TABLE_SCHEMA=? and TABLE_NAME=?";

        PreparedStatement preparedStatement = connection.prepareStatement(q);
        preparedStatement.setString(1,Schema);
        preparedStatement.setString(2,Table);

        ResultSet rs = preparedStatement.executeQuery();

        boolean ret=true;
        if (!rs.next()) {System.out.println("Table "+Table+" does not exist"); ret=false;}
        preparedStatement.close();
        return ret;
    }

    public static void LoadFilev2(String configName, String table, String sourceFile, String username, String password) throws SQLException, IOException {

        if (!table.contains("vss")) {
            System.out.println("table must contain vss");
            return;
        }

        JsonNode jurl = ConfigManager.getConfigurationAsJson(configName, "db_subscriber");

        String url = jurl.get("url").asText();

        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);

        dataSource.setReadOnlyPropagatesToServer(true);
        dataSource.setAllowLoadLocalInfile(true);
        dataSource.setAllowMultiQueries(true);

        Connection connection = dataSource.getConnection();

        Boolean v = ValidateTable("data_extracts",table,connection);
        if (isFalse(v)) {return;}

        // check that the code tables exist
        v = ValidateTable("data_extracts", "bartsvssjabs", connection);
        if (isFalse(v)) {return;}
        v = ValidateTable("data_extracts", "bartsvssobs", connection);
        if (isFalse(v)) {return;}

        System.out.println("Filing "+table);

        String q = "truncate table data_extracts." + table + "; load data local infile '" + sourceFile + "' into table data_extracts." + table + ";";

        PreparedStatement ps = connection.prepareStatement(q);
        ps.execute();

        ps.close();
        connection.close();
    }

    private static String EncryptNHSNumbers(String sNhsNumber, byte[] saltBytes) throws Exception {
        // encrypt all the NHS numbers in the database
        // key/value pair: pseudo_nhs_number = nhs_number
        TreeMap<String, String> keys = new TreeMap<>();

        keys.put("nhs_number", "" + sNhsNumber);
        Crypto crypto = new Crypto();
        crypto.SetEncryptedSalt(saltBytes);

        String pseudoNhsNumber = crypto.GetDigest(keys);

        return pseudoNhsNumber;
    }
}
