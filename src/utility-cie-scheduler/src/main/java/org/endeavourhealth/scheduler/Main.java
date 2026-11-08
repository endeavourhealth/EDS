package org.endeavourhealth.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ScheduledTaskAuditDalI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static String appId = "cie-run";
    private static String configId = "cie-scheduler";


    public static void main(String[] args) throws Exception {

        ConfigManager.Initialize(appId);

        JsonNode cieSchedulerCfg = ConfigManager.getConfigurationAsJson(configId);

        if (null == cieSchedulerCfg) {
            throw new Exception("Failed to find scheduler configuration JSON");
        }

        String pkbUrl=null;
        String processType = args[0];

        if(processType.equalsIgnoreCase("bulkStart")){
            pkbUrl = cieSchedulerCfg.get("bulkStart").asText();
        } else if (processType.equalsIgnoreCase("deltaStart")) {
            pkbUrl = cieSchedulerCfg.get("deltaStart").asText();
        }else if (processType.equalsIgnoreCase("bulkStop")) {
            pkbUrl = cieSchedulerCfg.get("bulkStop").asText();
        }else if (processType.equalsIgnoreCase("deltaStop")) {
            pkbUrl = cieSchedulerCfg.get("deltaStop").asText();
        }else if (processType.equalsIgnoreCase("reBulkStart")) {
            pkbUrl = cieSchedulerCfg.get("reBulkStart").asText();
        }else if (processType.equalsIgnoreCase("reBulkStop")) {
            pkbUrl = cieSchedulerCfg.get("reBulkStop").asText();
        }else if (processType.equalsIgnoreCase("reprocessStart")) {
            pkbUrl = cieSchedulerCfg.get("reprocessStart").asText();
        }else if (processType.equalsIgnoreCase("reprocessStop")) {
            pkbUrl = cieSchedulerCfg.get("reprocessStop").asText();
        }
        else {

        }

        String projectId=cieSchedulerCfg.get("projectId").asText();
        String token_url=cieSchedulerCfg.get("keycloakTokenUri").asText();
        String clientId=cieSchedulerCfg.get("keycloakClientId").asText();
        String clientSecret=cieSchedulerCfg.get("keycloakClientSecret").asText();
        String token_msg="{\n" +"  \"grantType\":\"client_credentials\",\n" +"  \"clientId\":\""+clientId+"\",\n" +" \"clientSecret\":\""+clientSecret+"\"\n" + "}";

        invokeCIEProcess(projectId,  pkbUrl, token_url,token_msg, args);

    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        Main.token = token;
    }

    private static String token;
    private static void invokeCIEProcess(String projectId,String pkbUrl,String token_url,String token_msg, String args[]) throws Exception {
        try {
            URL url = new URL(pkbUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            if (getToken() == null) {
                setToken(prepareToken(token_url, token_msg));
            }
            conn.setRequestProperty("Authorization", getToken());
            conn.setRequestProperty("ProjectID", projectId);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setReadTimeout(0);

            if (conn.getResponseCode() == 504) {
                /* ALB is sending 504 error due to the default setting although back end job is running
                   fine with 200 Code in the server access logs, this is being checked in the actual process.
                 */
                SlackHelper.sendSlackMessage(SlackHelper.Channel.CIEFhirExtractor,"ALB is sending 504 error due to the default setting " +
                        "although back end job is running fine with 200 Code in the access logs for (" + args[0] + ") process, " +
                        "this process is being handled in the actual process, check for any valid server errors");
                auditSuccess(configId, args);
            } else if (conn.getResponseCode() == 409) {
                throw new RuntimeException( args[0] + " already in Progress, please check");
            } else if (conn.getResponseCode() < 200 || conn.getResponseCode() >= 300) {
                throw new RuntimeException("Failed : HTTP error code with message : "
                        + conn.getResponseCode() + conn.getResponseMessage());
            }
            else {
                auditSuccess(configId, args);
                LOG.info("Process of "+args[0]+" is completed");
            }

            conn.disconnect();
        } catch (Exception ex) {

            LOG.error("Error processing the data for: " + args[0], ex);
            SlackHelper.sendSlackMessage(SlackHelper.Channel.CIEFhirExtractor, "Exception in processing the data for (" + args[0] + ")", ex);
            auditFailure(configId, args, ex);
        }
    }

    private static void auditSuccess(String queryName, String[] args) throws Exception {
        ScheduledTaskAuditDalI dal = DalProvider.factoryScheduledTaskAuditDal();
        dal.auditTaskSuccess(queryName, args);
    }

    private static void auditFailure(String queryName, String[] args, Throwable t) throws Exception {
        ScheduledTaskAuditDalI dal = DalProvider.factoryScheduledTaskAuditDal();
        dal.auditTaskFailure(queryName, args, t);
    }

    private static String prepareToken(String token_url, String token_msg) {
        String tOutput=null;
        try {
            URL tUrl = new URL(token_url);
            HttpURLConnection tConn = (HttpURLConnection) tUrl.openConnection();
            tConn.setDoOutput(true);
            tConn.setRequestMethod("POST");
            tConn.setRequestProperty("Content-Type", "application/json");
            OutputStream tos = tConn.getOutputStream();
            tos.write(token_msg.getBytes());
            tos.flush();
            if (tConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed Token: HTTP error code : "
                        + tConn.getResponseCode());
            }

            BufferedReader tbr = new BufferedReader(new InputStreamReader(
                    (tConn.getInputStream())));

            if ((tOutput = tbr.readLine()) != null) {
                tOutput=tOutput.substring(tOutput.indexOf(":")+2,tOutput.indexOf(",")-1);
            }
            tConn.disconnect();

        } catch (IOException e) {

            e.printStackTrace();

        }
        return  tOutput;
    }

}