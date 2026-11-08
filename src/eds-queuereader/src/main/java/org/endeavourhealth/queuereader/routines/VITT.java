package org.endeavourhealth.queuereader.routines;

import OpenPseudonymiser.Crypto;
import com.fasterxml.jackson.databind.JsonNode;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public abstract class VITT extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(VITT.class);
    public static String AEdate;
    public static String ThrombosisDate;
    public static String ThrombosisCode;

    private static String USER;
    private static String PASS;
    private static String HOST;
    private static String GPDB;
    private static String HSPDB;
    private static String URL;

    private static Connection connection;

    private static  byte[] saltBytes;

    private static String CODES;

    private static FileWriter tabWriter;
    private static FileWriter tabWriterCsv;

    private static String OS;
    private static String scriptFile;
    private static String mySQLclient;
    private static String dir;

    private static Integer latest;
    private static Integer writeToVT;

    public static void Start(String nhsno) throws Exception
    {

        // EncryptFromFile("C:\\Users\\Paul\\Desktop\\vitt_report (2).txt");

        /*
        List<File> ret = new ArrayList<>();
        String tempDir = "/tmp/";
        String outputFileName = "fred.txt";
        File dstFile = new File(tempDir, outputFileName);
        ret.add(dstFile);
        boolean sentOk = EmailSender.sendEmail("psimon1@supanet.com", "test", "line-1\nline-2", ret);
        sentOk = EmailSender.sendEmail("p.simon@nhs.net", "test", "line-1\nline-2", ret);
         */

        JsonNode VITTConfig = ConfigManager.getConfigurationAsJson("vitt");
        USER = VITTConfig.get("user").asText();
        PASS = VITTConfig.get("pass").asText();
        HOST = VITTConfig.get("host").asText();
        // GP database
        GPDB = VITTConfig.get("gp_schema").asText();
        // hospital database
        HSPDB = VITTConfig.get("hospital_schema").asText();
        URL = VITTConfig.get("jdbc").asText();

        String base64Salt = ConfigManager.getConfiguration("barts-salt", "vacc-events");
        saltBytes = Base64.getDecoder().decode(base64Salt);

        String SQL = "";

        // windows or linux?
        mySQLclient = "/usr/bin/mysql";
        scriptFile = "/tmp/test.sh";
        dir = "/tmp/";
        String sqlDir = "/tmp/";

        OS = System.getProperty("os.name").toLowerCase();

        String File = "/tmp/VITT.txt";
        if (OS.indexOf("win") >= 0) {
            File = "c:\\tmp\\VITT.txt";
        }

        String STR;
        BufferedReader csvReader = new BufferedReader(new FileReader(File));
        Integer ft = 1; CODES = "";
        while ((STR = csvReader.readLine()) != null) {
            String DBID = Piece(STR,"~",1,1);
            CODES = CODES + "'" + DBID +"',";
        }
        csvReader.close();

        CODES = CODES.substring(0, CODES.length()-1);

        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(URL);
        dataSource.setUser(USER);
        dataSource.setPassword(PASS);

        dataSource.setReadOnlyPropagatesToServer(true);
        dataSource.setAllowLoadLocalInfile(true);
        dataSource.setAllowMultiQueries(true);

        connection = dataSource.getConnection();

        File f = new File("/tmp/VITT_java_output.txt");
        if(f.exists() && !f.isDirectory()) {
            f.delete();
        }

        String tFileOutput = "/tmp/VITT_java_output.txt";
        tabWriter = new FileWriter(tFileOutput);

        tabWriterCsv = new FileWriter("/tmp/VITT_java.txt");
        tabWriterCsv.write("nhs_number\tjab_date\tA&E date\tonset5-30\td-dimer > 4\td-dimer 2-4\tthrombosis\tplatelet < 150\tantipf4\ttotal yes\tcondition\n");

        latest = 0; writeToVT = 0;
        if (Piece(nhsno, "~", 1, 1).equals("LATEST")) {
            // nhs number
            String n = Piece(nhsno, "~", 2, 2);

            latest = 1;

            if (n.equals("BULK")) {
                RunBulk();
            } else { writeToVT = 1; Latest(n); }

            CloseFlush();
            return;
        }

        if (!nhsno.equals("BULK")) {
            writeToVT = 1;
            System.out.println("Running for single patient " +  nhsno);
            // RunForPatient(nhsno, CODES);
            EachJab(nhsno);
            CloseFlush();
            return;
        }

        if (OS.indexOf("win") >= 0) {
            mySQLclient = "\"C:\\Program Files\\MySQL\\MySQL Workbench 8.0 CE\\mysql\"";
            scriptFile = "c:\\tmp\\test.bat";
            dir = "c:\\tmp\\";
            sqlDir = "c:\\\\tmp\\\\";
        }

        if (nhsno.equals("BULK"))
        {
            RunBulk();
            CloseFlush();
            return;
        }

        //SQL = "\"set @x=0; SELECT distinct @x:=@x+1, p.nhs_number, p.id, p.date_of_birth FROM subscriber_pi.encounter e join subscriber_pi.person p on e.person_id = p.id;\"";

        /*
        SQL = "\"SELECT distinct p.nhs_number, p.id, p.date_of_birth FROM "+HSPDB+".person p where organization_id = '2782572' and TIMESTAMPDIFF(YEAR,p.date_of_birth,CURDATE()) > 17;\"";
        System.out.println(SQL);

        String X = mySQLclient+" --local_infile=1 -h "+HOST+" --user="+USER+" --password="+PASS+" --execute "+SQL+" > "+dir+"thrombosis_nhs.txt";

        Integer exitCode = WriteToScript(X);
        if (!exitCode.equals(0)) { System.out.println("problems creating thrombosis_nhs.txt script"); return;}

        exitCode = RunScript();
        if (!exitCode.equals(0)) { System.out.println("problems running thrombosis_nhs.txt"); return;}
        System.out.println("ran OK");
         */

        // insert into database (truncate table before importing the data)
        // don't load for now - run down test file instead
        //LoadFileIntoTable(sqlDir+"thrombosis_nhs.txt","vitt.vitt_cohort",mySQLclient, HOST, USER, PASS, scriptFile);
        //exitCode = RunScript(scriptFile);
        //if (!exitCode.equals(0)) {return;}
        //System.out.println("ran OK");

        /*
        File = dir+"thrombosis_nhs.txt";
        csvReader = new BufferedReader(new FileReader(File));
        String row = "";
        ft = 1;
        while ((row = csvReader.readLine()) != null) {
            // if (ft.equals(1)) {ft = 0; continue;}

            String p[] = row.split("\t", -1);
            String nhs_number = p[0];

            // RunForPatient(nhs_number, CODES);
            EachJab(nhs_number);

            System.out.println(nhs_number);
        }
        csvReader.close();

        connection.close();

        tabWriter.flush();
        tabWriter.close();
         */
    }

    public static void CloseFlush() throws Exception
    {
        tabWriterCsv.close();

        tabWriter.flush();
        tabWriter.close();
        connection.close();
    }

    public static void RunBulk() throws Exception
    {
        // STORED PROCS WILL GET CALLED FROM YAML
        // Cohort : executeVITTCovidCohortv2
        // Emergency encounters : executeVITTCovidAEEncounters
        // covid jabs : executeVITTCovidJabs
        // Thrombosis diagnosis : VITTCovidThrombosisEvents
        // Values (ddimer etc) : executeVITTCovidValues

        Integer total = countPatients();

        String q = "select nhs_number from data_extracts.vitt_cohort_v2;";
        PreparedStatement preparedStatement = connection.prepareStatement(q);

        ResultSet rs = preparedStatement.executeQuery();

        Integer cnt = 1;
        while (rs.next()) {
            String nhs_number = rs.getString("nhs_number");
            EachJab(nhs_number);

            if (cnt % 10000 == 0) {
                System.out.println(cnt + " of " + total);
            }

            cnt++;
        }

        rs.close();
        preparedStatement.close();
    }

    public static Integer countPatients() throws Exception
    {
        String q = "select count(1) from data_extracts.vitt_cohort_v2;";
        PreparedStatement preparedStatement = connection.prepareStatement(q);

        ResultSet rs = preparedStatement.executeQuery();

        Integer count = 0;
        if (rs.next()) {
            count = rs.getInt(1);
        }

        rs.close();
        preparedStatement.close();
        return count;
    }

    public static Integer RunSh(String X) throws Exception
    {
        Integer exitCode = WriteToScript(X);
        if (!exitCode.equals(0)) { System.out.println("problems creating thrombosis_nhs.txt script"); return 0;}

        exitCode = RunScript();
        if (!exitCode.equals(0)) { System.out.println("problems running thrombosis_nhs.txt"); return 0;}

        return exitCode;
    }

    public static void EachJab(String nhs_number) throws SQLException, IOException, Exception
    {
        List<List<String>> rxdats = GetJabs(nhs_number);

        String rxdat = "";

        for(List<String> rec : rxdats) {
            rxdat = rec.get(0);

            Integer H = DH(rxdat);
            Integer from  = H + 4;
            Integer to = H + 29;

            RunForPatient(nhs_number, from, to, rxdat);
        }
    }

    public static void Latest(String nhs_number) throws Exception
    {
        RunForPatient(nhs_number, 1, 1, "");
    }

    public static void RunForPatient(String nhs_number, Integer from, Integer to, String rxdat) throws SQLException, IOException, Exception
    {

        Integer thrombosis = 0;
        Integer platelet = 0;
        Integer antipf4 = 0;

        Integer onset530 = 0;
        String jabdatfrom = ""; String jabdatto = "";

        AEdate = ""; ThrombosisDate = ""; ThrombosisCode = "";

        jabdatfrom = HD(from);
        jabdatto = HD(to);

        boolean eajd = false;
        if (latest.equals(0)) eajd = EncounterAfterJabDate(jabdatfrom, jabdatto, nhs_number);
        if (eajd == true) {onset530 = 1;}

        if (jabdatfrom == "") { jabdatfrom = "1841-01-01";}

        boolean thBool = Thrombosis(jabdatfrom, nhs_number, CODES, latest);
        if (thBool == true) {thrombosis = 1;}

        Hashtable<String, String> results = OtherObservations(jabdatfrom, nhs_number, latest);

        String zddimerDate = ""; String zddimerValue = "";
        // d-dimer
        if (results.containsKey("1426682")) {
            String ddimerStr = results.get("1426682");
            zddimerDate = Piece(ddimerStr, "~", 1, 1);
            zddimerValue = Piece(ddimerStr, "~", 2, 2);
        }

        // platelet
        String zplateletDate = ""; String zplateletValue = "";
        if (results.containsKey("1426613")) {
            String plateletStr = results.get("1426613");
            zplateletDate = Piece(plateletStr, "~", 1, 1);
            zplateletValue = Piece(plateletStr, "~", 2, 2);
        }

        // antipf4
        String zantipf4Date = ""; String zantipf4Value = "";
        if (results.containsKey("1427637")) {
            String antipf4Str = results.get("1427637");
            zantipf4Date = Piece(antipf4Str, "~" ,1 ,1);
            antipf4 = 1;
        }

        if (!zplateletValue.isEmpty()) {
            float v = toFloat(zplateletValue);
            if (v != -1 && v < 150) {
                platelet = 1;
            }
        }

        Integer totYes = onset530 + thrombosis + platelet + antipf4;

        String condition = "";

        float zddimer = toFloat(zddimerValue);

        if (totYes.equals(4) && zddimer > 4) condition = "DEFINITE";
        if (totYes.equals(3) && zddimer > 4) condition = "PROBABLE";
        if (zddimer >2 && zddimer <4) {
            if (totYes.equals(4)) condition = "PROBABLE";
            if (totYes.equals(2) || totYes.equals(3)) condition = "POSSIBLE";
        }

        if (onset530.equals(1) & thrombosis.equals(0) && platelet.equals(1) && zddimer<2) condition = "UNLIKELY";
        if (onset530.equals(1) & thrombosis.equals(1) && platelet.equals(0) && zddimer<2) condition = "UNLIKELY";

        if (!condition.isEmpty()) {
            String csv = ""; String T = "\t";

            String a1 = "onset time 5-30 days after jab admin " + jabdatfrom + " to " + jabdatto;
            SystemOut(a1);

            String ps = EncryptNHSNumbers(nhs_number, saltBytes);
            SystemOut(ps);

            String b = "patient " + nhs_number + " had a jab on " + rxdat + " and visited A&E on " + AEdate;
            SystemOut(b);

            String c = "thrombosis diagnosis was found on " + ThrombosisDate;
            SystemOut(c);

            // ** TO DO lookup the term from hash table
            SystemOut("thrombosis code " + ThrombosisCode);

            SystemOut("d-dimer value " + zddimerValue);

            String g = "d-dimer event date " + zddimerDate;
            SystemOut(g);

            String h = "platelet count " + zplateletValue;
            SystemOut(h);

            String i = "anti-pf4 antibodies record exists? " + Select(antipf4);
            SystemOut(i);

            String j = "event date" + zantipf4Date;
            SystemOut(j);

            String k = "onset 5-30d = " + Select(onset530);
            SystemOut(k);

            String z = "no";
            if (zddimer > 4) z = "yes";
            String l = "d-dimer > 4? " + z;
            SystemOut(l);

            String y = "no";
            if (zddimer > 2 && zddimer < 4) y = "yes";
            String m = "d-dimer between 2-4 ?" + y;
            SystemOut(m);

            String n = "thrombosis = " + Select(thrombosis);
            SystemOut(n);

            String o = "platelet < 150 " + Select(platelet);
            SystemOut(o);

            String p = "antipf4 = " + Select(antipf4);
            SystemOut(p);

            String q = "Total yes = "+totYes;
            SystemOut(q);

            String r = "CONDITION = " + condition;
            SystemOut(r);

            csv = nhs_number + T + rxdat + T + AEdate + T + Select(onset530) + T + z + T + y + T + Select(thrombosis) + T + Select(platelet) + T + Select(antipf4) + T + totYes + T + condition + "\n";
            tabWriterCsv.write(csv);
            tabWriterCsv.flush();
        }
    }

    public static String Select(Integer OneOrZero)
    {
        String z = "yes";
        if (OneOrZero.equals(0)) z = "no";
        return z;
    }

    public static void SystemOut(String z) throws Exception
    {
        if (writeToVT.equals(1)) System.out.println(z);
        tabWriter.append(z+"\n");
    }

    public static float toFloat(String Str)
    {
        float v;
        try {
            v = (Float.valueOf(Str).floatValue());
        } catch (NumberFormatException e) {
            v = -1;
        }
        return v;
    }

    public static int toInt(String Str)
    {
        int v;
        try {
            v = Integer.parseInt(Str);
        } catch (NumberFormatException e) {
            v = -1;
        }
        return v;
    }

    public static Hashtable<String, String> OtherObservations(String jabdatfrom, String nhs_number, Integer latest) throws SQLException
    {

        Hashtable<String, String> result = new Hashtable<String, String>();

        // d-dimer, platelet, antipf4 (latest, after from date)
        //String q = "SELECT o.id, clinical_effective_date, result_value, non_core_concept_id from "+HSPDB+".observation o ";
        //q = q +"join "+HSPDB+".patient p on p.id=o.patient_id ";
        //q = q +"join "+HSPDB+".person z on z.id = p.person_id ";
        //q = q + "where z.nhs_number='"+nhs_number+"' and ";
        //if (latest.equals(0)) q = q + "o.clinical_effective_date > '"+jabdatfrom+"' and ";
        //q = q + "(o.non_core_concept_id = '1426682' or o.non_core_concept_id = '1426613' or o.non_core_concept_id = '1427637')";


        String q = "select d.id, d.clinical_effective_date, d.result_value, d.non_core_concept_id ";
        q = q + "from data_extracts.vitt_values d ";
        q = q + "where d.nhs_number = '"+nhs_number+"'";
        if (latest.equals(0)) q = q + " and d.clinical_effective_date >= '"+jabdatfrom+"'";

        // aaarrrghhh!
        if (latest.equals(0)) q = q + "order by clinical_effective_date desc, d.id asc;";

        // latest
        if (latest.equals(1)) q = q + "order by clinical_effective_date ASC, d.id asc;";

        PreparedStatement preparedStatement = connection.prepareStatement(q);
        ResultSet rs = preparedStatement.executeQuery();

        // read backwards
        rs.afterLast();
        while (rs.previous()) {
            String ite = rs.getString("non_core_concept_id");
            String data = rs.getString("clinical_effective_date") + "~" + rs.getString("result_value");

            String dat = rs.getString("clinical_effective_date");

            if (!result.containsKey(ite)) {
                result.put(ite, data);
            }
        }

        rs.close();
        preparedStatement.close();

        return result;
    }

    public static boolean Thrombosis(String jabdatfrom, String nhs_number, String CODES, Integer latest) throws SQLException
    {
        boolean z = false;
        //String q = "SELECT o.clinical_effective_date, o.non_core_concept_id FROM "+HSPDB+".observation o ";
        //q = q +"join "+HSPDB+".patient p on p.id=o.patient_id ";
        //q = q +"join "+HSPDB+".person z on z.id = p.person_id ";
        //q = q + "where z.nhs_number='"+nhs_number+"' and ";
        //if (latest.equals(0)) q = q + "o.clinical_effective_date > '"+jabdatfrom+"' and ";
        //q = q + "o.non_core_concept_id in (" + CODES + ") ";

        String q = "select d.clinical_effective_date, d.non_core_concept_id ";
        q = q + "from data_extracts.vitt_thrombosis_events d ";
        q = q + "where d.nhs_number = '"+nhs_number+"'";
        if (latest.equals(0)) q = q + " and d.clinical_effective_date >= '"+jabdatfrom+"'";

        q = q + "order by d.clinical_effective_date desc, d.id desc;";

        PreparedStatement preparedStatement = connection.prepareStatement(q);

        ResultSet rs = preparedStatement.executeQuery();

        //rs.afterLast();
        //if (rs.previous()) {

        if (rs.next())
        {
            ThrombosisDate = rs.getString("clinical_effective_date");
            ThrombosisCode  = rs.getString("non_core_concept_id");
            z = true;
        }

        rs.close();
        preparedStatement.close();
        return z;
    }

    public static boolean EncounterAfterJabDate(String jabdatfrom, String jabdatto, String nhs_number) throws SQLException
    {
        boolean z = false;

        //String q = "SELECT e.clinical_effective_date from "+HSPDB+".encounter e ";
        //q = q + "join "+HSPDB+".patient p on p.id=e.patient_id ";
        //q = q + "join "+HSPDB+".person z on z.id = p.person_id ";
        //q = q + "where z.nhs_number = '"+nhs_number+"' and e.clinical_effective_date > '"+jabdatfrom+"' and e.admission_method = 'emergency' ";
        //q = q + "order by e.clinical_effective_date asc";

        String q = "select d.clinical_effective_date ";
        q = q + "from data_extracts.vitt_ae_encounters d ";
        q = q + "where d.nhs_number = '"+nhs_number+"' ";
        q = q + "and d.clinical_effective_date > '"+jabdatfrom+"'";
        q = q + "order by d.clinical_effective_date asc";

        PreparedStatement preparedStatement = connection.prepareStatement(q);

        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            AEdate = rs.getString("clinical_effective_date");

            Integer zTo = DH(jabdatto);
            Integer AEHorolog = DH(AEdate);

            z = true;
            if (AEHorolog > zTo) z = false;
        }

        rs.close();
        preparedStatement.close();
        return z;
    }

    public static List<List<String>> GetJabs(String nhs_number) throws SQLException
    {
        List<List<String>> result = new ArrayList<>();

        //String q = "SELECT rx.clinical_effective_date ";
        //q =q + "from "+GPDB+".medication_statement rx ";
        //q =q + "join data_extracts.bartsvssjabs d on rx.non_core_concept_id=d.dbid ";
        //q = q + "join "+GPDB+".person p on rx.person_id=p.id ";
        //q = q + "where p.nhs_number = '"+nhs_number+"' ";
        //q = q + "order by rx.clinical_effective_date asc;";

        String q = "SELECT distinct jabs.clinical_effective_date ";
        q =q + "from data_extracts.vitt_covid_jabs jabs ";
        q = q + "where jabs.nhs_number = '"+nhs_number+"' ";
        q = q + "order by jabs.clinical_effective_date asc;";

        PreparedStatement preparedStatement = connection.prepareStatement(q);

        ResultSet rs = preparedStatement.executeQuery();

        while (rs.next()) {
            String rxdat = rs.getString("clinical_effective_date");

            List<String> row = new ArrayList<>();
            row.add(rxdat);
            result.add(row);
        }

        rs.close();
        preparedStatement.close();
        return result;
    }

    public static Integer RunScript() throws IOException, InterruptedException
    {
        Process p = Runtime.getRuntime().exec(scriptFile);
        p.waitFor();
        int exitCode = p.exitValue();
        return exitCode;
    }

    public static void LoadFileIntoTable(String pFile, String pTable, String mySQLclient, String HOST, String USER, String PASS) throws IOException, InterruptedException {
        //String SQL = "\"truncate table vitt.vitt_cohort; load data local infile 'c:\\\\tmp\\\\thrombosis_nhs.txt' into table vitt.vitt_cohort IGNORE 1 LINES;\"";
        String SQL = "\"truncate table "+pTable+"; load data local infile '"+pFile+"' into table "+pTable+" IGNORE 1 LINES;\"";

        System.out.println(SQL);

        String X = mySQLclient+" --local_infile=1 -h "+HOST+" --user="+USER+" --password="+PASS+" --execute "+SQL;
        WriteToScript(X);
    }

    public static Integer WriteToScript(String cmd) throws IOException, InterruptedException {
        FileWriter tabWriter = new FileWriter(scriptFile);
        tabWriter.write(cmd);
        tabWriter.close();

        int exitCode = 0;

        if (OS.indexOf("linux") >= 0) {
            Process p = Runtime.getRuntime().exec("chmod +x " + scriptFile);
            p.waitFor();
            exitCode = p.exitValue();
        }
        return exitCode;
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

        z = justify(year.toString())+"-"+justify(month.toString())+"-"+justify(day.toString());

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

    public static void EncryptFromFile(String File) throws Exception
    {
        String row;

        String base64Salt = ConfigManager.getConfiguration("barts-salt", "vacc-events");
        byte[] saltBytes = Base64.getDecoder().decode(base64Salt);

        BufferedReader csvReader = new BufferedReader(new FileReader(File));
        while ((row = csvReader.readLine()) != null) {
            if (row.contains("patient ")) {
                String[] data = row.split(" ",-1);
                String nhsno = data[1];

                String pseudoNhsNumber = EncryptNHSNumbers(nhsno, saltBytes);
                System.out.println(nhsno+"~"+pseudoNhsNumber);
            }
        }
        csvReader.close();
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