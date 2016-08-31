package org.endeavourhealth.sftpreader;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.endeavourhealth.sftpreader.implementations.*;
import org.endeavourhealth.sftpreader.model.db.*;
import org.endeavourhealth.sftpreader.model.exceptions.SftpFilenameParseException;
import org.endeavourhealth.sftpreader.model.exceptions.SftpReaderException;
import org.endeavourhealth.sftpreader.model.exceptions.SftpValidationException;
import org.endeavourhealth.sftpreader.utilities.KeycloakClient;
import org.endeavourhealth.sftpreader.utilities.PgpUtil;
import org.endeavourhealth.sftpreader.utilities.StreamExtension;
import org.endeavourhealth.sftpreader.utilities.postgres.PgStoredProcException;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnection;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnectionDetails;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnectionException;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpRemoteFile;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class SftpTask extends TimerTask
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SftpTask.class);

    private Configuration configuration = null;
    private DbConfiguration dbConfiguration = null;
    private DataLayer db = null;

    public SftpTask(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void run()
    {
        try
        {
            LOG.trace(">>>Starting scheduled SftpTask run, initialising");
            initialise();

            LOG.trace(">>>Downloading and decrypting files");
            downloadAndProcessFiles();

            LOG.trace(">>>Validating and sequencing batches");
            validateAndSequenceBatches();

            LOG.trace(">>>Notifying EDS");
            notifyEds();

            LOG.trace(">>>Completed SftpTask run");
        }
        catch (Exception e)
        {
            LOG.error(">>>Fatal exception in SftpTask run, terminating this run", e);
        }
    }

    private void initialise() throws Exception
    {
        this.dbConfiguration = configuration.getDbConfiguration();
        this.db = new DataLayer(configuration.getDatabaseConnection());
    }


    private void downloadAndProcessFiles()
    {
        SftpConnection sftpConnection = null;

        try
        {
            sftpConnection = openSftpConnection(dbConfiguration.getDbConfigurationSftp());

            String remotePath = dbConfiguration.getDbConfigurationSftp().getRemotePath();

            //rather than list the files in the remote directory, change to it and list from there
            //as downloading the files without changing to the folder seems to fail sometimes
            sftpConnection.cd(remotePath);
            List<SftpRemoteFile> sftpRemoteFiles = getFileList(sftpConnection, "\\");
            //List<SftpRemoteFile> sftpRemoteFiles = getFileList(sftpConnection, remotePath);

            int countAlreadyProcessed = 0;

            LOG.trace("Found {} files in {}", new Integer(sftpRemoteFiles.size()), remotePath);

            for (SftpRemoteFile sftpRemoteFile : sftpRemoteFiles)
            {
                //LOG.trace("  Processing remote file: {}", sftpRemoteFile.getFilename());

                SftpFile batchFile = instantiateSftpBatchFile(sftpRemoteFile);

                if (!batchFile.isFilenameValid())
                {
                    LOG.error("   Invalid filename, skipping: " + batchFile.getFilename());
                    db.addUnknownFile(dbConfiguration.getInstanceId(), batchFile);
                    continue;
                }

                AddFileResult addFileResult = db.addFile(configuration.getInstanceId(), batchFile);

                if (addFileResult.isFileAlreadyProcessed())
                {
                    countAlreadyProcessed ++;
                    //LOG.trace("   Already processed, skipping: " + batchFile.getFilename());
                    continue;
                }

                batchFile.setBatchFileId(addFileResult.getBatchFileId());

                createBatchDirectory(batchFile);

                downloadFile(sftpConnection, batchFile);

                if (batchFile.doesFileNeedDecrypting())
                    decryptFile(batchFile);
            }

            //logging out a count of how many were already processed, as we've got 400,000 lines of logging on AWS
            if (countAlreadyProcessed > 0) {
                LOG.trace("Skipped {} files as already processed them", new Integer(countAlreadyProcessed));
            }

            LOG.info(" Completed processing {} files", Integer.toString(sftpRemoteFiles.size()));
        }
        catch (Exception e)
        {
            LOG.error("Exception occurred while processing files", e);
        }
        finally
        {
            closeConnection(sftpConnection);
        }
    }

    private static SftpConnection openSftpConnection(DbConfigurationSftp configurationSftp) throws SftpConnectionException, JSchException, IOException
    {
        SftpConnection sftpConnection = new SftpConnection(getSftpConnectionDetails(configurationSftp));

        String hostname = sftpConnection.getConnectionDetails().getHostname();
        String port = Integer.toString(sftpConnection.getConnectionDetails().getPort());
        String username = sftpConnection.getConnectionDetails().getUsername();

        LOG.info(" Opening SFTP connection to " + hostname + " on port " + port + " with user " + username);

        sftpConnection.open();

        return sftpConnection;
    }

    private static SftpConnectionDetails getSftpConnectionDetails(DbConfigurationSftp configurationSftp)
    {
        return new SftpConnectionDetails()
                .setHostname(configurationSftp.getHostname())
                .setPort(configurationSftp.getPort())
                .setUsername(configurationSftp.getUsername())
                .setClientPrivateKey(configurationSftp.getClientPrivateKey())
                .setClientPrivateKeyPassword(configurationSftp.getClientPrivateKeyPassword())
                .setHostPublicKey(configurationSftp.getHostPublicKey());
    }

    private static void closeConnection(SftpConnection sftpConnection) {

        //only attempt to close it, if it was opened
        if (sftpConnection == null) {
            return;
        }
        //LOG.trace("Closing SFTP connection");

        sftpConnection.close();
    }

    private static List<SftpRemoteFile> getFileList(SftpConnection sftpConnection, String remotePath) throws SftpException
    {
        return sftpConnection.getFileList(remotePath);
    }

    private void downloadFile(SftpConnection sftpConnection, SftpFile batchFile) throws Exception
    {
        //the connection has already changed to the remote directory, so download using just the filename, not the full path
        downloadFile(sftpConnection, batchFile.getFilename(), batchFile.getLocalFilePath());
        //downloadFile(sftpConnection, batchFile.getRemoteFilePath(), batchFile.getLocalFilePath());

        batchFile.setLocalFileSizeBytes(getFileSizeBytes(batchFile.getLocalFilePath()));

        db.setFileAsDownloaded(batchFile);
    }

    private static void downloadFile(SftpConnection sftpConnection, String remoteFilePath, String localFilePath) throws SftpException, IOException
    {
        LOG.info("   Downloading file to: " + localFilePath);

        File temporaryDownloadFile = new File(localFilePath + ".download");

        if (temporaryDownloadFile.exists())
            if (!temporaryDownloadFile.delete())
                throw new IOException("Could not delete existing temporary download file " + temporaryDownloadFile);

        InputStream inputStream = sftpConnection.getFile(remoteFilePath);

        Files.copy(inputStream, temporaryDownloadFile.toPath());

        if (!temporaryDownloadFile.renameTo(new File(localFilePath)))
            throw new IOException("Could not temporary download file to " + localFilePath);
    }

    private SftpFile instantiateSftpBatchFile(SftpRemoteFile sftpRemoteFile)
    {
        SftpFilenameParser emisSftpFilenameParser = ImplementationActivator.createFilenameParser(sftpRemoteFile.getFilename(), dbConfiguration);

        return new SftpFile(sftpRemoteFile,
                emisSftpFilenameParser,
                dbConfiguration.getLocalRootPath());
    }

    private void createBatchDirectory(SftpFile batchFile) throws IOException
    {
        File localPath = new File(batchFile.getLocalPath());

        if (!localPath.exists())
            if (!localPath.mkdirs())
                throw new IOException("Could not create path " + localPath);
    }

    private void deleteRemoteFile(SftpConnection sftpConnection, String remoteFilePath) throws SftpException
    {
        LOG.info("Deleting remote file " + remoteFilePath);

        sftpConnection.deleteFile(remoteFilePath);
    }

    private void decryptFile(SftpFile batchFile) throws Exception
    {
        String localFilePath = batchFile.getLocalFilePath();
        String decryptedLocalFilePath = batchFile.getDecryptedLocalFilePath();
        String senderPublicKey = dbConfiguration.getDbConfigurationPgp().getPgpSenderPublicKey();
        String recipientPrivateKey = dbConfiguration.getDbConfigurationPgp().getPgpRecipientPrivateKey();
        String recipientPrivateKeyPassword = dbConfiguration.getDbConfigurationPgp().getPgpRecipientPrivateKeyPassword();

        LOG.info("   Decrypting file to: " + decryptedLocalFilePath);

        PgpUtil.decryptAndVerify(localFilePath, senderPublicKey, recipientPrivateKey, recipientPrivateKeyPassword, decryptedLocalFilePath);

        batchFile.setDecryptedFileSizeBytes(getFileSizeBytes(batchFile.getDecryptedLocalFilePath()));

        db.setFileAsDecrypted(batchFile);
    }

    private static long getFileSizeBytes(String filePath)
    {
        File file = new File(filePath);
        return file.length();
    }

    private void validateAndSequenceBatches() throws PgStoredProcException, SftpValidationException, SftpFilenameParseException
    {
        try
        {
            List<UnknownFile> unknownFiles = getUnknownFiles();

            if (unknownFiles.size() > 0)
                return;

            List<Batch> incompleteBatches = getIncompleteBatches();

            if (incompleteBatches.size() == 0)
                return;

            Batch lastCompleteBatch = db.getLastCompleteBatch(dbConfiguration.getInstanceId());

            validateBatches(incompleteBatches, lastCompleteBatch);
            splitBatches(incompleteBatches);
            sequenceBatches(incompleteBatches, lastCompleteBatch);

        }
        catch (Exception e)
        {
            LOG.error("Error occurred during validation and sequencing", e);
        }
    }

    private List<UnknownFile> getUnknownFiles() throws PgStoredProcException
    {
        LOG.trace(" Checking for unknown files");

        List<UnknownFile> unknownFiles = db.getUnknownFiles(dbConfiguration.getInstanceId());

        if (!unknownFiles.isEmpty()) {
            LOG.error(" There are {} unknown files", unknownFiles.size());
        }

        return unknownFiles;
    }

    private List<Batch> getIncompleteBatches() throws PgStoredProcException
    {
        LOG.trace(" Getting batches ready for validation and sequencing");

        List<Batch> incompleteBatches = db.getIncompleteBatches(dbConfiguration.getInstanceId());

        LOG.trace(" There are {} batches ready for validation and sequencing", Integer.toString(incompleteBatches.size()));

        return incompleteBatches;
    }

    private void validateBatches(List<Batch> incompleteBatches, Batch lastCompleteBatch) throws SftpValidationException
    {
        String batchIdentifiers = StringUtils.join(incompleteBatches
                .stream()
                .map(t -> t.getBatchIdentifier())
                .collect(Collectors.toList()), ", ");

        LOG.trace(" Validating batches: " + batchIdentifiers);

        SftpBatchValidator sftpBatchValidator = ImplementationActivator.createSftpBatchValidator();
        sftpBatchValidator.validateBatches(incompleteBatches, lastCompleteBatch, dbConfiguration);

        LOG.trace(" Completed batch validation");
    }

    private void sequenceBatches(List<Batch> incompleteBatches, Batch lastCompleteBatch) throws SftpValidationException, SftpFilenameParseException, PgStoredProcException
    {
        LOG.trace(" Sequencing batches");

        int nextSequenceNumber = getNextSequenceNumber(lastCompleteBatch);

        SftpBatchSequencer sftpBatchSequencer = ImplementationActivator.createSftpBatchSequencer();
        Map<Batch, Integer> batchSequence = sftpBatchSequencer.determineBatchSequenceNumbers(incompleteBatches, nextSequenceNumber, lastCompleteBatch);

        Map<Batch, Integer> sortedBatchSequence = StreamExtension.sortByValue(batchSequence);

        if (!new HashSet(incompleteBatches).equals(sortedBatchSequence.keySet()))
            throw new SftpValidationException("Batch sequence does not contain all unsequenced batches");

        for (Batch batch : sortedBatchSequence.keySet())
            if (sortedBatchSequence.get(batch).intValue() != nextSequenceNumber++)
                throw new SftpValidationException("Unexpected batch sequence number");

        for (Batch batch : sortedBatchSequence.keySet())
        {
            LOG.debug("  Batch " + batch.getBatchIdentifier() + " sequenced as " + sortedBatchSequence.get(batch).toString());
            db.setBatchAsComplete(batch, sortedBatchSequence.get(batch));
        }

        LOG.trace(" Completed batch sequencing");
    }

    private void splitBatches(List<Batch> batches) throws Exception {
        LOG.trace("Splitting batches");

        for (Batch batch: batches) {

            SftpBatchSplitter sftpBatchSplitter = ImplementationActivator.createSftpBatchSplitter();
            List<BatchSplit> splitBatches = sftpBatchSplitter.splitBatch(batch, db, dbConfiguration);

            for (BatchSplit splitBatch: splitBatches) {
                db.addBatchSplit(splitBatch, dbConfiguration.getInstanceId());
            }
        }

        LOG.trace("Completed splitting");
    }

    private static int getNextSequenceNumber(Batch lastCompleteBatch)
    {
        if (lastCompleteBatch == null)
            return 1;

        return lastCompleteBatch.getSequenceNumber() + 1;
    }

    private void notifyEds() throws PgStoredProcException, SftpReaderException {

        List<BatchSplit> unnotifiedBatchSplits = db.getUnnotifiedBatchSplits(dbConfiguration.getInstanceId());
        LOG.debug("There are {} complete split batches for notification", unnotifiedBatchSplits.size());

        if (dbConfiguration.getDbConfigurationEds().isUseKeycloak())
        {
            LOG.trace("Initialising keycloak at: {}", dbConfiguration.getDbConfigurationEds().getKeycloakTokenUri());

            KeycloakClient.init(dbConfiguration.getDbConfigurationEds().getKeycloakTokenUri(),
                    dbConfiguration.getDbConfigurationEds().getKeycloakRealm(),
                    dbConfiguration.getDbConfigurationEds().getKeycloakUsername(),
                    dbConfiguration.getDbConfigurationEds().getKeycloakPassword(),
                    dbConfiguration.getDbConfigurationEds().getKeycloakClientId());
        }
        else
        {
            LOG.trace("Keycloak is not enabled");
        }

        //hash the split batches by organisation ID
        HashMap<String, List<BatchSplit>> hmByOrg = new HashMap<>();
        for (BatchSplit batchSplit: unnotifiedBatchSplits) {
            List<BatchSplit> list = hmByOrg.get(batchSplit.getOrganisationId());
            if (list == null) {
                list = new ArrayList<>();
                hmByOrg.put(batchSplit.getOrganisationId(), list);
            }
            list.add(batchSplit);
        }

        //then attempt to notify EDS for each organisation
        for (String organisationId: hmByOrg.keySet()) {
            List<BatchSplit> batchSplits = hmByOrg.get(organisationId);

            try
            {
                //sort by sequence ID
                batchSplits = batchSplits
                        .stream()
                        .sorted(Comparator.comparing(t -> t.getBatch().getSequenceNumber()))
                        .collect(Collectors.toList());


                for (BatchSplit batchSplit: batchSplits) {

                    LOG.trace("Notifying EDS for batch split: {}", batchSplit.getBatchSplitId());
                    notify(batchSplit);
                }
            }
            catch (Exception e) {
                LOG.error("Error occurred notifying EDS for batch split", e);
            }
        }

    }


    private void notify(BatchSplit unnotifiedBatchSplit) throws SftpReaderException, PgStoredProcException, IOException
    {
        SftpNotificationCreator sftpNotificationCreator = ImplementationActivator.createSftpNotificationCreator();

        String messagePayload = sftpNotificationCreator.createNotificationMessage(dbConfiguration, unnotifiedBatchSplit);
        EdsEnvelopeBuilder edsEnvelopeBuilder = new EdsEnvelopeBuilder(dbConfiguration.getDbConfigurationEds());
        UUID messageId = UUID.randomUUID();
        String organisationId = unnotifiedBatchSplit.getOrganisationId();
        String outboundMessage = edsEnvelopeBuilder.buildEnvelope(messageId, messagePayload, organisationId);

        try {
            String inboundMessage = notifyEds(outboundMessage);

            db.addBatchNotification(unnotifiedBatchSplit.getBatchId(),
                    unnotifiedBatchSplit.getBatchSplitId(),
                    dbConfiguration.getInstanceId(),
                    messageId,
                    outboundMessage,
                    inboundMessage,
                    true,
                    null);
        }
        catch (Exception e)
        {
            //LOG.error("Error notifying EDS for batch split " + unnotifiedBatchSplit.getBatchSplitId(), e);

            String inboundMessage = e.getMessage();

            db.addBatchNotification(unnotifiedBatchSplit.getBatchId(),
                    unnotifiedBatchSplit.getBatchSplitId(),
                    dbConfiguration.getInstanceId(),
                    messageId,
                    outboundMessage,
                    inboundMessage,
                    false,
                    e.getClass().getName() + " | " + e.getMessage());

            throw new SftpReaderException("Error notifying EDS for batch split " + unnotifiedBatchSplit.getBatchSplitId(), e);
        }
    }

    private String notifyEds(String outboundMessage) throws IOException
    {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build())
        {
            HttpPost httpPost = new HttpPost(dbConfiguration.getDbConfigurationEds().getEdsUrl());
            LOG.trace("Performing POST to {}", dbConfiguration.getDbConfigurationEds().getEdsUrl());

            if (dbConfiguration.getDbConfigurationEds().isUseKeycloak()) {
                Header keycloakHeader = KeycloakClient.instance().getAuthorizationHeader();
                httpPost.addHeader(keycloakHeader);
                LOG.trace("Added keycloak header {}={}", keycloakHeader.getName(), keycloakHeader.getValue());
            }

            //the bundle is being sent as XML, so we need to declare this
            httpPost.addHeader("Content-Type", "text/xml");
            httpPost.setEntity(new ByteArrayEntity(outboundMessage.getBytes()));
            LOG.trace("Set payload to {} bytes", outboundMessage.getBytes().length);
            HttpResponse response = httpClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            LOG.trace("Received HTTP code {}: {}", statusCode, response.getStatusLine());

            List<String> lines = new ArrayList<>();
            lines.add(response.getStatusLine().toString());

            HttpEntity entity = response.getEntity();
            if (entity != null)
            {
                try (InputStream instream = entity.getContent()) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(instream, "UTF-8"));
                    String line = bufferedReader.readLine();
                    while (line != null){
                        LOG.trace(line);
                        line = bufferedReader.readLine();
                        lines.add(line);
                    }
                }
            }

            String responseString = String.join("/n", lines);
            if (statusCode == HttpStatus.SC_OK) {
                return responseString;
            } else {
                throw new IOException(responseString);
            }
        }
    }

}
