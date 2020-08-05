package org.endeavourhealth.emis.cron;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.*;

public class Main implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String FILE_HEADER = "CodeId";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";


    public static void main(String[] args) throws Exception {

        //0 0 9 ? * MON *  To run Every month Every week monday at 9 AM
        LOG.info("Initializing  emis-missing-codes-cron-scheduler!!");

        ConfigManager.Initialize("emis-missing-codes-cron-attrbs");
        JsonNode json = ConfigManager.getConfigurationAsJson("emis");
        String cronTimer = json.get("cron.timer").asText();
        //cronTimer = "0 34 10 ? AUG WED *";
        JobDetail jobDetail = JobBuilder.newJob(Main.class).build();
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("Cron Trigger").
                withSchedule(CronScheduleBuilder.cronSchedule(cronTimer)).build();
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        scheduler.scheduleJob(jobDetail, trigger);

        LOG.info("Scheduler scheduled!!");

    }

    private static String getLastRunDateAndTime(String reportFilePath) throws Exception {

        String lastRunDateInFile = null;
        String formattedDate = null;
        try {

            File file = new File(reportFilePath);
            //if no file exists, get the current date and time and return.
            if (!file.exists()) {
                return retrieveFormattedDate(null);
            }

            File myObj = new File(reportFilePath);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                lastRunDateInFile = myReader.nextLine();
            }
            myReader.close();
            formattedDate = retrieveFormattedDate(lastRunDateInFile);

        } catch (Throwable t) {
            LOG.error("Error occurred getting last run date and time " + t);
        }
        return formattedDate;
    }

    private static String retrieveFormattedDate(String lastRunDateInFile) throws Exception {

        if (StringUtils.isEmpty(lastRunDateInFile)) {
            lastRunDateInFile = getCurrentDateAndTime();
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        Date parsedDate = formatter.parse(lastRunDateInFile);

        return formatter.format(parsedDate);

    }

    private static void writeCurrentDateTimeToFile(String reportFilePath) {

        String currentDateAndTime = getCurrentDateAndTime();
        try {
            File file = new File(reportFilePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(currentDateAndTime);
            bw.close();

        } catch (Throwable t) {
            LOG.error("Exception occurred in writeCurrentDateTimeToFile " + t);
        }
    }


    private static void writeEmisMissingCodeDetailsToFile(List<String> missingCodes, String subject, String fileName, String missingCodesFilePath, String lastRunDate) throws Exception {

        if (missingCodes.size() > 0) {

            if (!StringUtils.isEmpty(missingCodesFilePath)) {
                missingCodesFilePath = missingCodesFilePath.concat(fileName).concat(".csv");

                // final String FILE_HEADER = "CodeId";
                FileWriter writer = null;
                try {
                    writer = new FileWriter(missingCodesFilePath, false);
                    writer.append(FILE_HEADER);
                    writer.append(NEW_LINE_SEPARATOR);

                    for (String missingCodesList : missingCodes) {
                        writer.write(missingCodesList);
                        writer.append(NEW_LINE_SEPARATOR);
                    }
                } catch (Throwable t) {
                    LOG.error("Exception in writing to csv file !!!" + t);
                } finally {
                    try {
                        writer.flush();
                        writer.close();
                    } catch (Throwable t) {
                        LOG.error("Error while flushing/closing fileWriter !!!" + t);
                    }
                    LOG.info("Write Success!!!");
                    writer.close();
                }
                sendEmails(missingCodesFilePath, subject, lastRunDate);
            }
        }
    }

    private static String getCurrentDateAndTime() {

        DateFormat df = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date today = Calendar.getInstance().getTime();

        return df.format(today);
    }

    /**
     * New missing codes appeared this week
     */
    public List<String> retrieveMissingCodesAppearedThisWeek(String lastRunDate) throws Exception {

        Connection connection = ConnectionManager.getPublisherCommonConnection();
        PreparedStatement ps = null;
        List<String> emisCodeList = new ArrayList<String>();

        try {

            String sql = "select DISTINCT code_id from emis_missing_code_error Where  DATE_FORMAT(timestmp,\"%Y-%m-%d\")\n" +
                    "between  DATE_SUB(DATE_FORMAT(? ,\"%Y-%m-%d\"), INTERVAL 1 WEEK) " +
                    "and DATE_FORMAT(? ,\"%Y-%m-%d\") and dt_fixed is null";

            ps = connection.prepareStatement(sql);
            int col = 1;
            ps.setString(col++, lastRunDate);
            ps.setString(col++, lastRunDate);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                String codeId = rs.getString(1);
                emisCodeList.add(codeId);
            }

            return emisCodeList;

        } catch (Exception ex) {
            connection.rollback();
            throw ex;
        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }

    /**
     * missing codes fixed this week
     */
    public List<String> retrieveMissingCodesFixedThisWeek(String lastRunDate) throws Exception {

        Connection connection = ConnectionManager.getPublisherCommonConnection();
        PreparedStatement ps = null;
        List<String> emisCodeList = new ArrayList<String>();

        try {

            String sql = "select DISTINCT code_id from emis_missing_code_error Where  DATE_FORMAT(dt_fixed,\"%Y-%m-%d\")\n" +
                    "between  DATE_SUB(DATE_FORMAT(? ,\"%Y-%m-%d\"), INTERVAL 1 WEEK) " +
                    "and DATE_FORMAT(? ,\"%Y-%m-%d\")";

            ps = connection.prepareStatement(sql);
            int col = 1;
            ps.setString(col++, lastRunDate);
            ps.setString(col++, lastRunDate);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String codeId = rs.getString(1);
                emisCodeList.add(codeId);
            }

            return emisCodeList;

        } catch (Exception ex) {
            connection.rollback();
            throw ex;
        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }

    /**
     * Current state of missing codes in general.
     */
    public List<String> retrieveAllMissingCodes(String lastRunDateAndTime) throws Exception {

        Connection connection = ConnectionManager.getPublisherCommonConnection();
        List<String> emisCodeList = new ArrayList<String>();
        PreparedStatement ps = null;

        try {

            String sql = "select DISTINCT code_id from emis_missing_code_error Where dt_fixed is null";
            ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String codeId = rs.getString(1);
                emisCodeList.add(codeId);
            }

            return emisCodeList;

        } catch (Exception ex) {
            connection.rollback();
            throw ex;
        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }


    public static void sendEmails(String path, String subjectStr, String lastRunDate) throws Exception {

        JsonNode json = ConfigManager.getConfigurationAsJson("emis");
        String smtp = json.get("mail.transport.protocol").asText();
        String isEnabled = json.get("mail.smtp.starttls.enable").asText();
        String smtpPort = json.get("mail.smtp.port").asText();
        String fromAddress = json.get("from.address").asText();
        String toAddress = json.get("to.address").asText();
        String userName = json.get("mail.user.name").asText();
        String password = json.get("mail.user.password").asText();
        String subject = subjectStr;
        String bodyText = "Kindly find the attached file of " + subject + " and the job last triggered on " + lastRunDate + ".";
        String attachmentName = path;

        // Creates a Session with the following properties.
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.transport.protocol", smtp);
        props.put("mail.smtp.starttls.enable", isEnabled);
        //props.put("mail.password", "password");
        props.put("mail.smtp.port", smtpPort);
        Session session = Session.getDefaultInstance(props);

        try {

            // Create an Internet mail msg.
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromAddress));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            msg.setSubject(subject);
            msg.setSentDate(new Date());

            // Set the email msg text.
            MimeBodyPart messagePart = new MimeBodyPart();
            messagePart.setText(bodyText);

            // Set the email attachment file
            FileDataSource fileDataSource = new FileDataSource(attachmentName);

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setDataHandler(new DataHandler(fileDataSource));
            attachmentPart.setFileName(fileDataSource.getName());

            // Create Multipart E-Mail.
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messagePart);
            multipart.addBodyPart(attachmentPart);

            msg.setContent(multipart);

            // to authenticate to the mail server.
            Transport.send(msg, userName, password);
        } catch (Throwable t) {
            LOG.error("Exception occurred in sendEmails " + t);
        }

    }

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        try {

            LOG.info("Execution of the job!!");

            List<String> retrieveAllMissingCodes = null;
            List retrieveMissingCodesFixedThisWeek = null;
            List retrieveMissingCodesAppeardThisWeek = null;
            JsonNode json = ConfigManager.getConfigurationAsJson("emis");
            String reportFilePath = json.get("report.file.path").asText();
            String missingCodesFilePath = json.get("missingcodes.file.path").asText();
            String lastRunDate = getLastRunDateAndTime(reportFilePath);
            LOG.info("LastRunDate!!!" + lastRunDate);
            //lastRunDate = "2020-04-01";
            String subject = null;
            String fileName = null;

            if (!StringUtils.isEmpty(lastRunDate)) {

                retrieveAllMissingCodes = new Main().retrieveAllMissingCodes(lastRunDate);

                if (retrieveAllMissingCodes.size() > 0) {
                    fileName = "EmisMissingCodes";
                    subject = "Emis missing codes in general";
                    writeEmisMissingCodeDetailsToFile(retrieveAllMissingCodes, subject, fileName, missingCodesFilePath, lastRunDate);
                }

                retrieveMissingCodesFixedThisWeek = new Main().retrieveMissingCodesFixedThisWeek(lastRunDate);

                if (retrieveMissingCodesFixedThisWeek.size() > 0) {
                    fileName = "EmisMissingCodesFixedThisWeek";
                    subject = "Emis missing codes fixed this week";
                    writeEmisMissingCodeDetailsToFile(retrieveMissingCodesFixedThisWeek, subject, fileName, missingCodesFilePath, lastRunDate);
                }

                retrieveMissingCodesAppeardThisWeek = new Main().retrieveMissingCodesAppearedThisWeek(lastRunDate);

                if (retrieveMissingCodesAppeardThisWeek.size() > 0) {
                    fileName = "EmisMissingCodesAppearedThisWeek";
                    subject = "Emis missing codes appeared this week";
                    writeEmisMissingCodeDetailsToFile(retrieveMissingCodesAppeardThisWeek, subject, fileName, missingCodesFilePath, lastRunDate);
                }
                //write current date and time to file which will pick up the date and time in the second run to get the last run date and time.
                if (!StringUtils.isEmpty(reportFilePath)) {
                    writeCurrentDateTimeToFile(reportFilePath);
                }
            }
        } catch (Throwable t) {
            LOG.error("Exception occurred in executing job" + t);
        }

    }
}



