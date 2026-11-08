package org.endeavourhealth.queuereader.routines;

import com.fasterxml.jackson.databind.JsonNode;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OutputEncryptor;
import org.endeavourhealth.common.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.*;
import java.util.*;
import java.util.Date;

import static java.lang.Thread.sleep;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

public abstract class HealthChecks extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(HealthChecks.class);

    private static Hashtable<String, String> NHSNumbers = new Hashtable<String, String>();

    private static String HCCodes;
    private static String RiskCodes;

    public static void Start(String configName, String eligibleTable, String ineligibleTable) throws Exception
    {
        System.out.println("running FixIneligibles");
        FixIneligibles("db_subscriber:"+configName, eligibleTable, ineligibleTable);
    }

    public static boolean indexInBound(String[] data, int index) {
        return data != null && index >= 0 && index < data.length;
    }

    public static String Piece(String str, String del, Integer from, Integer to) {
        Integer i;

        if (!str.contains(del)) {return str;}

        String p[] = str.split(del, -1);
        String z = "";

        from = from - 1;
        to = to - 1;

        Integer zdel = 0;
        if (to > from) {
            zdel = 1;
        }

        for (i = from; i <= to; i++) {
            if (indexInBound(p, i)) {
                z = z + p[i];
                if (zdel.equals(1)) {
                    z = z + del;
                }
            }
        }

        if (zdel.equals((1)) && !z.isEmpty()) {
            // remove delimeter
            z = z.substring(0, z.length() - del.length());
        }

        return z;
    }

    public static String justify(String str)
    {
        // ("00000000" + "Apple").substring("Apple".length())
        if (str.length()>2) return str;
        str = ("00"+str).substring(str.length());
        return str;
    }

    // if we ever need to convert the horolog dates to dates!
    public static String HD(Integer date)
    {
        String z = "";
        if (date>21914) date = date+1;
        Integer leap = Math.round(date/1461);

        Integer r = date % 1461;

        Integer year = leap*4+1841+(Math.round(r/365));
        Integer day = r % 365;
        Integer month = 1;

        if (r == 1460 && leap != 14) {day=365; year=year-1;}

        Integer x = 28;
        if (r>1154 && leap !=14) {x=29;}

        Integer var[] = {31,x,31,30,31,30,31,31,30,31,30};
        for(int i=0; i<var.length ;i++) {
            Integer loop = var[i];
            if (loop>day || loop == day) break;
            month = month+1;
            day = day-loop;
        }

        if (day.equals(0)) {year=year-1; month=12; day=31;}
        z = justify(day.toString())+"."+justify(month.toString())+"."+justify(year.toString());

        return z;
    }

    // DD-MM-YY -> $H
    public static Integer DH(String date)
    {
        String year = Piece(date, "-", 1, 1);
        String day = Piece(date, "-", 3, 3);
        String month = Piece(date, "-", 2, 2);

        Integer y = Integer.parseInt(year);
        Integer d = Integer.parseInt(day);
        Integer m = Integer.parseInt(month);

        Integer r = Math.round((y-1)/4)-(Math.round((y-1)/100))+(Math.round((y-1)/400))-446;

        Integer ret = 366*r+((y-1841-r)*365)+d;

        Integer leap = 29;

        if (y%4>0) { leap = 28;}
        else if (y%100>0) {leap = 29;}
        else if (y%400>0) {leap = 28;}

        Integer var[] = {31,leap,31,30,31,30,31,31,30,31,30,31};
        for(int i=0; i<var.length ;i++) {
            m = m - 1;
            if (m.equals(0)) break;
            ret = ret + var[i];
        }

        return ret;
    }

    public static String getLatestHealthCheckDate(String nor, String ineligibleTable, Connection connection) throws SQLException
    {
        String date = "";
        String q = "SELECT * FROM data_extracts."+ineligibleTable+" WHERE original_code in ("+HCCodes+")"+" and patient_id="+nor;

        q = q + " order by clinical_effective_date desc";

        PreparedStatement ps = connection.prepareStatement(q);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            date = rs.getString("clinical_effective_date");
        }
        return date;
    }

    public static String getLatestQRISKScore(String nor, String ineligibleTable, Connection connection) throws SQLException
    {
        String score = "0"; String date = "";
        String q = "SELECT * FROM data_extracts."+ineligibleTable+" WHERE original_code in ("+RiskCodes+") and patient_id="+nor;
        q = q + " order by clinical_effective_date desc, result_value desc";

        PreparedStatement ps = connection.prepareStatement(q);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            score = rs.getString("result_value");
            date = rs.getString("clinical_effective_date");
        }

        ps.close();
        return score+"~"+date;
    }

    // fix counts
    public static void DeleteFromIneligibleCohort(String patientId, Connection connection) throws SQLException
    {
        String q = "DELETE FROM data_extracts.tmp_health_checks_cohort_ineligible WHERE id=?";

        PreparedStatement preparedStmt = connection.prepareStatement(q);
        preparedStmt.setString(1,patientId);
        preparedStmt.execute();
        preparedStmt.close();
    }

    public static void UpdateEligibleCounts(String table, String odscode, Connection connection) throws SQLException
    {
        String q = "select eligible_count from "+table+" where ods_code = ?";

        PreparedStatement preparedStmt = connection.prepareStatement(q);
        preparedStmt.setString(1, odscode);

        ResultSet rs = preparedStmt.executeQuery();

        if (rs.next()) {
            String eligbleCount = rs.getString("eligible_count");
            Long l = Long.parseLong(eligbleCount);
            l = l +1;

            q = "UPDATE "+table+" set eligible_count=? where ods_code= ?";
            preparedStmt = connection.prepareStatement(q);
            String ecount = l.toString(l);
            preparedStmt.setString(1, ecount);
            preparedStmt.setString(2, odscode);
            preparedStmt.execute();
        }

        preparedStmt.close();
    }

    public static void InsertIntoEligibleCohort(String patientId, Connection connection) throws SQLException
    {
        String q = "INSERT INTO data_extracts.tmp_health_checks_cohort_eligible (id) values (?)";

        PreparedStatement preparedStmt = connection.prepareStatement(q);
        preparedStmt.setString(1, patientId);
        preparedStmt.execute();
        preparedStmt.close();

        /*
        // fix counts
        q = "select o.ods_code, ho.ccg from enterprise_pi.patient p ";
        q = q + "join  enterprise_pi.organization o on p.organization_id=o.id";
        q = q + "join data_extracts.hc_organisations ho on o.ods_code = ho.ods_code";
        q = q + "where p.id="+patientId;

        preparedStmt = connection.prepareStatement(q);
        ResultSet rs = preparedStmt.executeQuery();

        String table = "";
        if (rs.next()) {
            String odscode = rs.getString("ods_code");
            String ccg = rs.getString("ccg");

            if (ccg.equals("Havering")) { table = "data_extracts.hc_eligible_count_havering";}
            if (ccg.equals("Barking & Dagenham")) { table = "data_extracts.hc_eligible_count_barking";}
            if (ccg.equals("Redbridge")) {table = "data_extracts.hc_eligible_count_redbridge";};

            UpdateEligibleCounts(table, odscode, connection);
        }

        preparedStmt.close();
         */
    }

    public static void MoveToEligibleCohort(String nor, Connection connection) throws SQLException
    {
        // convert nor to long and divide by 2 to get the DDS patient_id
        Long l = Long.parseLong(nor);
        l = l / 2;
        String patientId = Long.toString(l);
        DeleteFromIneligibleCohort(patientId, connection);
        InsertIntoEligibleCohort(patientId, connection);
    }

    public static Integer NoOtherObsRecords(String nor, String ineligibleTable, String qdate, String hcDate, Connection connection) throws SQLException
    {
        // String q = "SELECT * FROM data_extracts."+ineligibleTable+" WHERE patient_id="+nor+" and original_code not in ("+RiskCodes+","+HCCodes+") and clinical_effective_date > '"+qdate+"'";

        //String q = "SELECT * FROM data_extracts."+ineligibleTable+" WHERE patient_id="+nor+" and (original_code is null or original_code not in ("+RiskCodes+","+HCCodes+"));";

        String q = "SELECT * FROM data_extracts."+ineligibleTable+" WHERE patient_id="+nor+" and (original_code is null or original_code not in ("+HCCodes+")) and clinical_effective_date < '"+hcDate+"'";

        PreparedStatement ps = connection.prepareStatement(q);
        ResultSet rs = ps.executeQuery();

        Integer p = 1;
        if (rs.next()) {p=0;}

        //if (p.equals(1)) {
            System.out.println(q);
        //}


        ps.close();

        return p;
    }

    public static void DeleteFromIneligibleTable(String patient_id, String ineligibleTable, Connection connection) throws SQLException
    {
        String q = "DELETE FROM data_extracts."+ineligibleTable+" WHERE patient_id=?";
        PreparedStatement preparedStmt = connection.prepareStatement(q);

        preparedStmt.setString(1,patient_id);
        preparedStmt.execute();

        preparedStmt.close();
    }

    public static void InsertIntoEligibleTable(String q, Connection connection, String patient_id, String dob, String gender, String ethnic_code, String lsoa, String msoa, String clinical_date, String ods_code, String original_code, String original_term, String result, String result_units) throws SQLException
    {
        PreparedStatement preparedStmt = connection.prepareStatement(q);

        preparedStmt.setString(1, patient_id);
        preparedStmt.setString(2, dob);
        preparedStmt.setString(3,gender);
        preparedStmt.setString(4,ethnic_code);
        preparedStmt.setString(5, lsoa);
        preparedStmt.setString(6, msoa);
        preparedStmt.setString(7, clinical_date);
        preparedStmt.setString(8, ods_code);
        preparedStmt.setString(9, original_code);
        preparedStmt.setString(10, original_term);
        preparedStmt.setString(11, result);
        preparedStmt.setString(12, result_units);

        preparedStmt.execute();
        preparedStmt.close();
    }

    public static void MoveToEligibleList(String nor, String eligibleTable, String ineligibleTable, Connection connection) throws SQLException
    {
        String q = "SELECT * FROM data_extracts."+ineligibleTable+" WHERE patient_id="+nor;

        PreparedStatement ps = connection.prepareStatement(q);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            String patient_id = rs.getString("patient_id");
            String dob = rs.getString("date_of_birth");
            String gender = rs.getString("patient_gender_id");
            String ethnic_code = rs.getString("ethnic_code");
            String lsoa = rs.getString("lsoa_code");
            String msoa = rs.getString("msoa_code");
            String clinical_date = rs.getString("clinical_effective_date");
            String ods_code = rs.getString("ods_code");
            String original_code = rs.getString("original_code");
            String original_term = rs.getString("original_term");
            String result = rs.getString("result_value");
            String result_units = rs.getString("result_value_units");

            q = "INSERT INTO data_extracts."+eligibleTable+" (patient_id, date_of_birth, patient_gender_id, ethnic_code, lsoa_code, msoa_code, clinical_effective_date, ods_code, ";
            q = q + "original_code, original_term, result_value, result_value_units) values (?,?,?,?,?,?,?,?,?,?,?,?)";
            InsertIntoEligibleTable(q, connection, patient_id, dob, gender, ethnic_code, lsoa, msoa, clinical_date, ods_code, original_code, original_term, result, result_units);

            DeleteFromIneligibleTable(patient_id, ineligibleTable, connection);
        }

        ps.close();
    }

    private static String GetCodes(String CodeSetId, Connection connection) throws SQLException
    {
        String codes="";
        String q = "SELECT * FROM rf2.code_set_codes where code_set_id in ("+CodeSetId+")";

        PreparedStatement ps = connection.prepareStatement(q);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            String read2 = rs.getString("read2_concept_id");
            String cvt3_concept_id = rs.getString("ctv3_concept_id");

            if (!read2.isEmpty()) {
                codes = codes + "'"+read2 + "',";
            }

            if (!cvt3_concept_id.isEmpty()) {
                String p = "";
                if (!cvt3_concept_id.contains("CTV3_")) { p="CTV3_";}
                codes = codes + "'"+ p + cvt3_concept_id + "',";
            }
        }

        ps.close();

        codes = codes.substring(0, codes.length()-1);

        return codes;
    }

    public static void Fix(String nor, String ineligibleTable, String eligibleTable, Connection connection) throws SQLException
    {
        String score = getLatestQRISKScore(nor, ineligibleTable, connection);
        String[] ss = score.split("\\~",-1);
        score = ss[0]; String qdate = ss[1];

        Float fScore = Float.parseFloat(score);
        if (fScore < 20) return;

        String hcDate = getLatestHealthCheckDate(nor, ineligibleTable, connection);

        if (hcDate.isEmpty()) {return;}

        Integer hcHorolog = DH(hcDate);
        Integer qriskHorolog = DH(qdate);

        Integer touched = 0;
        if (qriskHorolog >= hcHorolog) {
            Integer ret = NoOtherObsRecords(nor, ineligibleTable, qdate, hcDate, connection);
            if (ret.equals(1)) {
                touched = 1;
                //System.out.println(nor);
                MoveToEligibleList(nor, eligibleTable, ineligibleTable, connection);
                MoveToEligibleCohort(nor, connection);
            }
        }

        // CALL buildHcCountDatasets(); to fix the cohort counts
        // uncomment if updating eligable counts updated on the fly does not work
        /*
        if (touched.equals(1)) {
            String q = "CALL buildHcCountDatasets();";
            PreparedStatement preparedStatement = connection.prepareStatement(q);
            ResultSet rs = preparedStatement.executeQuery();
            preparedStatement.close();
        }
         */
    }

    public static void FixIneligibles(String configName, String eligibleTable, String ineligibleTable) throws SQLException, IOException
    {

        // java version of $$DH^STDDATE("29.02.2021")
        // Integer tst = DH("2024-02-29");
        // String date = HD(65379);
        // String date = HD(1);

        JsonNode jurl = ConfigManager.getConfigurationAsJson(configName, "db_subscriber");

        String url = jurl.get("url").asText();

        JsonNode HCConfig = ConfigManager.getConfigurationAsJson("healthcheck");
        String username = HCConfig.get("user").asText();
        String password = HCConfig.get("pass").asText();

        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);

        dataSource.setReadOnlyPropagatesToServer(true);
        dataSource.setAllowLoadLocalInfile(true);
        dataSource.setAllowMultiQueries(true);

        Connection connection = dataSource.getConnection();

        //String table = "data_extracts.hc_eligible_count_havering"; String odscode = "F82002";
        //UpdateEligibleCounts(table, odscode, connection);

        HCCodes = GetCodes("6,7", connection);
        RiskCodes = GetCodes("0", connection);

        String q = "SELECT distinct patient_id FROM data_extracts."+ineligibleTable+";";

        PreparedStatement ps = connection.prepareStatement(q);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Long patient_id = rs.getLong("patient_id");
            /*
            if (patient_id.toString().equals("1194948672")) {
                System.out.println("test");
            }
             */
            Fix(Long.toString(patient_id), ineligibleTable, eligibleTable, connection);
        }
        ps.close();

        connection.close();
    }

}
